package com.cooksync_server.controllers;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.dtos.request.ingredient.CreateIngredientRequset;
import com.cooksync_server.dtos.request.tags.CreateTagRequest;
import com.cooksync_server.dtos.response.ApiResponse;
import com.cooksync_server.dtos.response.ingredient.IngredientResponse;
import com.cooksync_server.dtos.response.tags.TagResponse;
import com.cooksync_server.services.IngredientService;
import com.cooksync_server.services.TagService;

import jakarta.persistence.Access;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ingredientController {

    private final IngredientService ingredientService;
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ingredients")
    public ApiResponse<List<IngredientResponse>> getIngredients() {   
        return new ApiResponse<>(true, ingredientService.getAllIngredients(), null, "All ingredients retrieved successfully");
    }
    @GetMapping("{recipeId}/ingredients")
    public ApiResponse<List<IngredientResponse>> getIngredientsByRecipeId(@PathVariable String recipeId) {
        List<IngredientResponse> ingredients = ingredientService.getIngredientsByRecipeId(recipeId);
        return new ApiResponse<>(true, ingredients, null, "Ingredients for recipe retrieved successfully");
    }

    @GetMapping("{recipeId}/{ingredientId}")
    public ApiResponse<IngredientResponse> getIngredient(@PathVariable String recipeId, @PathVariable String ingredientId) {
        IngredientResponse ingredient = ingredientService.getIngredientById(ingredientId);
        return new ApiResponse<>(true, ingredient, null, "Ingredient retrieved successfully");
    }

    @PostMapping("{recipeId}/ingredients")
    public ApiResponse<IngredientResponse> createIngredient(@PathVariable String recipeId, @RequestBody @Valid CreateIngredientRequset ingredientRequest) {
        IngredientResponse ingredient = ingredientService.createIngredient(recipeId, ingredientRequest);
        return new ApiResponse<>(true, ingredient, null, "Ingredient created successfully");
    }

    @DeleteMapping("{recipeId}/{ingredientId}")
    public ApiResponse<Void> deleteIngredient(@PathVariable String recipeId, @PathVariable String ingredientId) {
        ingredientService.deleteIngredientById(ingredientId);
        return new ApiResponse<>(true, null, null, "Ingredient deleted successfully");  
    }
}
