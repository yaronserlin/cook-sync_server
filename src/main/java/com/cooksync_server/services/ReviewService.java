package com.cooksync_server.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import com.dtos.request.review.ReportReviewRequestDTO;
import com.dtos.request.review.ReviewRequestDTO;
import com.dtos.response.review.ReviewResponse;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.Review;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.mappers.ReviewMapper;
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

    /**
     * Retrieves all reviews for a recipe. Confirmed missing entirely on the
     * server before this fix — the client's GET call was hitting a 500
     * ("Request method not supported") since no such endpoint existed.
     */
    public List<ReviewResponse> getReviewsForRecipe(String recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResourceNotFoundException("Recipe", recipeId);
        }
        return reviewRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId).stream()
                .map(ReviewMapper::toResponse)
                .toList();
    }

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

        // Count/average are denormalized onto Recipe so recipe list views avoid
        // aggregating the reviews table on every read.
        recipe.setReviewCount(recipe.getReviewCount() + 1);
        recipe.getReviews().add(review);
        recomputeAverageRating(recipe);
        recipeRepository.save(recipe);
    }

    @Transactional
    public void deleteReview(String reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        // Only the review author or an admin can delete a review
        OwnershipValidator.requireOwnerOrAdmin(review.getUser().getId(), currentUser,
                "You are not allowed to delete this review.");

        Recipe recipe = review.getRecipe();
        recipe.setReviewCount(Math.max(0, recipe.getReviewCount() - 1));
        recipe.getReviews().removeIf(r -> r.getId().equals(review.getId()));
        recomputeAverageRating(recipe);
        recipeRepository.save(recipe);

        reviewRepository.delete(review);
    }

    @Transactional
    public void reportReview(String reviewId, ReportReviewRequestDTO request, String userEmail) {
        // Ensures the reporter is a real, authenticated user without requiring
        // any particular relationship to the review being reported.
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        review.setReported(true);
        review.setReportReason(Review.ReportReason.valueOf(request.reason().toUpperCase()));
        review.setReportedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    private void recomputeAverageRating(Recipe recipe) {
        List<Review> reviews = recipe.getReviews();
        if (reviews == null || reviews.isEmpty()) {
            recipe.setAverageRating(null);
            return;
        }
        double average = reviews.stream()
                .mapToDouble(r -> r.getRating().doubleValue())
                .average()
                .orElse(0.0);
        recipe.setAverageRating(average);
    }
}
