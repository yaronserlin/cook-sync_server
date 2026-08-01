package com.cooksync_server.mappers;

import com.cooksync_server.entities.Review;
import com.dtos.response.admin.ReportedReviewResponse;

public final class AdminMapper {

    private AdminMapper() {
    }

    public static ReportedReviewResponse toReportedReviewResponse(Review r) {
        if (r == null) {
            return null;
        }
        String reviewerName = r.getUser() == null ? null : r.getUser().getFullName();
        String reviewerId = r.getUser() == null ? null : r.getUser().getId();
        String recipeId = r.getRecipe() == null ? null : r.getRecipe().getId();
        String recipeTitle = r.getRecipe() == null ? null : r.getRecipe().getTitle();
        String reason = r.getReportReason() == null ? null : r.getReportReason().name();
        String reportedAt = MapperUtils.toIsoStringOrNull(r.getReportedAt());

        return new ReportedReviewResponse(
                r.getId(),
                reviewerName,
                reviewerId,
                recipeId,
                recipeTitle,
                reason,
                r.getComment(),
                r.getRating(),
                reportedAt
        );
    }
}
