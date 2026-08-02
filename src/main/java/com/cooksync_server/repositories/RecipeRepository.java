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

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String>, JpaSpecificationExecutor<Recipe> {

    // חיפוש חופשי לפי כותרת (עבור מסך הבית)
    List<Recipe> findByTitleContainingIgnoreCase(String title);

    // סינון לפי רמת קושי
    List<Recipe> findByDifficulty(Recipe.Difficulty difficulty);

    // סינון מתכונים לפי זמן הכנה מקסימלי
    List<Recipe> findByPrepTimeMinutesLessThanEqual(int maxPrepTime);

    // שאילתה מותאמת לסינון מתכונים לפי תגית מסוימת (על בסיס קשר המני-טו-מני)
    @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE t.name = :tagName")
    List<Recipe> findByTagName(@Param("tagName") String tagName);

    // מתכוני משתמש ספציפי (עבור מסך "המתכונים שלי")
    List<Recipe> findByCreatedById(String userId);

    // r.createdBy.enabled = true excludes recipes belonging to deactivated accounts from public listings.
    @Query("SELECT r FROM Recipe r WHERE r.visibility = :visibility AND r.createdBy.enabled = true")
    List<Recipe> findByVisibility(@Param("visibility") Recipe.Visibility visibility);

    @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE t.name = :tagName AND r.visibility = :visibility AND r.createdBy.enabled = true")
    List<Recipe> findByTagNameAndVisibility(@Param("tagName") String tagName, @Param("visibility") Recipe.Visibility visibility);

    // Paged variant of findByVisibility, used by the Home feed's infinite scroll.
    // Kept alongside the unpaged version above (still used by Search/Filters, which need the full list).
    @Query("SELECT r FROM Recipe r WHERE r.visibility = :visibility AND r.createdBy.enabled = true")
    Page<Recipe> findByVisibility(@Param("visibility") Recipe.Visibility visibility, Pageable pageable);
}