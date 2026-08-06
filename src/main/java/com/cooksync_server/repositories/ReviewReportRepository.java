package com.cooksync_server.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cooksync_server.entities.ReviewReport;

/**
 * Spring Data JPA Repository interface for ReviewReport entity persistence.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 05/08/2026
 */
@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, String> {

    /**
     * Retrieves the most recently submitted report for a review, used to surface the latest
     * reporter's comment on the admin moderation console alongside the review's flat
     * "currently reported" flag.
     *
     * @param reviewId target review unique identifier
     * @return the most recent matching report, or empty if the review has never been reported
     */
    Optional<ReviewReport> findTopByReviewIdOrderByCreatedAtDesc(String reviewId);
}
