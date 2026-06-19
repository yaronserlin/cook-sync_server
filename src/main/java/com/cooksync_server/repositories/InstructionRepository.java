package com.cooksync_server.repositories;


import com.cooksync_server.entities.Instruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InstructionRepository extends JpaRepository<Instruction, String> {
    
    // שליפת כל שלבי ההכנה של מתכון מסוים, ממוינים לפי סדר השלבים עבור "מצב מטבח"
    List<Instruction> findByRecipeIdOrderByStepNumberAsc(String recipeId);
}