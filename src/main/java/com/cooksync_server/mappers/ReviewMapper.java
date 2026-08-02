package com.cooksync_server.mappers;

import com.cooksync_server.entities.Review;
import com.dtos.response.review.ReviewResponse;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        String userId = review.getUser() == null ? null : review.getUser().getId();
        String authorName = review.getUser() == null ? null : review.getUser().getFullName();
        String recipeId = review.getRecipe() == null ? null : review.getRecipe().getId();
        String created = MapperUtils.toIsoStringOrNull(review.getCreatedAt());
        String updated = MapperUtils.toIsoStringOrNull(review.getUpdatedAt());
        return new ReviewResponse(review.getId(), userId, authorName, recipeId, review.getRating(), review.getTitle(),
                review.getComment(), created, updated);
    }
}
