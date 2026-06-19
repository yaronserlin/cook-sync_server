package com.cooksync_server.repositories;

import com.cooksync_server.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    
    // שליפת כל הביקורות של מתכון מסוים
    List<Review> findByRecipeIdOrderByCreatedAtDesc(String recipeId);
}