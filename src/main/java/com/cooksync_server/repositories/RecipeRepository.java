package com.cooksync_server.repositories;

import com.cooksync_server.entities.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Advanced search: title/author/ingredient are each optional (null = don't filter on that field), ANDed together.
    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN r.ingredients i "
            + "WHERE r.visibility = :visibility AND r.createdBy.enabled = true "
            + "AND (:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) "
            + "AND (:author IS NULL OR LOWER(CONCAT(r.createdBy.firstName, ' ', r.createdBy.lastName)) LIKE LOWER(CONCAT('%', :author, '%'))) "
            + "AND (:ingredient IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredient, '%')))")
    List<Recipe> searchRecipesAdvanced(@Param("title") String title, @Param("author") String author,
            @Param("ingredient") String ingredient, @Param("visibility") Recipe.Visibility visibility);
}