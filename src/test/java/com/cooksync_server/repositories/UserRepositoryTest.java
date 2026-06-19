package com.cooksync_server.repositories;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.cooksync_server.entities.User;

@DataJpaTest
@DisplayName("🧪 Data Access Layer Tests - UserRepository")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("✅ Find existing user by email")
    void shouldFindUserByEmail() {
        System.out.println("⏳ Starting test: Find user by email...");

        // Arrange - Setup data
        User user = User.builder()
                .name("Gaya")
                .email("gaya@example.com")
                .passwordHash("hashed_password_123")
                .isAdmin(false)
                .build();
        userRepository.save(user);

        // Act - Execute the method being tested
        Optional<User> foundUser = userRepository.findByEmail("gaya@example.com");

        // Assert - Verify the outcome
        assertThat(foundUser)
                .as("❌ Failure: User was not found despite being saved in the database!")
                .isPresent();

        assertThat(foundUser.get().getName())
                .as("❌ Failure: The retrieved name does not match the saved name!")
                .isEqualTo("Gaya");

        System.out.println("🎉 Test passed: User found and data matches!");
        System.out.println("---------------------------------------------------");
    }

    @Test
    @DisplayName("✅ Return empty when searching for non-existent email")
    void shouldReturnEmptyWhenEmailNotFound() {
        System.out.println("⏳ Starting test: Return empty for non-existent email...");

        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser)
                .as("❌ Failure: Expected an empty result for a non-existent email, but a user was found!")
                .isEmpty();

        System.out.println("🎉 Test passed: Handled non-existent email correctly!");
        System.out.println("---------------------------------------------------");
    }

    @Test
    @DisplayName("✅ Check if email exists in the system (exists / does not exist)")
    void shouldReturnTrueIfEmailExists() {
        System.out.println("⏳ Starting test: Verify email existence check...");

        // Arrange
        User user = User.builder()
                .name("Shirko")
                .email("shirko@example.com")
                .passwordHash("secret_hash")
                .isAdmin(true)
                .build();
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByEmail("shirko@example.com");
        boolean notExists = userRepository.existsByEmail("nobody@example.com");

        // Assert
        assertThat(exists)
                .as("❌ Failure: The system claims the saved email does not exist!")
                .isTrue();

        assertThat(notExists)
                .as("❌ Failure: The system claims a fictitious email exists!")
                .isFalse();

        System.out.println("🎉 Test passed: Email existence identified correctly!");
        System.out.println("---------------------------------------------------");
    }
}
