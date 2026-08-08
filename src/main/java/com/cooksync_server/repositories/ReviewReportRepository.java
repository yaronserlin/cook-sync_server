package com.cooksync_server.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Deletes every report filed against a specific review via a single bulk statement,
     * bypassing the persistence context entirely. Called before deleting the review itself,
     * since {@code review_reports.review_id} is a non-nullable foreign key with no cascade
     * delete — leaving report rows behind would fail the review deletion with a database
     * integrity violation. A bulk {@code @Modifying} query is used deliberately instead of a
     * derived entity-based {@code deleteBy...} method: for a {@link ReviewReport} still tracked
     * as managed in the current session (e.g. one just persisted earlier in the same request),
     * Hibernate can resolve the derived delete as an in-memory field update (nulling the
     * {@code review} association) rather than a row delete, which then fails the same
     * not-null constraint this method exists to avoid.
     *
     * @param reviewId target review unique identifier
     */
    @Modifying
    @Query("DELETE FROM ReviewReport r WHERE r.review.id = :reviewId")
    void deleteByReviewId(@Param("reviewId") String reviewId);
}
