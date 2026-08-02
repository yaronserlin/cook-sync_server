package com.cooksync_server.repositories;

import com.cooksync_server.entities.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA Repository interface for Recipe entity operations and criteria specifications.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String>, JpaSpecificationExecutor<Recipe> {

    /**
     * Case-insensitive free-text search for recipes by title keyword.
     *
     * @param title target title search string
     * @return matching list of recipes
     */
    List<Recipe> findByTitleContainingIgnoreCase(String title);

    /**
     * Filters recipes by difficulty classification level.
     *
     * @param difficulty target difficulty enum
     * @return matching list of recipes
     */
    List<Recipe> findByDifficulty(Recipe.Difficulty difficulty);

    /**
     * Filters recipes with preparation time less than or equal to maximum specified.
     *
     * @param maxPrepTime upper threshold preparation duration in minutes
     * @return matching list of recipes
     */
    List<Recipe> findByPrepTimeMinutesLessThanEqual(int maxPrepTime);

    /**
     * Custom JPQL query selecting recipes tagged with a specific tag name.
     *
     * @param tagName target tag label name
     * @return list of recipes associated with tag
     */
    @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE t.name = :tagName")
    List<Recipe> findByTagName(@Param("tagName") String tagName);

    /**
     * Retrieves all recipes authored by a specific user account ID.
     *
     * @param userId unique user identifier
     * @return list of authored recipe entities
     */
    List<Recipe> findByCreatedById(String userId);

    /**
     * Retrieves public recipes created by active, enabled user accounts.
     *
     * @param visibility visibility setting state
     * @return list of visible public recipes
     */
    @Query("SELECT r FROM Recipe r WHERE r.visibility = :visibility AND r.createdBy.enabled = true")
    List<Recipe> findByVisibility(@Param("visibility") Recipe.Visibility visibility);

    /**
     * Retrieves public recipes associated with tag name created by active accounts.
     *
     * @param tagName target tag label name
     * @param visibility visibility setting state
     * @return list of visible public recipes
     */
    @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE t.name = :tagName AND r.visibility = :visibility AND r.createdBy.enabled = true")
    List<Recipe> findByTagNameAndVisibility(@Param("tagName") String tagName, @Param("visibility") Recipe.Visibility visibility);

    /**
     * Paginated retrieval of public recipes for feed infinite scrolling.
     *
     * @param visibility visibility setting state
     * @param pageable page request criteria
     * @return page of public recipe entities
     */
    @Query("SELECT r FROM Recipe r WHERE r.visibility = :visibility AND r.createdBy.enabled = true")
    Page<Recipe> findByVisibility(@Param("visibility") Recipe.Visibility visibility, Pageable pageable);
}