package com.cooksync_server.mappers;

import com.cooksync_server.entities.Review;
import com.dtos.response.admin.ReportedReviewResponse;

public final class AdminMapper {

    private AdminMapper() {
    }

    public static ReportedReviewResponse toReportedReviewResponse(Review review) {
        if (review == null) {
            return null;
        }
        String reviewerName = review.getUser() == null ? null : review.getUser().getFullName();
        String reviewerId = review.getUser() == null ? null : review.getUser().getId();
        String recipeId = review.getRecipe() == null ? null : review.getRecipe().getId();
        String recipeTitle = review.getRecipe() == null ? null : review.getRecipe().getTitle();
        String reason = review.getReportReason() == null ? null : review.getReportReason().name();
        String reportedAt = MapperUtils.toIsoStringOrNull(review.getReportedAt());

        return new ReportedReviewResponse(
                review.getId(),
                reviewerName,
                reviewerId,
                recipeId,
                recipeTitle,
                reason,
                review.getComment(),
                review.getRating(),
                reportedAt
        );
    }
}
