package com.cooksync_server.repositories;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;

@DataJpaTest
@DisplayName("🧪 Data Access Layer Tests - RecipeRepository")
class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("✅ Find recipes by partial title (Ignore Case)")
    void shouldFindRecipesByTitleContainingIgnoreCase() {
        System.out.println("⏳ Starting test: Search recipes by partial title...");

        // Arrange
        User author = User.builder()
                .name("Alice Brown")
                .email("alice@example.com")
                .passwordHash("hash")
                .isAdmin(false)
                .build();
        userRepository.save(author);

        Recipe recipe1 = Recipe.builder()
                .createdBy(author)
                .title("Vegan Chocolate Cake")
                .difficulty(Recipe.Difficulty.MEDIUM)
                .prepTimeMinutes(20)
                .cookTimeMinutes(30)
                .servings(8)
                .build();

        Recipe recipe2 = Recipe.builder()
                .createdBy(author)
                .title("Quick Vegan Pasta")
                .difficulty(Recipe.Difficulty.EASY)
                .prepTimeMinutes(10)
                .cookTimeMinutes(15)
                .servings(2)
                .build();

        recipeRepository.saveAll(List.of(recipe1, recipe2));

        // Act - Searching for "vegan" in lower case
        List<Recipe> foundRecipes = recipeRepository.findByTitleContainingIgnoreCase("vegan");
        List<Recipe> notFoundRecipes = recipeRepository.findByTitleContainingIgnoreCase("beef");

        // Assert
        assertThat(foundRecipes)
                .as("❌ Failure: Expected to find 2 recipes containing 'vegan'!")
                .hasSize(2);

        assertThat(notFoundRecipes)
                .as("❌ Failure: Expected to find 0 recipes containing 'beef'!")
                .isEmpty();

        System.out.println("🎉 Test passed: Title search works correctly ignoring case!");
        System.out.println("---------------------------------------------------");
    }

    @Test
    @DisplayName("✅ Filter recipes by difficulty")
    void shouldFilterRecipesByDifficulty() {
        System.out.println("⏳ Starting test: Filter recipes by difficulty level...");

        // Arrange
        User author = User.builder()
                .name("John Smith")
                .email("john@example.com")
                .passwordHash("hash")
                .isAdmin(false)
                .build();
        userRepository.save(author);

        Recipe easyRecipe = Recipe.builder()
                .createdBy(author)
                .title("Easy Salad")
                .difficulty(Recipe.Difficulty.EASY)
                .prepTimeMinutes(10)
                .cookTimeMinutes(0)
                .servings(1)
                .build();
        recipeRepository.save(easyRecipe);

        // Act
        List<Recipe> easyRecipes = recipeRepository.findByDifficulty(Recipe.Difficulty.EASY);

        // Assert
        assertThat(easyRecipes)
                .as("❌ Failure: Did not find the recipe marked as EASY!")
                .hasSize(1);

        assertThat(easyRecipes.get(0).getTitle())
                .as("❌ Failure: The retrieved recipe title does not match!")
                .isEqualTo("Easy Salad");

        System.out.println("🎉 Test passed: Difficulty filtering is accurate!");
        System.out.println("---------------------------------------------------");
    }
}
