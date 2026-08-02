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
 * REST Controller managing recipe catalog browsing, searching, creation, update, and deletion endpoints.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    /**
     * Retrieves all public recipes for general feed display.
     *
     * Complexity:
     * Time: O(N) where N is total public recipe count
     * Space: O(N)
     *
     * @return response entity containing list of RecipePreviewResponse DTOs
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<RecipePreviewResponse>>> getAllRecipes() {
        List<RecipePreviewResponse> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(new ApiResponse<>(true, recipes, null, "Recipes retrieved successfully"));
    }

    /**
     * Retrieves a paginated slice of public recipes for feed infinite scrolling.
     *
     * Complexity:
     * Time: O(S) where S is page size
     * Space: O(S)
     *
     * @param page zero-based page index
     * @param size page size limit
     * @return response entity containing PagedResponse of RecipePreviewResponse DTOs
     */
    @GetMapping("/public/paged")
    public ResponseEntity<ApiResponse<PagedResponse<RecipePreviewResponse>>> getAllRecipesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(new ApiResponse<>(true, recipeService.getAllRecipesPaged(page, size), null, "Recipes retrieved successfully"));
    }

    /**
     * Retrieves full detail view of a single recipe by ID.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target recipe unique identifier
     * @return response entity containing full RecipeResponse DTO
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<RecipeResponse>> getRecipeById(@PathVariable String id) {
        RecipeResponse recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, recipe, null, "Recipe retrieved successfully"));
    }

    /**
     * Executes unified keyword and faceted attribute search across recipe catalog.
     *
     * Complexity:
     * Time: O(M) where M is number of matching recipes returned
     * Space: O(M)
     *
     * @param q unified free-text search string
     * @param author author name filter string
     * @param ingredient ingredient name filter string
     * @return response entity containing search result list of RecipePreviewResponse DTOs
     */
    @GetMapping("/public/search")
    public ResponseEntity<ApiResponse<List<RecipePreviewResponse>>> searchRecipes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String ingredient) {
        List<RecipePreviewResponse> recipes = recipeService.searchRecipes(q, author, ingredient);
        return ResponseEntity.ok(new ApiResponse<>(true, recipes, null, "Search completed"));
    }

    /**
     * Filters public recipes associated with a specific tag name.
     *
     * Complexity:
     * Time: O(T) where T is count of recipes tagged with tag name
     * Space: O(T)
     *
     * @param tagName target tag label name
     * @return response entity containing list of RecipePreviewResponse DTOs
     */
    @GetMapping("/public/tag/{tagName}")
    public ResponseEntity<ApiResponse<List<RecipePreviewResponse>>> getRecipesByTag(@PathVariable String tagName) {
        List<RecipePreviewResponse> recipes = recipeService.findRecipesByTag(tagName);
        return ResponseEntity.ok(new ApiResponse<>(true, recipes, null, "Recipes retrieved by tag"));
    }

    /**
     * Retrieves all recipes authored by the currently authenticated user.
     *
     * Complexity:
     * Time: O(U) where U is count of user authored recipes
     * Space: O(U)
     *
     * @param authentication active user authentication token
     * @return response entity containing user's RecipePreviewResponse DTOs
     */
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<RecipePreviewResponse>>> getMyRecipes(Authentication authentication) {
        List<RecipePreviewResponse> recipes = recipeService.getMyRecipes(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, recipes, null, "Your recipes retrieved successfully"));
    }

    /**
     * Creates a new recipe entry in the system.
     *
     * Complexity:
     * Time: O(I + S + T) where I=ingredients, S=instructions, T=tags
     * Space: O(I + S + T)
     *
     * @param request recipe creation payload DTO
     * @param authentication active user authentication token
     * @return response entity containing created RecipeResponse DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RecipeResponse>> createRecipe(
            @Valid @RequestBody RecipeCreateRequestDTO request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        RecipeResponse createdRecipe = recipeService.createRecipe(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, createdRecipe, null, "Recipe created successfully"));
    }

    /**
     * Updates an existing recipe entry.
     *
     * Complexity:
     * Time: O(I + S + T) where I=ingredients, S=instructions, T=tags
     * Space: O(I + S + T)
     *
     * @param id target recipe unique identifier
     * @param request recipe update payload DTO
     * @param authentication active user authentication token
     * @return response entity containing updated RecipeResponse DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecipeResponse>> updateRecipe(
            @PathVariable String id,
            @Valid @RequestBody RecipeCreateRequestDTO request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        RecipeResponse updatedRecipe = recipeService.updateRecipe(id, request, userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, updatedRecipe, null, "Recipe updated successfully"));
    }

    /**
     * Deletes a recipe by unique ID.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target recipe unique identifier
     * @param authentication active user authentication token
     * @return response entity acknowledging recipe deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(
            @PathVariable String id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        recipeService.deleteRecipe(id, userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Recipe deleted successfully"));
    }
}
