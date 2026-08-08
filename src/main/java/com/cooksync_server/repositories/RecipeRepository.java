package com.cooksync_server.repositories;

import com.cooksync_server.entities.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository interface for Recipe entity operations and criteria specifications.
 * Includes EntityGraph optimizations to eliminate N+1 queries on nested collections.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String>, JpaSpecificationExecutor<Recipe> {

    /**
     * Case-insensitive free-text search for recipes by title keyword.
     *
     * Complexity:
     * Time: O(N) where N is table size
     * Space: O(N)
     *
     * @param title target title search string
     * @return matching list of recipes
     */
    List<Recipe> findByTitleContainingIgnoreCase(String title);

    /**
     * Filters recipes by difficulty classification level.
     *
     * Complexity:
     * Time: O(N)
     * Space: O(N)
     *
     * @param difficulty target difficulty enum
     * @return matching list of recipes
     */
    List<Recipe> findByDifficulty(Recipe.Difficulty difficulty);

    /**
     * Filters recipes with preparation time less than or equal to maximum specified.
     *
     * Complexity:
     * Time: O(N)
     * Space: O(N)
     *
     * @param maxPrepTime upper threshold preparation duration in minutes
     * @return matching list of recipes
     */
    List<Recipe> findByPrepTimeMinutesLessThanEqual(int maxPrepTime);

    /**
     * Custom JPQL query selecting recipes tagged with a specific tag name.
     *
     * Complexity:
     * Time: O(N)
     * Space: O(N)
     *
     * @param tagName target tag label name
     * @return list of recipes associated with tag
     */
    @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE t.name = :tagName")
    List<Recipe> findByTagName(@Param("tagName") String tagName);

    /**
     * Counts how many recipes are tagged with a specific tag ID, for the admin duplicate-tag
     * merge UI's "recipes using this tag" indicator.
     *
     * Complexity:
     * Time: O(1) (indexed join-table lookup)
     * Space: O(1)
     *
     * @param tagId target tag unique identifier
     * @return number of recipes associated with the tag
     */
    @Query("SELECT COUNT(r) FROM Recipe r JOIN r.tags t WHERE t.id = :tagId")
    long countByTagId(@Param("tagId") String tagId);

    /**
     * Retrieves all recipes authored by a specific user account ID.
     *
     * Complexity:
     * Time: O(N)
     * Space: O(N)
     *
     * @param userId unique user identifier
     * @return list of authored recipe entities
     */
    @EntityGraph(attributePaths = {"createdBy", "tags", "images"})
    Page<Recipe> findByCreatedById(String userId, Pageable pageable);

    /**
     * Retrieves public recipes created by active, enabled user accounts with eager graph loading.
     *
     * Complexity:
     * Time: O(N)
     * Space: O(N)
     *
     * @param visibility visibility setting state
     * @return list of visible public recipes
     */
    @EntityGraph(attributePaths = {"createdBy", "tags", "images"})
    @Query("SELECT r FROM Recipe r WHERE r.visibility = :visibility AND r.createdBy.enabled = true")
    List<Recipe> findByVisibility(@Param("visibility") Recipe.Visibility visibility);

    /**
     * Retrieves public recipes associated with tag name created by active accounts.
     *
     * Complexity:
     * Time: O(N)
     * Space: O(N)
     *
     * @param tagName target tag label name
     * @param visibility visibility setting state
     * @return list of visible public recipes
     */
    @EntityGraph(attributePaths = {"createdBy", "tags", "images"})
    @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE t.name = :tagName AND r.visibility = :visibility AND r.createdBy.enabled = true")
    List<Recipe> findByTagNameAndVisibility(@Param("tagName") String tagName, @Param("visibility") Recipe.Visibility visibility);

    /**
     * Paginated retrieval of public recipes for feed infinite scrolling.
     *
     * Complexity:
     * Time: O(S) where S is page size
     * Space: O(S)
     *
     * @param visibility visibility setting state
     * @param pageable page request criteria
     * @return page of public recipe entities
     */
    @EntityGraph(attributePaths = {"createdBy", "tags", "images"})
    @Query("SELECT r FROM Recipe r WHERE r.visibility = :visibility AND r.createdBy.enabled = true")
    Page<Recipe> findByVisibility(@Param("visibility") Recipe.Visibility visibility, Pageable pageable);

    /**
     * Retrieves full recipe graph with all nested relations in a single query execution.
     * Prevents N+1 SELECT overhead when mapping full RecipeResponse DTOs.
     * Deliberately excludes {@code descriptionBlocks}: it is a {@code List} (not a {@code Set}),
     * so joining it alongside the other collection fetches here would multiply it by their
     * Cartesian product instead of being deduplicated the way the Set-typed collections are.
     * See {@link #findDescriptionBlocksByRecipeId(String)} for the companion fetch.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target recipe unique identifier
     * @return optional containing fully initialized Recipe entity if present
     */
    @Query("SELECT DISTINCT r FROM Recipe r " +
           "LEFT JOIN FETCH r.createdBy " +
           "LEFT JOIN FETCH r.tags " +
           "LEFT JOIN FETCH r.images " +
           "LEFT JOIN FETCH r.ingredients i " +
           "LEFT JOIN FETCH i.unit " +
           "LEFT JOIN FETCH r.instructions " +
           "WHERE r.id = :id")
    Optional<Recipe> findByIdWithDetails(@Param("id") String id);

    /**
     * Fetches a recipe's description blocks in isolation, in author-intended sort order.
     * Paired with {@link #findByIdWithDetails(String)} within the same transaction so
     * Hibernate attaches the ordered list to the already-loaded managed Recipe instance.
     *
     * Complexity:
     * Time: O(B) where B is description block count
     * Space: O(B)
     *
     * @param id target recipe unique identifier
     * @return optional containing the recipe with its description blocks initialized
     */
    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.descriptionBlocks WHERE r.id = :id")
    Optional<Recipe> findDescriptionBlocksByRecipeId(@Param("id") String id);
}