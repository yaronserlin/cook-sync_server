package com.cooksync_server.repositories;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.Review;
import com.cooksync_server.entities.User;

@DataJpaTest
@DisplayName("🧪 Data Access Layer Tests - ReviewRepository")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("✅ Find reviews by recipe ID ordered by creation date descending")
    void shouldFindReviewsOrderedByDateDesc() throws InterruptedException {
        System.out.println("⏳ Starting test: Retrieve newest reviews first...");

        // Arrange
        User user = User.builder()
                .name("Reviewer")
                .email("reviewer@example.com")
                .passwordHash("hash")
                .isAdmin(false)
                .build();
        userRepository.save(user);

        Recipe recipe = Recipe.builder()
                .createdBy(user)
                .title("Burger")
                .difficulty(Recipe.Difficulty.MEDIUM)
                .prepTimeMinutes(15)
                .cookTimeMinutes(15)
                .servings(2)
                .build();
        recipeRepository.save(recipe);

        Review oldReview = Review.builder()
                .user(user)
                .recipe(recipe)
                .rating(new BigDecimal("4.0"))
                .title("Good")
                .comment("Tastes good.")
                .build();
        reviewRepository.save(oldReview);

        // Sleep briefly to ensure timestamps are distinctly different
        Thread.sleep(50);

        Review newReview = Review.builder()
                .user(user)
                .recipe(recipe)
                .rating(new BigDecimal("5.0"))
                .title("Amazing")
                .comment("Best burger ever!")
                .build();
        reviewRepository.save(newReview);

        // Act
        List<Review> reviews = reviewRepository.findByRecipeIdOrderByCreatedAtDesc(recipe.getId());

        // Assert
        assertThat(reviews)
                .as("❌ Failure: Expected 2 reviews!")
                .hasSize(2);

        assertThat(reviews.get(0).getTitle())
                .as("❌ Failure: The newest review is not at the top!")
                .isEqualTo("Amazing");

        System.out.println("🎉 Test passed: Reviews are ordered newest to oldest!");
        System.out.println("---------------------------------------------------");
    }
}
