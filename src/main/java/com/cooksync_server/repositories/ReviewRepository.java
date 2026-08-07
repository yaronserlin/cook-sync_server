package com.cooksync_server.repositories;

import com.cooksync_server.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Spring Data JPA Repository interface for Review entity persistence and moderation operations.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    /**
     * Retrieves all review entries for a given recipe ordered by creation timestamp descending.
     *
     * @param recipeId target recipe unique identifier
     * @param pageable pagination parameters
     * @return page of matching review entities
     */
    org.springframework.data.domain.Page<Review> findByRecipeIdOrderByCreatedAtDesc(String recipeId, org.springframework.data.domain.Pageable pageable);

    /**
     * Retrieves all reviews flagged as reported for administrative moderation.
     *
     * @return list of reported review entities
     */
    Page<Review> findByReportedTrue(Pageable pageable);

    /**
     * Counts the total number of flagged reported reviews awaiting moderation.
     *
     * @return aggregate count of reported reviews
     */
    long countByReportedTrue();
}