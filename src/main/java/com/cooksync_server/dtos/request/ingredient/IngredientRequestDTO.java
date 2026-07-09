package com.cooksync_server.dtos.request.ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Data Transfer Object for creating or updating a specific ingredient.
 * Uses Java records for an immutable data carrier.
 */
public record IngredientRequestDTO(
    String tmpId,

    @NotBlank(message = "Ingredient name is required") 
    String name,
    
    @Positive(message = "Quantity must be a positive number") 
    double quantity,
    
    @NotNull(message = "Unit ID is required") 
    String unitId
) {}