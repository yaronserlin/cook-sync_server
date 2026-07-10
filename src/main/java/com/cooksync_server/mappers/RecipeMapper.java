package com.cooksync_server.mappers;

import com.cooksync_server.entities.Recipe;
import com.dtos.response.recipe.RecipeResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class RecipeMapper {

    private RecipeMapper() {
    }

    public static RecipeResponse toResponse(Recipe r) {
        if (r == null) {
            return null;
        }
        var createdBy = UserMapper.toResponse(r.getCreatedBy());
        List<com.dtos.response.review.ReviewResponse> reviews = r.getReviews() == null ? List.of() : r.getReviews().stream().map(ReviewMapper::toResponse).collect(Collectors.toList());
        List<com.dtos.response.tags.TagResponse> tags = r.getTags() == null ? List.of() : r.getTags().stream().map(TagMapper::toResponse).collect(Collectors.toList());
        Set<com.dtos.response.ingredient.IngredientResponse> ingredients = r.getIngredients() == null ? Set.of() : r.getIngredients().stream().map(IngredientMapper::toResponse).collect(Collectors.toSet());
        List<com.dtos.response.instruction.InstructionResponse> instructions = r.getInstructions() == null ? List.of() : r.getInstructions().stream().map(InstructionMapper::toResponse).collect(Collectors.toList());
        String created = r.getCreatedAt() == null ? null : r.getCreatedAt().toString();
        String updated = r.getUpdatedAt() == null ? null : r.getUpdatedAt().toString();

        return new RecipeResponse(
                r.getId(),
                createdBy,
                r.getTitle(),
                r.getDescription(),
                r.getDifficulty() == null ? null : r.getDifficulty().name(),
                r.getPrepTimeMinutes(),
                r.getCookTimeMinutes(),
                r.getServings(),
                r.getReviewCount(),
                reviews,
                created,
                updated,
                tags,
                ingredients,
                instructions
        );
    }

    public static com.dtos.response.recipe.RecipePreviewResponse toPreview(Recipe r) {
        if (r == null) {
            return null;
        }
        String authorName = r.getCreatedBy() == null ? null : r.getCreatedBy().getFirstName() + " " + r.getCreatedBy().getLastName();
        List<com.dtos.response.tags.TagResponse> tags = r.getTags() == null ? List.of() : r.getTags().stream().map(TagMapper::toResponse).collect(Collectors.toList());
        String created = r.getCreatedAt() == null ? null : r.getCreatedAt().toString();
        return new com.dtos.response.recipe.RecipePreviewResponse(
                r.getId(),
                authorName,
                r.getTitle(),
                r.getDescription(),
                r.getDifficulty() == null ? null : r.getDifficulty().name(),
                r.getPrepTimeMinutes(),
                r.getCookTimeMinutes(),
                r.getReviewCount(),
                created,
                tags
        );
    }
}
