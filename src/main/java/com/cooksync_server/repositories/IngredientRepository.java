package com.cooksync_server.repositories;

import com.cooksync_server.entities.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, String> {
    List<Ingredient> findByRecipeId(String recipeId);

    Optional<Ingredient> findById(String id);
}