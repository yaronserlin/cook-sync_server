package com.cooksync_server.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.dtos.request.CreateRecipeRequest;
import com.cooksync_server.dtos.response.IngredientResponse;
import com.cooksync_server.dtos.response.InstructionResponse;
import com.cooksync_server.dtos.response.RecipeResponse;
import com.cooksync_server.dtos.response.TagResponse;
import com.cooksync_server.dtos.response.UserResponse;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.services.RecipeService;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /*
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
     */
    @GetMapping("/public/all")
    public ResponseEntity<List<RecipeResponse>> getAllRecipes() {

        List<Recipe> recipes = recipeService.getAllRecipes();

        List<RecipeResponse> response = recipes.stream()
                .map(recipe -> RecipeResponse.builder()
                .id(recipe.getId())
                .createdBy(UserResponse.fromEntity(recipe.getCreatedBy()))
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .difficulty(recipe.getDifficulty())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .servings(recipe.getServings())
                .reviewCount(recipe.getReviewCount())
                .createdAt(recipe.getCreatedAt().toString())
                .updatedAt(recipe.getUpdatedAt().toString())
                .tags(TagResponse.fromEntities(recipe.getTags()))
                .ingredients(IngredientResponse.fromEntities(recipe.getIngredients()))
                .instructions(InstructionResponse.fromEntities(recipe.getInstructions()))
                .build())
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<RecipeResponse> getRecipeById(@PathVariable String id) {
        Recipe recipe = recipeService.getRecipeById(id);
        RecipeResponse response = RecipeResponse.builder()
                .id(recipe.getId())
                .createdBy(UserResponse.fromEntity(recipe.getCreatedBy()))
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .difficulty(recipe.getDifficulty())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .servings(recipe.getServings())
                .reviewCount(recipe.getReviewCount())
                .createdAt(recipe.getCreatedAt().toString())
                .updatedAt(recipe.getUpdatedAt().toString())
                .tags(TagResponse.fromEntities(recipe.getTags()))
                .ingredients(IngredientResponse.fromEntities(recipe.getIngredients()))
                .instructions(InstructionResponse.fromEntities(recipe.getInstructions()))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<RecipeResponse>> searchRecipes(@RequestParam String q) {
        return ResponseEntity.ok(recipeService.searchRecipes(q).stream()
                .map(recipe -> RecipeResponse.builder()
                .id(recipe.getId())
                .createdBy(UserResponse.fromEntity(recipe.getCreatedBy()))
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .difficulty(recipe.getDifficulty())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .servings(recipe.getServings())
                .reviewCount(recipe.getReviewCount())
                .createdAt(recipe.getCreatedAt().toString())
                .updatedAt(recipe.getUpdatedAt().toString())
                .tags(TagResponse.fromEntities(recipe.getTags()))
                .ingredients(IngredientResponse.fromEntities(recipe.getIngredients()))
                .instructions(InstructionResponse.fromEntities(recipe.getInstructions()))
                .build())
                .toList());
    }

    @GetMapping("/public/tag/{tagName}")
    public ResponseEntity<List<RecipeResponse>> getRecipesByTag(@PathVariable String tagName) {
        return ResponseEntity.ok(recipeService.findRecipesByTag(tagName).stream()
                .map(recipe -> RecipeResponse.builder()
                .id(recipe.getId())
                .createdBy(UserResponse.fromEntity(recipe.getCreatedBy()))
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .difficulty(recipe.getDifficulty())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .servings(recipe.getServings())
                .reviewCount(recipe.getReviewCount())
                .createdAt(recipe.getCreatedAt().toString())
                .updatedAt(recipe.getUpdatedAt().toString())
                .tags(TagResponse.fromEntities(recipe.getTags()))
                .ingredients(IngredientResponse.fromEntities(recipe.getIngredients()))
                .instructions(InstructionResponse.fromEntities(recipe.getInstructions()))
                .build())
                .toList());
    }

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(@RequestBody CreateRecipeRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        Recipe createdRecipe = recipeService.createRecipe(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(RecipeResponse.builder()
                .id(createdRecipe.getId())
                .createdBy(UserResponse.fromEntity(createdRecipe.getCreatedBy()))
                .title(createdRecipe.getTitle())
                .description(createdRecipe.getDescription())
                .difficulty(createdRecipe.getDifficulty())
                .prepTimeMinutes(createdRecipe.getPrepTimeMinutes())
                .cookTimeMinutes(createdRecipe.getCookTimeMinutes())
                .servings(createdRecipe.getServings())
                .reviewCount(createdRecipe.getReviewCount())
                .createdAt(createdRecipe.getCreatedAt().toString())
                .updatedAt(createdRecipe.getUpdatedAt().toString())
                .tags(TagResponse.fromEntities(createdRecipe.getTags()))
                .ingredients(IngredientResponse.fromEntities(createdRecipe.getIngredients()))
                .instructions(InstructionResponse.fromEntities(createdRecipe.getInstructions()))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(@PathVariable String id, @RequestBody CreateRecipeRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        Recipe updatedRecipe = recipeService.updateRecipe(id, request, userEmail);
        return ResponseEntity.ok(RecipeResponse.builder()
                .id(updatedRecipe.getId())
                .createdBy(UserResponse.fromEntity(updatedRecipe.getCreatedBy()))
                .title(updatedRecipe.getTitle())
                .description(updatedRecipe.getDescription())
                .difficulty(updatedRecipe.getDifficulty())
                .prepTimeMinutes(updatedRecipe.getPrepTimeMinutes())
                .cookTimeMinutes(updatedRecipe.getCookTimeMinutes())
                .servings(updatedRecipe.getServings())
                .reviewCount(updatedRecipe.getReviewCount())
                .createdAt(updatedRecipe.getCreatedAt().toString())
                .updatedAt(updatedRecipe.getUpdatedAt().toString())
                .tags(TagResponse.fromEntities(updatedRecipe.getTags()))
                .ingredients(IngredientResponse.fromEntities(updatedRecipe.getIngredients()))
                .instructions(InstructionResponse.fromEntities(updatedRecipe.getInstructions()))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable String id, Authentication authentication) {
        String userEmail = authentication.getName();
        recipeService.deleteRecipe(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}
