package com.cooksync_server.mappers;

import com.cooksync_server.entities.Ingredient;
import com.dtos.response.ingredient.IngredientResponse;
import com.cooksync_server.entities.Unit;
import com.dtos.response.unit.UnitResponse;

import java.math.BigDecimal;

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
            String uCreated = unit.getCreatedAt() == null ? null : unit.getCreatedAt().toString();
            String uUpdated = unit.getUpdatedAt() == null ? null : unit.getUpdatedAt().toString();
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

    public static Ingredient fromRequest(com.dtos.request.ingredient.IngredientRequestDTO req) {
        if (req == null) {
            return null;
        }
        Ingredient i = new Ingredient();
        i.setName(req.name());
        i.setQuantity(BigDecimal.valueOf(req.quantity()));
        // caller must set recipe and unit associations
        return i;
    }
}
