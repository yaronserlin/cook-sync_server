package com.cooksync_server.repositories;

import com.cooksync_server.entities.RecipeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeImageRepository extends JpaRepository<RecipeImage, String> {
    
    // שליפת כל התמונות של מתכון מסוים
    List<RecipeImage> findByRecipeId(String recipeId);
}