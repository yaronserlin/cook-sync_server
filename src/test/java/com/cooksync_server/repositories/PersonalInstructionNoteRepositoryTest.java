package com.cooksync_server.repositories;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.cooksync_server.entities.Instruction;
import com.cooksync_server.entities.PersonalInstructionNote;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;

@DataJpaTest
@DisplayName("🧪 Data Access Layer Tests - PersonalInstructionNoteRepository")
class PersonalInstructionNoteRepositoryTest {

    @Autowired
    private PersonalInstructionNoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private InstructionRepository instructionRepository;

    @Test
    @DisplayName("✅ Find private note attached to a specific instruction step")
    void shouldFindNoteByUserIdRecipeIdAndInstructionId() {
        System.out.println("⏳ Starting test: Retrieve private note for a specific step...");

        // Arrange
        User user = User.builder()
                .name("Home Cook")
                .email("cook@example.com")
                .passwordHash("hash")
                .isAdmin(false)
                .build();
        userRepository.save(user);

        Recipe recipe = Recipe.builder()
                .createdBy(user)
                .title("Roast Chicken")
                .difficulty(Recipe.Difficulty.HARD)
                .prepTimeMinutes(30)
                .cookTimeMinutes(90)
                .servings(4)
                .build();
        recipeRepository.save(recipe);

        Instruction step = Instruction.builder()
                .recipe(recipe)
                .stepNumber(1)
                .description("Bake the chicken.")
                .hasTimer(true)
                .timeSeconds(5400)
                .build();
        instructionRepository.save(step);

        PersonalInstructionNote note = PersonalInstructionNote.builder()
                .user(user)
                .recipe(recipe)
                .instruction(step)
                .note("Use less salt than the recipe says.")
                .build();
        noteRepository.save(note);

        // Act
        Optional<PersonalInstructionNote> foundNote = noteRepository
                .findByUserIdAndRecipeIdAndInstructionId(user.getId(), recipe.getId(), step.getId());

        // Assert
        assertThat(foundNote)
                .as("❌ Failure: Private note was not found for this specific step!")
                .isPresent();

        assertThat(foundNote.get().getNote())
                .as("❌ Failure: Note content does not match!")
                .isEqualTo("Use less salt than the recipe says.");

        System.out.println("🎉 Test passed: Private step-note retrieved successfully!");
        System.out.println("---------------------------------------------------");
    }
}
