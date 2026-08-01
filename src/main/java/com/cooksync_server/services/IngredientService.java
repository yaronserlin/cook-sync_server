package com.cooksync_server.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.request.ingredient.IngredientRequestDTO;
import com.dtos.response.ingredient.IngredientResponse;
import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.Unit;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.mappers.IngredientMapper;
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

        // Only the recipe's creator or an admin may add ingredients to it.
        OwnershipValidator.requireOwnerOrAdmin(recipe.getCreatedBy().getId(), user,
                "You are not allowed to modify this recipe's ingredients.");

        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", request.unitId()));

        Ingredient ingredient = Ingredient.builder()
                .recipe(recipe)
                .name(request.name())
                .quantity(BigDecimal.valueOf(request.quantity()))
                .unit(unit)
                .build();

        return IngredientMapper.toResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientResponse updateIngredient(String ingredientId, IngredientRequestDTO request, String userEmail) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", ingredientId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        // Authorization is derived from the parent recipe's owner.
        OwnershipValidator.requireOwnerOrAdmin(ingredient.getRecipe().getCreatedBy().getId(), user,
                "You are not allowed to modify this ingredient.");

        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", request.unitId()));

        ingredient.setName(request.name());
        ingredient.setQuantity(BigDecimal.valueOf(request.quantity()));
        ingredient.setUnit(unit);

        return IngredientMapper.toResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void deleteIngredient(String ingredientId, String userEmail) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", ingredientId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        OwnershipValidator.requireOwnerOrAdmin(ingredient.getRecipe().getCreatedBy().getId(), user,
                "You are not allowed to delete this ingredient.");

        ingredientRepository.delete(ingredient);
    }
}
