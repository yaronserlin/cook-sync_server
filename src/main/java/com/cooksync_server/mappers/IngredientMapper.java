package com.cooksync_server.mappers;

import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.entities.Unit;
import com.dtos.response.ingredient.IngredientResponse;
import com.dtos.response.unit.UnitResponse;

public final class IngredientMapper {

    private IngredientMapper() {
    }

    public static IngredientResponse toResponse(Ingredient entity) {
        if (entity == null) {
            return null;
        }

        Unit unit = entity.getUnit();
        UnitResponse unitResponse = null;
        if (unit != null) {
            String uCreated = MapperUtils.toIsoStringOrNull(unit.getCreatedAt());
            String uUpdated = MapperUtils.toIsoStringOrNull(unit.getUpdatedAt());
            unitResponse = new UnitResponse(unit.getId(), unit.getCode(), unit.getName(), uCreated, uUpdated);
        }

        String recipeId = null;
        if (entity.getRecipe() != null) {
            recipeId = entity.getRecipe().getId();
        }

        return new IngredientResponse(
                entity.getId(),
                entity.getName(),
                entity.getQuantity(),
                recipeId,
                unitResponse
        );
    }
}
