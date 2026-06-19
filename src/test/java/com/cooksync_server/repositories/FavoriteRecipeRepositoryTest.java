package com.cooksync_server.repositories;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.cooksync_server.entities.FavoriteRecipe;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;

@DataJpaTest
@DisplayName("🧪 Data Access Layer Tests - FavoriteRecipeRepository")
class FavoriteRecipeRepositoryTest {

    @Autowired
    private FavoriteRecipeRepository favoriteRecipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Test
    @DisplayName("✅ Find all favorite recipes for a specific user")
    void shouldFindFavoritesByUserId() {
        System.out.println("⏳ Starting test: Retrieve user's favorite list...");

        // Arrange - Save User
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hash")
                .isAdmin(false)
                .build();
        userRepository.save(user);

        // Arrange - Save Recipe
        Recipe recipe = Recipe.builder()
                .createdBy(user)
                .title("Favorite Pizza")
                .difficulty(Recipe.Difficulty.HARD)
                .prepTimeMinutes(45)
                .cookTimeMinutes(15)
                .servings(4)
                .build();
        recipeRepository.save(recipe);

        // Arrange - Save Favorite mapping
        FavoriteRecipe favorite = FavoriteRecipe.builder()
                .user(user)
                .recipe(recipe)
                .build();
        favoriteRecipeRepository.save(favorite);

        // Act
        List<FavoriteRecipe> favorites = favoriteRecipeRepository.findByUserId(user.getId());

        // Assert
        assertThat(favorites)
                .as("❌ Failure: Could not retrieve the favorite recipe list for the user!")
                .hasSize(1);

        assertThat(favorites.get(0).getRecipe().getTitle())
                .as("❌ Failure: The title of the favorite recipe does not match!")
                .isEqualTo("Favorite Pizza");

        System.out.println("🎉 Test passed: User's favorite recipes retrieved successfully!");
        System.out.println("---------------------------------------------------");
    }

    @Test
    @DisplayName("✅ Check if a specific recipe is marked as favorite by user")
    void shouldCheckIfFavoriteExistsForUserAndRecipe() {
        System.out.println("⏳ Starting test: Verify specific favorite existence...");

        // Arrange
        User user = User.builder()
                .name("Test User 2")
                .email("test2@example.com")
                .passwordHash("hash")
                .isAdmin(false)
                .build();
        userRepository.save(user);

        Recipe recipe = Recipe.builder()
                .createdBy(user)
                .title("Pasta Alfredo")
                .difficulty(Recipe.Difficulty.MEDIUM)
                .prepTimeMinutes(10)
                .cookTimeMinutes(20)
                .servings(2)
                .build();
        recipeRepository.save(recipe);

        FavoriteRecipe favorite = FavoriteRecipe.builder()
                .user(user)
                .recipe(recipe)
                .build();
        favoriteRecipeRepository.save(favorite);

        // Act
        boolean exists = favoriteRecipeRepository.existsByUserIdAndRecipeId(user.getId(), recipe.getId());

        // Assert
        assertThat(exists)
                .as("❌ Failure: System did not recognize the recipe as a favorite for this user!")
                .isTrue();

        System.out.println("🎉 Test passed: Specific favorite validation works!");
        System.out.println("---------------------------------------------------");
    }
}
