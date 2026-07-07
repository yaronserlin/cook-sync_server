package com.cooksync_server.repositories;

import com.cooksync_server.entities.PersonalInstructionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonalInstructionNoteRepository extends JpaRepository<PersonalInstructionNote, String> {
    
    // שליפת כל ההערות הפרטיות של משתמש על מתכון מסוים
    Optional<PersonalInstructionNote> findByUserIdAndRecipeId(String userId, String recipeId);
    
    // שליפת הערה פרטית של משתמש לשלב ספציפי במתכון
    Optional<PersonalInstructionNote> findByUserIdAndRecipeIdAndInstructionId(String userId, String recipeId, String instructionId);
}