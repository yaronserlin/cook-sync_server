package com.cooksync_server.repositories;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.cooksync_server.entities.Instruction;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;

@DataJpaTest
@DisplayName("🧪 Data Access Layer Tests - InstructionRepository")
class InstructionRepositoryTest {

    @Autowired
    private InstructionRepository instructionRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("✅ Find instructions by recipe ID ordered by step number ascending")
    void shouldFindInstructionsOrderedByStepNumber() {
        System.out.println("⏳ Starting test: Retrieve instructions in correct order...");

        // Arrange
        User user = User.builder()
                .name("Chef")
                .email("chef@example.com")
                .passwordHash("hash")
                .isAdmin(false)
                .build();
        userRepository.save(user);

        Recipe recipe = Recipe.builder()
                .createdBy(user)
                .title("Pancakes")
                .difficulty(Recipe.Difficulty.EASY)
                .prepTimeMinutes(5)
                .cookTimeMinutes(10)
                .servings(4)
                .build();
        recipeRepository.save(recipe);

        // Saving instructions out of order intentionally
        Instruction step3 = Instruction.builder()
                .recipe(recipe)
                .stepNumber(3)
                .description("Flip the pancake.")
                .hasTimer(false)
                .build();

        Instruction step1 = Instruction.builder()
                .recipe(recipe)
                .stepNumber(1)
                .description("Mix the batter.")
                .hasTimer(false)
                .build();

        Instruction step2 = Instruction.builder()
                .recipe(recipe)
                .stepNumber(2)
                .description("Pour batter into pan.")
                .hasTimer(true)
                .timeSeconds(120)
                .build();

        instructionRepository.saveAll(List.of(step3, step1, step2));

        // Act
        List<Instruction> instructions = instructionRepository.findByRecipeIdOrderByStepNumberAsc(recipe.getId());

        // Assert
        assertThat(instructions)
                .as("❌ Failure: Expected 3 instructions!")
                .hasSize(3);

        assertThat(instructions.get(0).getStepNumber())
                .as("❌ Failure: First instruction is not Step 1!")
                .isEqualTo(1);

        assertThat(instructions.get(2).getStepNumber())
                .as("❌ Failure: Last instruction is not Step 3!")
                .isEqualTo(3);

        System.out.println("🎉 Test passed: Instructions are ordered perfectly!");
        System.out.println("---------------------------------------------------");
    }
}
