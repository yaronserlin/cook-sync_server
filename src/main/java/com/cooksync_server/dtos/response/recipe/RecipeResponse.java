package com.cooksync_server.dtos.response.recipe;

import java.util.List;
import java.util.Set;

import com.cooksync_server.dtos.response.ingredient.IngredientResponse;
import com.cooksync_server.dtos.response.instruction.InstructionResponse;
import com.cooksync_server.dtos.response.tags.TagResponse;
import com.cooksync_server.dtos.response.user.UserResponse;
import com.cooksync_server.entities.Recipe;

/**
 * Data Transfer Object for a complete recipe response.
 * Uses Java records for an immutable data carrier.
 */
public record RecipeResponse(
    String id,
    UserResponse createdBy,
    String title,
    String description,
    String difficulty,
    int prepTimeMinutes,
    int cookTimeMinutes,
    int servings,
    int reviewCount,
    String createdAt,
    String updatedAt,
    List<TagResponse> tags,
    Set<IngredientResponse> ingredients,
    List<InstructionResponse> instructions
) {
    /**
     * Maps a persistent Recipe entity to a RecipeResponse DTO.
     */
    public static RecipeResponse fromEntity(Recipe recipe) {
        return new RecipeResponse(
            recipe.getId(),
            UserResponse.fromEntity(recipe.getCreatedBy()),
            recipe.getTitle(),
            recipe.getDescription(),
            recipe.getDifficulty() != null ? recipe.getDifficulty().name() : null,
            recipe.getPrepTimeMinutes(),
            recipe.getCookTimeMinutes(),
            recipe.getServings(),
            recipe.getReviewCount(),
            recipe.getCreatedAt() != null ? recipe.getCreatedAt().toString() : null,
            recipe.getUpdatedAt() != null ? recipe.getUpdatedAt().toString() : null,
            TagResponse.fromEntities(recipe.getTags()),
            IngredientResponse.fromEntities(recipe.getIngredients()),
            InstructionResponse.fromEntities(recipe.getInstructions())
        );
    }
}