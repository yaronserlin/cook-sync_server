package com.cooksync_server.dtos.request;

import com.cooksync_server.entities.Recipe.Difficulty;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CreateRecipeRequest {

    private String title;
    private String description;
    private Difficulty difficulty;
    private int prepTimeMinutes;
    private int cookTimeMinutes;
    private int servings;

    private Set<String> tagNames; // רשימה של שמות תגיות לשיוך
    private List<IngredientDto> ingredients;
    private List<InstructionDto> instructions;
}
