package com.cooksync_server.dtos.response.ingredient;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.cooksync_server.dtos.response.unit.UnitResponse;
import com.cooksync_server.entities.Ingredient;

/**
 * Data Transfer Object for an ingredient response.
 */
public record IngredientResponse(
    String id,
    String name,
    BigDecimal quantity,
    String recipeId,
    UnitResponse unit
) {
    /**
     * Maps a persistent Ingredient entity to an IngredientResponse DTO.
     */
    public static IngredientResponse fromEntity(Ingredient ingredient) {
        return new IngredientResponse(
            ingredient.getId(),
            ingredient.getName(),
            ingredient.getQuantity(),
            ingredient.getRecipe() != null ? ingredient.getRecipe().getId() : null,
            UnitResponse.fromEntity(ingredient.getUnit())
        );
    }

    /**
     * Maps a collection of Ingredient entities to a Set of IngredientResponse DTOs.
     */
    public static Set<IngredientResponse> fromEntities(Collection<Ingredient> ingredients) {
        if (ingredients == null) return Set.of();
        
        return ingredients.stream()
                .map(IngredientResponse::fromEntity)
                .collect(Collectors.toSet());
    }
}