package com.cooksync_server.repositories;

import com.cooksync_server.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String> {

    // חיפוש חופשי לפי כותרת (עבור מסך הבית)
    List<Recipe> findByTitleContainingIgnoreCase(String title);

    // סינון לפי רמת קושי
    List<Recipe> findByDifficulty(Recipe.Difficulty difficulty);

    // סינון מתכונים לפי זמן הכנה מקסימלי
    List<Recipe> findByPrepTimeMinutesLessThanEqual(int maxPrepTime);

    // שאילתה מותאמת לסינון מתכונים לפי תגית מסוימת (על בסיס קשר המני-טו-מני)
    @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE t.name = :tagName")
    List<Recipe> findByTagName(@Param("tagName") String tagName);
}