package com.cooksync_server.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.services.FavoriteService;
import com.dtos.response.ApiResponse;
import com.dtos.response.recipe.RecipePreviewResponse;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller managing user favorite recipe bookmark creation, retrieval, and removal.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * Retrieves all recipe preview entries bookmarked as favorite by the authenticated user.
     *
     * Complexity:
     * Time: O(F) where F is count of user's favorite recipes
     * Space: O(F)
     *
     * @param authentication active user authentication token
     * @return response entity containing list of RecipePreviewResponse DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecipePreviewResponse>>> getUserFavorites(Authentication authentication) {
        String userEmail = authentication.getName();
        List<RecipePreviewResponse> favorites = favoriteService.getUserFavorites(userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, favorites, null, "Favorites retrieved successfully"));
    }

    /**
     * Adds a recipe to the authenticated user's favorite bookmarks.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param recipeId target recipe ID
     * @param authentication active user authentication token
     * @return response entity acknowledging favorite addition
     */
    @PostMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @PathVariable String recipeId, 
            Authentication authentication) {
        String userEmail = authentication.getName();
        favoriteService.addFavorite(recipeId, userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Added to favorites successfully"));
    }

    /**
     * Removes a recipe from the authenticated user's favorite bookmarks.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param recipeId target recipe ID
     * @param authentication active user authentication token
     * @return response entity acknowledging favorite removal
     */
    @DeleteMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable String recipeId, 
            Authentication authentication) {
        String userEmail = authentication.getName();
        favoriteService.removeFavorite(recipeId, userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Removed from favorites successfully"));
    }
}