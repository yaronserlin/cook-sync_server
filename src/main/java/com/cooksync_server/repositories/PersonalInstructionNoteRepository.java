package com.cooksync_server.repositories;

import com.cooksync_server.entities.PersonalInstructionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository interface for PersonalInstructionNote entity persistence.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Repository
public interface PersonalInstructionNoteRepository extends JpaRepository<PersonalInstructionNote, String> {

    /**
     * Retrieves all personal notes created by a specific user for a given recipe.
     *
     * @param userId target user ID
     * @param recipeId target recipe ID
     * @return list of matching personal instruction note entities
     */
    List<PersonalInstructionNote> findAllByUserIdAndRecipeId(String userId, String recipeId);

    /**
     * Checks if a user has created any notes for a specific recipe.
     *
     * @param userId target user ID
     * @param recipeId target recipe ID
     * @return true if user notes exist for recipe
     */
    boolean existsByUserIdAndRecipeId(String userId, String recipeId);

    /**
     * Retrieves the general recipe-wide personal note for a user (where instruction IS NULL).
     *
     * @param userId target user ID
     * @param recipeId target recipe ID
     * @return optional containing personal instruction note if found
     */
    @Query("SELECT n FROM PersonalInstructionNote n WHERE n.user.id = :userId AND n.recipe.id = :recipeId AND n.instruction IS NULL")
    Optional<PersonalInstructionNote> findByUserIdAndRecipeIdAndInstructionIdIsNull(@Param("userId") String userId, @Param("recipeId") String recipeId);

    /**
     * Retrieves a step-specific personal note for a user, recipe, and instruction step ID.
     *
     * @param userId target user ID
     * @param recipeId target recipe ID
     * @param instructionId target instruction step ID
     * @return optional containing personal note for step if found
     */
    @Query("SELECT n FROM PersonalInstructionNote n WHERE n.user.id = :userId AND n.recipe.id = :recipeId AND n.instruction.id = :instructionId")
    Optional<PersonalInstructionNote> findByUserIdAndRecipeIdAndInstructionId(@Param("userId") String userId, @Param("recipeId") String recipeId, @Param("instructionId") String instructionId);
}