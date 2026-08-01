package com.cooksync_server.mappers;

import com.cooksync_server.entities.Review;
import com.dtos.response.review.ReviewResponse;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewResponse toResponse(Review r) {
        if (r == null) {
            return null;
        }
        String userId = r.getUser() == null ? null : r.getUser().getId();
        String authorName = r.getUser() == null ? null : r.getUser().getFirstName() + " " + r.getUser().getLastName();
        String recipeId = r.getRecipe() == null ? null : r.getRecipe().getId();
        String created = MapperUtils.toIsoStringOrNull(r.getCreatedAt());
        String updated = MapperUtils.toIsoStringOrNull(r.getUpdatedAt());
        return new ReviewResponse(r.getId(), userId, authorName, recipeId, r.getRating(), r.getTitle(), r.getComment(), created, updated);
    }
}
