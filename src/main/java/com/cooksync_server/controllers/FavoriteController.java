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

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecipePreviewResponse>>> getUserFavorites(Authentication authentication) {
        String userEmail = authentication.getName();
        List<RecipePreviewResponse> favorites = favoriteService.getUserFavorites(userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, favorites, null, "Favorites retrieved successfully"));
    }

    @PostMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @PathVariable String recipeId, 
            Authentication authentication) {
        String userEmail = authentication.getName();
        favoriteService.addFavorite(recipeId, userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Added to favorites successfully"));
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable String recipeId, 
            Authentication authentication) {
        String userEmail = authentication.getName();
        favoriteService.removeFavorite(recipeId, userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Removed from favorites successfully"));
    }
}