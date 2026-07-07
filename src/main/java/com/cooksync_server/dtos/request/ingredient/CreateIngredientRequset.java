package com.cooksync_server.dtos.request.ingredient;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateIngredientRequset {

    @NotBlank(message = "Ingredient name cannot be blank")
    @Size(min = 2, max = 50, message = "Ingredient name must be between 2 and 50 characters")
    private String name;

    // @NotBlank(message = "Ingredient quantity cannot be blank")
    // @Size(min = 1, max = 20, message = "Ingredient quantity must be between 1 and 20 characters")
    // @Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "Ingredient quantity must be a valid number")
    private BigDecimal quantity;

    @NotBlank(message = "Ingredient recipe ID cannot be blank")
    @Size(min = 1, max = 50, message = "Ingredient recipe ID must be between 1 and 50 characters")
    // @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Ingredient recipe ID must contain only letters and numbers")
    private String recipeId;

    @NotBlank(message = "Ingredient unit code cannot be blank")
    @Size(min = 1, max = 10, message = "Ingredient unit code must be between 1 and 10 characters")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Ingredient unit code must contain only letters")
    private String unitCode;


    public String getName() {
        return name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getUnitCode() {
        return unitCode;
    }
}
