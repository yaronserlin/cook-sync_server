package com.cooksync_server.dtos.response.review;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.cooksync_server.entities.Review;

/**
 * Data Transfer Object for a review response.
 */
public record ReviewResponse(
    String id,
    String userId,
    String authorName,
    String recipeId,
    BigDecimal rating,
    String title,
    String comment,
    String createdAt,
    String updatedAt
) {
    /**
     * Maps a persistent Review entity to a ReviewResponse DTO.
     */
    public static ReviewResponse fromEntity(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getUser().getId(),
            review.getUser().getName(),
            review.getRecipe().getId(),
            review.getRating(),
            review.getTitle(),
            review.getComment(),
            review.getCreatedAt() != null ? review.getCreatedAt().toString() : null,
            review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : null
        );
    }

    /**
     * Maps a collection of Review entities to a List of ReviewResponse DTOs.
     */
    public static List<ReviewResponse> fromEntities(Collection<Review> reviews) {
        if (reviews == null) return List.of();
        
        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }
}