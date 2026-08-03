package com.cooksync_server.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.RecipeImage;
import com.cooksync_server.entities.User;
import com.dtos.response.recipe.RecipePreviewResponse;
import com.dtos.response.recipe.RecipeResponse;
import com.dtos.response.user.UserResponse;

/**
 * Unit test for ImageMapping entity-to-DTO image extraction functions.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
class ImageMappingTest {

    /**
     * Verifies that primary and secondary recipe image URLs map to response DTOs.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void shouldExposePrimaryAndAdditionalRecipeImagesInResponses() {
        Recipe recipe = Recipe.builder()
                .id("recipe-1")
                .title("Pasta")
                .build();

        recipe.setImages(List.of(
                RecipeImage.builder().imageUrl("https://example.com/primary.jpg").isPrimary(true).build(),
                RecipeImage.builder().imageUrl("https://example.com/extra-1.jpg").isPrimary(false).build(),
                RecipeImage.builder().imageUrl("https://example.com/extra-2.jpg").isPrimary(false).build()));

        RecipeResponse response = RecipeMapper.toResponse(recipe);
        RecipePreviewResponse preview = RecipeMapper.toPreview(recipe);

        assertEquals("https://example.com/primary.jpg", response.primaryImageUrl());
        assertEquals(List.of(
                "https://example.com/primary.jpg",
                "https://example.com/extra-1.jpg",
                "https://example.com/extra-2.jpg"), response.imageUrls());
        assertEquals("https://example.com/primary.jpg", preview.primaryImageUrl());
    }

    /**
     * Verifies that user avatar URLs map to user response DTOs.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void shouldExposeUserAvatarInResponse() {
        User user = User.builder()
                .id("user-1")
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@example.com")
                .passwordHash("hash")
                .avatarUrl("https://example.com/avatar.png")
                .build();

        UserResponse response = UserMapper.toResponse(user);

        assertEquals("https://example.com/avatar.png", response.avatarUrl());
    }
}
