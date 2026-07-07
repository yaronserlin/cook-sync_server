package com.cooksync_server.dtos.response.ingredient;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.cooksync_server.dtos.response.RecipeResponse;
import com.cooksync_server.dtos.response.unit.UnitResponse;
import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.entities.Recipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientResponse {

    private String id;
    private String name;
    private BigDecimal quantity;
    private String recipeId;
    private UnitResponse unit;

    public static Set<IngredientResponse> fromEntities(Set<Ingredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .recipeId(ingredient.getRecipe().getId())
                .unit(UnitResponse.fromEntity(ingredient.getUnit()))
                .build())
                .collect(Collectors.toSet());
    }
}
