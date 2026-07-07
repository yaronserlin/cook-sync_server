package com.cooksync_server.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooksync_server.dtos.response.recipe.RecipePreviewResponse;
import com.cooksync_server.entities.FavoriteRecipe;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.FavoriteRecipeRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRecipeRepository favoriteRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addFavorite(String recipeId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));

        if (!favoriteRepository.existsByUserIdAndRecipeId(user.getId(), recipe.getId())) {
            FavoriteRecipe favorite = FavoriteRecipe.builder()
                    .user(user)
                    .recipe(recipe)
                    .build();
            favoriteRepository.save(favorite);
        }
    }

    @Transactional
    public void removeFavorite(String recipeId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));

        favoriteRepository.deleteByUserIdAndRecipeId(user.getId(), recipe.getId());
    }

    public List<RecipePreviewResponse> getUserFavorites(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        
        return favoriteRepository.findByUserId(user.getId()).stream()
                .map(fav -> RecipePreviewResponse.fromEntity(fav.getRecipe()))
                .collect(Collectors.toList());
    }
}