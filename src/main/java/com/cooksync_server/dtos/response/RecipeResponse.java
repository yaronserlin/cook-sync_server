package com.cooksync_server.dtos.response;

import java.util.List;
import java.util.Set;

import com.cooksync_server.dtos.response.tags.TagResponse;
import com.cooksync_server.dtos.response.user.UserResponse;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.Recipe.Difficulty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {

    private String id;
    private UserResponse createdBy;
    private String title;
    private String description;
    private Enum<Difficulty> difficulty;
    private int prepTimeMinutes;
    private int cookTimeMinutes;
    private int servings;
    private int reviewCount;
    private String createdAt;
    private String updatedAt;

    private List<TagResponse> tags;
    private Set<IngredientResponse> ingredients;
    private List<InstructionResponse> instructions;

    public static RecipeResponse fromEntity(Recipe recipe) {
        return RecipeResponse.builder()
                .id(recipe.getId())
                .createdBy(UserResponse.fromEntity(recipe.getCreatedBy()))
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .difficulty(recipe.getDifficulty())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .servings(recipe.getServings())
                .reviewCount(recipe.getReviewCount())
                .createdAt(recipe.getCreatedAt().toString())
                .updatedAt(recipe.getUpdatedAt().toString())
                .tags(TagResponse.fromEntities(recipe.getTags()))
                .ingredients(IngredientResponse.fromEntities(recipe.getIngredients()))
                .instructions(InstructionResponse.fromEntities(recipe.getInstructions()))
                .build();
    }
}
