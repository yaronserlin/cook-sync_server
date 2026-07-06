package com.cooksync_server.dtos.response;

import java.util.List;
import java.util.Set;

import com.cooksync_server.dtos.response.tags.TagResponse;
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
public class RecipeCardResponse {

    private String id;
    private String createdByName;
    private String title;
    private String description;
    private Enum<Difficulty> difficulty;
    private int prepTimeMinutes;
    private int cookTimeMinutes;
    private int servings;
    private int reviewCount;
    private String createdAt;

    private List<TagResponse> tags;
    private Set<IngredientResponse> ingredients;
    private List<InstructionResponse> instructions;

    public static RecipeCardResponse fromEntity(Recipe recipe) {
        return RecipeCardResponse.builder()
                .id(recipe.getId())
                .createdByName(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getName() : null)
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .difficulty(recipe.getDifficulty())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .servings(recipe.getServings())
                .reviewCount(recipe.getReviewCount())
                .createdAt(recipe.getCreatedAt() != null ? recipe.getCreatedAt().toString() : null)
                .tags(TagResponse.fromEntities(recipe.getTags()))
                .build();
    }
}
