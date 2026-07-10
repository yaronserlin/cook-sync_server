package com.cooksync_server.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.request.review.ReviewRequestDTO;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.Review;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.ReviewRepository;
import com.cooksync_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addReview(String recipeId, ReviewRequestDTO request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));

        Review review = Review.builder()
                .user(user)
                .recipe(recipe)
                .rating(BigDecimal.valueOf(request.rating()))
                .title(request.title())
                .comment(request.comment())
                .build();

        reviewRepository.save(review);

        // Update recipe review count
        recipe.setReviewCount(recipe.getReviewCount() + 1);
        recipeRepository.save(recipe);
    }

    @Transactional
    public void deleteReview(String reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        // Only the review author or an admin can delete a review
        if (!review.getUser().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new UnauthorizedActionException("You are not allowed to delete this review.");
        }

        Recipe recipe = review.getRecipe();
        recipe.setReviewCount(Math.max(0, recipe.getReviewCount() - 1));
        recipeRepository.save(recipe);

        reviewRepository.delete(review);
    }
}
