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

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRecipeRepository favoriteRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final PersonalInstructionNoteRepository personalInstructionNoteRepository;

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

        // A single lookup per favorite (rather than an exists-check plus a separate
        // fetch) halves the query count against personal_instruction_notes here.
        return favoriteRepository.findByUserId(user.getId()).stream()
                .map(fav -> {
                    Optional<PersonalInstructionNote> note = personalInstructionNoteRepository
                            .findByUserIdAndRecipeIdAndInstructionIdIsNull(user.getId(), fav.getRecipe().getId());
                    return RecipeMapper.toPreview(fav.getRecipe(), note.isPresent(), note.map(PersonalInstructionNote::getNote).orElse(null));
                })
                .collect(Collectors.toList());
    }
}
