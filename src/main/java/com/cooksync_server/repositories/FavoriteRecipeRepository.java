package com.cooksync_server.repositories;

import com.cooksync_server.entities.FavoriteRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRecipeRepository extends JpaRepository<FavoriteRecipe, String> {
    
    // שליפת כל המתכונים המועדפים של משתמש ספציפי
    List<FavoriteRecipe> findByUserId(String userId);
    
    // בדיקה האם משתמש כבר שמר מתכון מסוים במועדפים (לצורך סימון הלב ב-UI)
    Optional<FavoriteRecipe> findByUserIdAndRecipeId(String userId, String recipeId);
    
    boolean existsByUserIdAndRecipeId(String userId, String recipeId);

    void deleteByUserIdAndRecipeId(String userId, String recipeId);
    
}