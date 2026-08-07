package com.cooksync_server.services;

import com.cooksync_server.entities.FavoriteRecipe;
import com.cooksync_server.entities.Recipe;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import com.cooksync_server.repositories.UserRepository;
import org.springframework.stereotype.Service;
import com.cooksync_server.entities.User;
import org.springframework.transaction.annotation.Transactional;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.PersonalInstructionNoteRepository;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import java.util.List;
import com.dtos.response.recipe.RecipePreviewResponse;
import com.cooksync_server.repositories.FavoriteRecipeRepository;
import java.util.Optional;
import com.cooksync_server.entities.PersonalInstructionNote;
import com.cooksync_server.mappers.RecipeMapper;

/**
 * Interface for FavoriteService.
 */
public interface IFavoriteService {
    void addFavorite(String recipeId, String userEmail);

    void removeFavorite(String recipeId, String userEmail);

    com.dtos.response.PagedResponse<RecipePreviewResponse> getUserFavorites(String userEmail, int page, int size);

}
