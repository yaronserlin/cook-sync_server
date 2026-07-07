package com.cooksync_server.dtos.request;

import java.util.List;
import java.util.Set;

import com.cooksync_server.dtos.request.ingredient.CreateIngredientRequset;
import com.cooksync_server.entities.Recipe.Difficulty;

import lombok.Data;

@Data
public class CreateRecipeRequest {

    private String title;
    private String description;
    private Difficulty difficulty;
    private int prepTimeMinutes;
    private int cookTimeMinutes;
    private int servings;

    private Set<String> tagNames; // רשימה של שמות תגיות לשיוך
    private List<CreateIngredientRequset> ingredients;
    private List<InstructionDto> instructions;
}
