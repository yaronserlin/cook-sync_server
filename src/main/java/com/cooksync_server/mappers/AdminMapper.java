package com.cooksync_server.mappers;

import com.cooksync_server.entities.Review;
import com.dtos.response.admin.ReportedReviewResponse;

/**
 * Mapper utility class transforming administrative entities into response DTO objects.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
public final class AdminMapper {

    private AdminMapper() {
    }

    /**
     * Converts a Review entity into a ReportedReviewResponse DTO for administrative moderation console.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param review target Review entity instance
     * @return populated ReportedReviewResponse DTO or null if entity is null
     */
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
