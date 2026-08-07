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

public interface IIngredientService {
    IngredientResponse addIngredientToRecipe(String recipeId, IngredientRequestDTO request, String userEmail);
    IngredientResponse updateIngredient(String ingredientId, IngredientRequestDTO request, String userEmail);
    void deleteIngredient(String ingredientId, String userEmail);
}