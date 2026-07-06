package com.cooksync_server.dtos.response;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.cooksync_server.entities.Ingredient;

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
    private UnitResponse unit;

    public static Set<IngredientResponse> fromEntities(Set<Ingredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .unit(UnitResponse.fromEntity(ingredient.getUnit()))
                .build())
                .collect(Collectors.toSet());
    }
}
