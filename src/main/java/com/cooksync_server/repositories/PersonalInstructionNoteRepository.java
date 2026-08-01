package com.cooksync_server.repositories;

import com.cooksync_server.entities.PersonalInstructionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalInstructionNoteRepository extends JpaRepository<PersonalInstructionNote, String> {

    // כל ההערות הפרטיות של משתמש על מתכון מסוים (הערה כללית + הערות לשלבים)
    List<PersonalInstructionNote> findAllByUserIdAndRecipeId(String userId, String recipeId);

    boolean existsByUserIdAndRecipeId(String userId, String recipeId);

    // ההערה הכללית (לא משויכת לשלב ספציפי) של משתמש על מתכון.
    // נכתב כ-JPQL מפורש: הנגזרת האוטומטית של Spring Data לא הצליחה לפענח
    // את הנתיב המקונן instruction.id דרך השם המורכב InstructionIdIsNull.
    @Query("SELECT n FROM PersonalInstructionNote n WHERE n.user.id = :userId AND n.recipe.id = :recipeId AND n.instruction IS NULL")
    Optional<PersonalInstructionNote> findByUserIdAndRecipeIdAndInstructionIdIsNull(@Param("userId") String userId, @Param("recipeId") String recipeId);

    // שליפת הערה פרטית של משתמש לשלב ספציפי במתכון
    @Query("SELECT n FROM PersonalInstructionNote n WHERE n.user.id = :userId AND n.recipe.id = :recipeId AND n.instruction.id = :instructionId")
    Optional<PersonalInstructionNote> findByUserIdAndRecipeIdAndInstructionId(@Param("userId") String userId, @Param("recipeId") String recipeId, @Param("instructionId") String instructionId);
}