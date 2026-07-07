package com.cooksync_server.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cooksync_server.dtos.request.ingredient.CreateIngredientRequset;
import com.cooksync_server.dtos.response.ingredient.IngredientResponse;
import com.cooksync_server.dtos.response.unit.UnitResponse;
import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.IngredientRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.UnitRepository;
import com.cooksync_server.entities.Recipe;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final UnitRepository unitRepository;

    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(ingredient -> IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .recipeId(ingredient.getRecipe().getId())
                .unit(UnitResponse.fromEntity(ingredient.getUnit()))
                .build())
                .toList();
    }

    public List<IngredientResponse> getIngredientsByRecipeId(String recipeId) {
        return ingredientRepository.findByRecipeId(recipeId).stream()
                    .map(ingredient -> IngredientResponse.builder()
                    .id(ingredient.getId())
                    .name(ingredient.getName())
                    .quantity(ingredient.getQuantity())
                    .recipeId(ingredient.getRecipe().getId())
                    .unit(UnitResponse.fromEntity(ingredient.getUnit()))
                    .build())
                    .toList();
        }

    public IngredientResponse getIngredientById(String ingredientId) {
        return ingredientRepository.findById(ingredientId)
                .map(ingredient -> IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .recipeId(ingredient.getRecipe().getId())
                .unit(UnitResponse.fromEntity(ingredient.getUnit()))
                .build())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", ingredientId));
    }

    public void deleteIngredientById(String ingredientId) {
        ingredientRepository.deleteById(ingredientId);
    }

    public IngredientResponse createIngredient(String recipeId, CreateIngredientRequset request) {
        var recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe" , recipeId));
        var unit = unitRepository.findByCode(request.getUnitCode())
                .orElseThrow(() -> new ResourceNotFoundException("Unit" , request.getUnitCode()));
        var ingredient = Ingredient.builder()
                .name(request.getName())
                .quantity(request.getQuantity())
                .unit(unit)
                .recipe(recipe)
                .build();
        var savedIngredient = ingredientRepository.save(ingredient);
        return IngredientResponse.builder()
                .id(savedIngredient.getId())
                .name(savedIngredient.getName())
                .quantity(savedIngredient.getQuantity())
                .recipeId(savedIngredient.getRecipe().getId())  
                .unit(UnitResponse.fromEntity(savedIngredient.getUnit()))
                .build();   
                
    }


    

}
