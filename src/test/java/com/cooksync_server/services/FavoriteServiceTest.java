package com.cooksync_server.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cooksync_server.entities.FavoriteRecipe;
import com.cooksync_server.entities.PersonalInstructionNote;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;
import com.cooksync_server.repositories.FavoriteRecipeRepository;
import com.cooksync_server.repositories.PersonalInstructionNoteRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.UserRepository;
import com.dtos.response.recipe.RecipePreviewResponse;

/**
 * Covers the Favorites-list fix where a note *indicator* was shown
 * (hasPersonalNote) but the actual note text was never included in the
 * response, so the UI could only ever render a hardcoded placeholder string.
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRecipeRepository favoriteRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PersonalInstructionNoteRepository personalInstructionNoteRepository;

    private FavoriteService favoriteService;

    private final String userId = "user-1";
    private final String userEmail = "ada@example.com";
    private User user;

    @BeforeEach
    void setUp() {
        favoriteService = new FavoriteService(favoriteRepository, recipeRepository, userRepository, personalInstructionNoteRepository);
        user = User.builder().id(userId).email(userEmail).build();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
    }

    @Test
    void getUserFavorites_recipeWithGeneralNote_includesActualNoteText() {
        Recipe recipe = Recipe.builder().id("recipe-1").title("Pasta").build();
        FavoriteRecipe favorite = FavoriteRecipe.builder().id("fav-1").user(user).recipe(recipe).build();
        when(favoriteRepository.findByUserId(userId)).thenReturn(List.of(favorite));
        when(personalInstructionNoteRepository.existsByUserIdAndRecipeId(userId, "recipe-1")).thenReturn(true);

        PersonalInstructionNote generalNote = PersonalInstructionNote.builder()
                .id("note-1").user(user).recipe(recipe).note("Add extra lime juice").build();
        when(personalInstructionNoteRepository.findByUserIdAndRecipeIdAndInstructionIdIsNull(userId, "recipe-1"))
                .thenReturn(Optional.of(generalNote));

        List<RecipePreviewResponse> favorites = favoriteService.getUserFavorites(userEmail);

        assertThat(favorites).hasSize(1);
        RecipePreviewResponse preview = favorites.get(0);
        assertThat(preview.hasPersonalNote()).isTrue();
        assertThat(preview.personalNoteText()).isEqualTo("Add extra lime juice");
    }

    @Test
    void getUserFavorites_recipeWithOnlyAPerStepNote_hasPersonalNoteTrueButNoTextPreview() {
        Recipe recipe = Recipe.builder().id("recipe-2").title("Soup").build();
        FavoriteRecipe favorite = FavoriteRecipe.builder().id("fav-2").user(user).recipe(recipe).build();
        when(favoriteRepository.findByUserId(userId)).thenReturn(List.of(favorite));
        // A per-step note exists (hasPersonalNote reflects "any note"), but there is no general/whole-recipe note.
        when(personalInstructionNoteRepository.existsByUserIdAndRecipeId(userId, "recipe-2")).thenReturn(true);
        when(personalInstructionNoteRepository.findByUserIdAndRecipeIdAndInstructionIdIsNull(userId, "recipe-2"))
                .thenReturn(Optional.empty());

        RecipePreviewResponse preview = favoriteService.getUserFavorites(userEmail).get(0);

        assertThat(preview.hasPersonalNote()).isTrue();
        assertThat(preview.personalNoteText()).isNull();
    }

    @Test
    void getUserFavorites_recipeWithNoNote_hasPersonalNoteFalseAndNoText() {
        Recipe recipe = Recipe.builder().id("recipe-3").title("Salad").build();
        FavoriteRecipe favorite = FavoriteRecipe.builder().id("fav-3").user(user).recipe(recipe).build();
        when(favoriteRepository.findByUserId(userId)).thenReturn(List.of(favorite));
        when(personalInstructionNoteRepository.existsByUserIdAndRecipeId(userId, "recipe-3")).thenReturn(false);
        when(personalInstructionNoteRepository.findByUserIdAndRecipeIdAndInstructionIdIsNull(userId, "recipe-3"))
                .thenReturn(Optional.empty());

        RecipePreviewResponse preview = favoriteService.getUserFavorites(userEmail).get(0);

        assertThat(preview.hasPersonalNote()).isFalse();
        assertThat(preview.personalNoteText()).isNull();
    }
}
