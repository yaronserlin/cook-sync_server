package com.cooksync_server.dtos.response.recipe;

import java.util.List;
import com.cooksync_server.dtos.response.tags.TagResponse;
import com.cooksync_server.entities.Recipe;

/**
 * Data Transfer Object for a recipe preview/card response.
 * Used in lists (home screen, search results) to save bandwidth by omitting heavy data (ingredients, instructions).
 * Uses Java records for an immutable data carrier.
 */
public record RecipePreviewResponse(
    String id,
    String authorName,
    String title,
    String description,
    String difficulty,
    int prepTimeMinutes,
    int cookTimeMinutes,
    int reviewCount,
    String createdAt,
    List<TagResponse> tags
) {
    /**
     * Maps a persistent Recipe entity to a lightweight RecipePreviewResponse DTO.
     */
    public static RecipePreviewResponse fromEntity(Recipe recipe) {
        return new RecipePreviewResponse(
            recipe.getId(),
            recipe.getCreatedBy() != null ? recipe.getCreatedBy().getName() : null,
            recipe.getTitle(),
            recipe.getDescription(),
            recipe.getDifficulty() != null ? recipe.getDifficulty().name() : null,
            recipe.getPrepTimeMinutes(),
            recipe.getCookTimeMinutes(),
            recipe.getReviewCount(),
            recipe.getCreatedAt() != null ? recipe.getCreatedAt().toString() : null,
            TagResponse.fromEntities(recipe.getTags()) 
        );
    }
}