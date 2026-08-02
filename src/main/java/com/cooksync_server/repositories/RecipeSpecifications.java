package com.cooksync_server.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.Tag;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/** Reusable JPA {@link Specification} building blocks for recipe search/filtering. */
public final class RecipeSpecifications {

    private RecipeSpecifications() {
    }

    public static Specification<Recipe> isPublicAndEnabled() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("visibility"), Recipe.Visibility.PUBLIC),
                cb.isTrue(root.get("createdBy").get("enabled")));
    }

    public static Specification<Recipe> hasAuthor(String author) {
        if (author == null || author.isBlank()) {
            return null;
        }
        String pattern = "%" + author.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(authorName(root, cb)), pattern);
    }

    public static Specification<Recipe> hasIngredient(String ingredient) {
        if (ingredient == null || ingredient.isBlank()) {
            return null;
        }
        String pattern = "%" + ingredient.toLowerCase() + "%";
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Recipe, Ingredient> ingredients = root.join("ingredients");
            return cb.like(cb.lower(ingredients.get("name")), pattern);
        };
    }

    /**
     * Unified search bar: splits the query into whitespace-separated tokens. A recipe matches
     * only if EVERY token is found in at least one of title / author name / tag name / ingredient
     * name. This lets a single field handle plain title searches, author lookups, tag names, and
     * multi-ingredient queries like "cucumber tomato lettuce" (which requires all three ingredients
     * to be present, since each becomes its own AND-ed token).
     */
    public static Specification<Recipe> matchesUnifiedQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }
        String[] tokens = rawQuery.trim().toLowerCase().split("\\s+");
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> tokenPredicates = new ArrayList<>();
            for (String token : tokens) {
                String pattern = "%" + token + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate authorMatch = cb.like(cb.lower(authorName(root, cb)), pattern);
                Predicate tagMatch = RecipeSpecifications.<Tag>existsInCollection(root, query, cb, "tags", "name", pattern);
                Predicate ingredientMatch = RecipeSpecifications.<Ingredient>existsInCollection(root, query, cb, "ingredients", "name", pattern);
                tokenPredicates.add(cb.or(titleMatch, authorMatch, tagMatch, ingredientMatch));
            }
            return cb.and(tokenPredicates.toArray(new Predicate[0]));
        };
    }

    /** Combines specifications with AND, skipping any that are null (i.e. "no filter for this field"). */
    @SafeVarargs
    public static Specification<Recipe> combine(Specification<Recipe>... specs) {
        Specification<Recipe> result = (root, query, cb) -> cb.conjunction();
        for (Specification<Recipe> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }

    private static jakarta.persistence.criteria.Expression<String> authorName(Root<Recipe> root, CriteriaBuilder cb) {
        return cb.concat(cb.concat(root.get("createdBy").get("firstName"), " "), root.get("createdBy").get("lastName"));
    }

    /** Correlated EXISTS subquery against a to-many collection, so multiple tokens don't multiply joins on the root query. */
    private static <T> Predicate existsInCollection(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb,
            String collectionAttribute, String fieldName, String pattern) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Recipe> subRoot = subquery.correlate(root);
        Join<Recipe, T> join = subRoot.join(collectionAttribute);
        subquery.select(cb.literal(1L)).where(cb.like(cb.lower(join.get(fieldName)), pattern));
        return cb.exists(subquery);
    }
}
