package com.cooksync_server.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cooksync_server.entities.Review;

/**
 * Spring Data JPA Repository interface for Review entity persistence and moderation operations.
 *
 * @author Yaron Serlin
 * @version 1.1
 * @since 02/08/2026
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    /**
     * Retrieves non-hidden review entries for a given recipe ordered by creation timestamp
     * descending, excluding reviews whose author has a pending account-deletion request.
     *
     * @param recipeId target recipe unique identifier
     * @param pageable pagination parameters
     * @return page of matching review entities
     */
    Page<Review> findByRecipeIdAndHiddenFalseOrderByCreatedAtDesc(String recipeId, Pageable pageable);

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

    /**
     * Retrieves the IDs of every review either authored by the given user or attached to one of
     * the given recipes, i.e. every review that account-deletion processing is about to remove
     * (directly, or via recipe cascade). Used to clean up dependent {@code ReviewReport} rows
     * before those reviews are deleted.
     *
     * @param userId author user ID whose reviews are included
     * @param recipeIds recipe IDs whose reviews are included
     * @return list of matching review IDs
     */
    @Query("SELECT r.id FROM Review r WHERE r.user.id = :userId OR r.recipe.id IN :recipeIds")
    List<String> findIdsByUserIdOrRecipeIdIn(@Param("userId") String userId, @Param("recipeIds") List<String> recipeIds);

    /**
     * Bulk-flips the hidden flag for every review authored by a user, used to hide their reviews
     * when an account-deletion request starts and to restore them if the user logs back in
     * within the grace period. Deliberately does NOT clear the persistence context (unlike the
     * other bulk-cleanup queries in this codebase): both callers in {@code AccountDeletionService}
     * mutate the {@code User} entity in the same transaction around this call, and an automatic
     * clear would detach that entity before its pending field changes (enabled/status/
     * deletionRequestedAt) are flushed — silently discarding them instead of persisting them.
     *
     * @param hidden new hidden flag value
     * @param userId author user ID
     */
    @Modifying
    @Query("UPDATE Review r SET r.hidden = :hidden WHERE r.user.id = :userId")
    void setHiddenByUserId(@Param("hidden") boolean hidden, @Param("userId") String userId);

    /**
     * Bulk-deletes every review authored by a user, as the final permanent-purge step for an
     * expired account-deletion request. Reviews the user authored on their own (already-deleted)
     * recipes are a no-op here since the recipe cascade removes them first.
     *
     * @param userId author user ID
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Review r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") String userId);
}