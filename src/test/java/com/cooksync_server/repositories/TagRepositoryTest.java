package com.cooksync_server.repositories;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.cooksync_server.entities.Tag;

@DataJpaTest
@DisplayName("🧪 Data Access Layer Tests - TagRepository")
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("✅ Find tag by exact name ignoring case")
    void shouldFindByNameIgnoreCase() {
        System.out.println("⏳ Starting test: Find tag by name (Ignore Case)...");

        // Arrange
        Tag tag = Tag.builder()
                .name("Gluten Free")
                .build();
        tagRepository.save(tag);

        // Act - Search using different casing
        Optional<Tag> foundTag = tagRepository.findByNameIgnoreCase("gLuTeN fReE");

        // Assert
        assertThat(foundTag)
                .as("❌ Failure: Tag was not found when searching with different casing!")
                .isPresent();

        assertThat(foundTag.get().getName())
                .as("❌ Failure: Retrieved tag name does not match the original!")
                .isEqualTo("Gluten Free");

        System.out.println("🎉 Test passed: Tag found regardless of case!");
        System.out.println("---------------------------------------------------");
    }

    @Test
    @DisplayName("✅ Check if tag exists ignoring case")
    void shouldReturnTrueIfExistsByNameIgnoreCase() {
        System.out.println("⏳ Starting test: Verify tag existence check...");

        // Arrange
        Tag tag = Tag.builder()
                .name("Quick")
                .build();
        tagRepository.save(tag);

        // Act
        boolean exists = tagRepository.existsByNameIgnoreCase("QUICK");
        boolean notExists = tagRepository.existsByNameIgnoreCase("Dessert");

        // Assert
        assertThat(exists)
                .as("❌ Failure: System failed to recognize an existing tag with uppercase letters!")
                .isTrue();

        assertThat(notExists)
                .as("❌ Failure: System claimed a non-existent tag exists!")
                .isFalse();

        System.out.println("🎉 Test passed: Tag existence check works correctly!");
        System.out.println("---------------------------------------------------");
    }
}
