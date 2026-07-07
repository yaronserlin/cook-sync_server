package com.cooksync_server.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooksync_server.dtos.request.ingredient.IngredientRequestDTO;
import com.cooksync_server.dtos.response.ingredient.IngredientResponse;
import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.Unit;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.repositories.IngredientRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.UnitRepository;
import com.cooksync_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;

    @Transactional
    public IngredientResponse addIngredientToRecipe(String recipeId, IngredientRequestDTO request, String userEmail) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        // אימות הרשאות: רק יוצר המתכון או מנהל יכולים להוסיף מצרכים
        if (!recipe.getCreatedBy().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new UnauthorizedActionException("You are not allowed to modify this recipe's ingredients.");
        }

        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", request.unitId()));

        Ingredient ingredient = Ingredient.builder()
                .recipe(recipe)
                .name(request.name())
                .quantity(BigDecimal.valueOf(request.quantity()))
                .unit(unit)
                .build();

        return IngredientResponse.fromEntity(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientResponse updateIngredient(String ingredientId, IngredientRequestDTO request, String userEmail) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", ingredientId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        // אימות הרשאות באמצעות הגישה למתכון אליו שייך המצרך
        if (!ingredient.getRecipe().getCreatedBy().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new UnauthorizedActionException("You are not allowed to modify this ingredient.");
        }

        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", request.unitId()));

        ingredient.setName(request.name());
        ingredient.setQuantity(BigDecimal.valueOf(request.quantity()));
        ingredient.setUnit(unit);

        return IngredientResponse.fromEntity(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void deleteIngredient(String ingredientId, String userEmail) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", ingredientId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (!ingredient.getRecipe().getCreatedBy().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new UnauthorizedActionException("You are not allowed to delete this ingredient.");
        }

        ingredientRepository.delete(ingredient);
    }
}