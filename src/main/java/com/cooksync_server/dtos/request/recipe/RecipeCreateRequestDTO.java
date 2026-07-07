package com.cooksync_server.dtos.request.recipe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

import com.cooksync_server.dtos.request.ingredient.IngredientRequestDTO;
import com.cooksync_server.dtos.request.instruction.InstructionRequestDTO;

/**
 * Data Transfer Object for creating or updating a recipe.
 * Uses Java records for immutable data carrier.
 */
public record RecipeCreateRequestDTO(
    @NotBlank(message = "Recipe title is required") 
    String title,
    
    String description,
    
    @NotBlank(message = "Difficulty level is required (EASY, MEDIUM, HARD)") 
    String difficulty,
    
    @Min(value = 0, message = "Preparation time cannot be negative") 
    int prepTimeMinutes,
    
    @Min(value = 0, message = "Cooking time cannot be negative") 
    int cookTimeMinutes,
    
    @Min(value = 1, message = "Servings must be at least 1") 
    int servings,
    
    List<String> tagIds,
    
    @NotEmpty(message = "At least one ingredient is required")
    @Valid 
    List<IngredientRequestDTO> ingredients,
    
    @NotEmpty(message = "At least one instruction step is required")
    @Valid 
    List<InstructionRequestDTO> instructions,
    
    String primaryImageUrl
) {}