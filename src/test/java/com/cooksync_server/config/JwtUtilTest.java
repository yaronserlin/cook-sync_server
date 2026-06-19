package com.cooksync_server.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("🧪 Security Tests - JwtUtil")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // מאתחלים את המחלקה לפני כל טסט
        jwtUtil = new JwtUtil();
    }

    @Test
    @DisplayName("✅ Generate and extract token data successfully")
    void shouldGenerateAndExtractToken() {
        System.out.println("⏳ Starting test: Generate and decode JWT...");

        // Arrange
        String email = "gaya@example.com";
        String userId = "user-123";
        boolean isAdmin = true;

        // Act - יצירת הטוקן
        String token = jwtUtil.generateToken(email, userId, isAdmin);

        // Assert - וידוא שהטוקן נוצר ושניתן לחלץ ממנו את המידע המקורי
        assertThat(token).as("❌ Failure: Token was not generated!").isNotBlank();

        assertThat(jwtUtil.extractEmail(token))
                .as("❌ Failure: Extracted email does not match!")
                .isEqualTo(email);

        assertThat(jwtUtil.extractUserId(token))
                .as("❌ Failure: Extracted user ID does not match!")
                .isEqualTo(userId);

        assertThat(jwtUtil.validateToken(token, email))
                .as("❌ Failure: Token validation failed for the correct email!")
                .isTrue();

        System.out.println("🎉 Test passed: JWT encoding and decoding works flawlessly!");
        System.out.println("---------------------------------------------------");
    }

    @Test
    @DisplayName("✅ Fail validation when checking token against wrong email")
    void shouldFailValidationForWrongEmail() {
        System.out.println("⏳ Starting test: Validate token with wrong email...");

        // Arrange
        String token = jwtUtil.generateToken("correct@example.com", "user-1", false);

        // Act
        boolean isValid = jwtUtil.validateToken(token, "wrong@example.com");

        // Assert
        assertThat(isValid)
                .as("❌ Failure: Token was validated successfully despite wrong email!")
                .isFalse();

        System.out.println("🎉 Test passed: Token validation correctly rejected wrong email!");
        System.out.println("---------------------------------------------------");
    }
}
