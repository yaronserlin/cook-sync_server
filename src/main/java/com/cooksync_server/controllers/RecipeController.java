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

import com.dtos.request.recipe.RecipeCreateRequestDTO;
import com.dtos.response.ApiResponse;
import com.dtos.response.PagedResponse;
import com.dtos.response.recipe.RecipeResponse;
import com.dtos.response.recipe.RecipePreviewResponse;
import com.cooksync_server.services.RecipeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for managing recipes. Public endpoints are accessible without
 * authentication, while modification endpoints require a valid JWT.
 */
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<RecipePreviewResponse>>> getAllRecipes() {
        List<RecipePreviewResponse> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(new ApiResponse<>(true, recipes, null, "Recipes retrieved successfully"));
    }

    /** Paged variant of {@link #getAllRecipes()}, used by the Home feed's infinite scroll. */
    @GetMapping("/public/paged")
    public ResponseEntity<ApiResponse<PagedResponse<RecipePreviewResponse>>> getAllRecipesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(new ApiResponse<>(true, recipeService.getAllRecipesPaged(page, size), null, "Recipes retrieved successfully"));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<RecipeResponse>> getRecipeById(@PathVariable String id) {
        RecipeResponse recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, recipe, null, "Recipe retrieved successfully"));
    }

    /**
     * Unified search: {@code q} is matched against title, author, tags, and ingredients at once
     * (space-separated terms are ANDed, e.g. "cucumber tomato lettuce" finds recipes containing all
     * three ingredients). The optional {@code author}/{@code ingredient} params layer on additional
     * AND-ed filters for the advanced-search fields. {@link #getRecipesByTag} remains available for
     * direct tag-chip navigation.
     */
    @GetMapping("/public/search")
    public ResponseEntity<ApiResponse<List<RecipePreviewResponse>>> searchRecipes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String ingredient) {
        List<RecipePreviewResponse> recipes = recipeService.searchRecipes(q, author, ingredient);
        return ResponseEntity.ok(new ApiResponse<>(true, recipes, null, "Search completed"));
    }

    @GetMapping("/public/tag/{tagName}")
    public ResponseEntity<ApiResponse<List<RecipePreviewResponse>>> getRecipesByTag(@PathVariable String tagName) {
        List<RecipePreviewResponse> recipes = recipeService.findRecipesByTag(tagName);
        return ResponseEntity.ok(new ApiResponse<>(true, recipes, null, "Recipes retrieved by tag"));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<RecipePreviewResponse>>> getMyRecipes(Authentication authentication) {
        List<RecipePreviewResponse> recipes = recipeService.getMyRecipes(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, recipes, null, "Your recipes retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecipeResponse>> createRecipe(
            @Valid @RequestBody RecipeCreateRequestDTO request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        RecipeResponse createdRecipe = recipeService.createRecipe(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, createdRecipe, null, "Recipe created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecipeResponse>> updateRecipe(
            @PathVariable String id,
            @Valid @RequestBody RecipeCreateRequestDTO request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        RecipeResponse updatedRecipe = recipeService.updateRecipe(id, request, userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, updatedRecipe, null, "Recipe updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(
            @PathVariable String id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        recipeService.deleteRecipe(id, userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Recipe deleted successfully"));
    }
}
