package com.cooksync_server.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.response.recipe.RecipePreviewResponse;
import com.cooksync_server.entities.FavoriteRecipe;
import com.cooksync_server.entities.PersonalInstructionNote;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.mappers.RecipeMapper;
import com.cooksync_server.repositories.FavoriteRecipeRepository;
import com.cooksync_server.repositories.PersonalInstructionNoteRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service class managing user favorite recipe bookmark additions, removals, and retrievals.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRecipeRepository favoriteRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final PersonalInstructionNoteRepository personalInstructionNoteRepository;

    /**
     * Adds a recipe to the user's favorite bookmarks if not already bookmarked.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param recipeId target recipe ID
     * @param userEmail authenticated user email address
     */
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

    /**
     * Removes a recipe from the user's favorite bookmarks.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param recipeId target recipe ID
     * @param userEmail authenticated user email address
     */
    @Transactional
    public void removeFavorite(String recipeId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));

        favoriteRepository.deleteByUserIdAndRecipeId(user.getId(), recipe.getId());
    }

    /**
     * Retrieves all recipe preview entries bookmarked as favorite by the user.
     *
     * Complexity:
     * Time: O(F) where F is count of bookmarked favorite recipes
     * Space: O(F)
     *
     * @param userEmail authenticated user email address
     * @return list of RecipePreviewResponse DTOs with personal notes if present
     */
    public List<RecipePreviewResponse> getUserFavorites(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        return favoriteRepository.findByUserId(user.getId()).stream()
                .map(fav -> {
                    Optional<PersonalInstructionNote> note = personalInstructionNoteRepository
                            .findByUserIdAndRecipeIdAndInstructionIdIsNull(user.getId(), fav.getRecipe().getId());
                    return RecipeMapper.toPreview(fav.getRecipe(), note.isPresent(), note.map(PersonalInstructionNote::getNote).orElse(null));
                })
                .collect(Collectors.toList());
    }
}
