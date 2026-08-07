package com.cooksync_server.repositories;

import com.cooksync_server.entities.FavoriteRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository interface for FavoriteRecipe entity management.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Repository
public interface FavoriteRecipeRepository extends JpaRepository<FavoriteRecipe, String> {

    /**
     * Retrieves a paginated list of favorite recipe bookmark entries for a specific user ID.
     *
     * @param userId unique user identifier
     * @param pageable pagination and sorting information
     * @return page of favorite recipe entities
     */
    org.springframework.data.domain.Page<FavoriteRecipe> findByUserId(String userId, org.springframework.data.domain.Pageable pageable);

    /**
     * Finds a favorite recipe relation by user ID and recipe ID.
     *
     * @param userId unique user identifier
     * @param recipeId unique recipe identifier
     * @return optional containing favorite recipe if bookmarked
     */
    Optional<FavoriteRecipe> findByUserIdAndRecipeId(String userId, String recipeId);

    /**
     * Checks if a user has bookmarked a specific recipe.
     *
     * @param userId unique user identifier
     * @param recipeId unique recipe identifier
     * @return true if favorite relation exists
     */
    boolean existsByUserIdAndRecipeId(String userId, String recipeId);

    /**
     * Deletes a favorite recipe bookmark relation by user ID and recipe ID.
     *
     * @param userId unique user identifier
     * @param recipeId unique recipe identifier
     */
    void deleteByUserIdAndRecipeId(String userId, String recipeId);
}