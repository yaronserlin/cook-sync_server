package com.cooksync_server.dtos.response;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;

class RecipeCardResponseTest {

    @Test
    void fromEntityShouldExposeOnlyCardSummaryFields() {
        User creator = new User();
        creator.setId("user-1");
        creator.setName("Ada");
        creator.setEmail("ada@example.com");
        creator.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        creator.setUpdatedAt(LocalDateTime.of(2024, 1, 2, 10, 0));
        creator.setAdmin(false);

        Recipe recipe = Recipe.builder()
                .id("recipe-1")
                .createdBy(creator)
                .title("Pasta")
                .description("Quick dinner")
                .difficulty(Recipe.Difficulty.EASY)
                .prepTimeMinutes(10)
                .cookTimeMinutes(15)
                .servings(2)
                .reviewCount(4)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 2, 10, 0))
                .tags(new ArrayList<>())
                .ingredients(new java.util.HashSet<>())
                .instructions(new ArrayList<>())
                .build();

        RecipeCardResponse response = RecipeCardResponse.fromEntity(recipe);

        assertEquals("recipe-1", response.getId());
        assertEquals("Pasta", response.getTitle());
        assertEquals("Quick dinner", response.getDescription());
        assertEquals(Recipe.Difficulty.EASY, response.getDifficulty());
        assertEquals(10, response.getPrepTimeMinutes());
        assertEquals(15, response.getCookTimeMinutes());
        assertEquals(2, response.getServings());
        assertEquals(4, response.getReviewCount());
        assertEquals("Ada", response.getCreatedByName());
        assertEquals("2024-01-01T10:00", response.getCreatedAt());
        assertNull(response.getIngredients());
        assertNull(response.getInstructions());
    }
}
