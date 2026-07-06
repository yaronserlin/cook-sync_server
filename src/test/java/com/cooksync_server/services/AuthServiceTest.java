package com.cooksync_server.services;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cooksync_server.config.JwtUtil;
import com.cooksync_server.dtos.request.auth.LoginRequest;
import com.cooksync_server.dtos.request.auth.RegisterRequest;
import com.cooksync_server.dtos.response.auth.AuthResponse;
import com.cooksync_server.entities.User;
import com.cooksync_server.repositories.UserRepository;

@ExtendWith(MockitoExtension.class) // מפעיל את התמיכה של Mockito
@DisplayName("🧪 Business Logic Tests - AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService; // לתוכו יוזרקו כל ה-Mocks שהגדרנו למעלה

    @Test
    @DisplayName("✅ Register a new user successfully")
    void shouldRegisterNewUser() {
        System.out.println("⏳ Starting test: Successful user registration...");

        // Arrange - הכנת בקשת ההרשמה
        RegisterRequest request = new RegisterRequest();
        request.setName("Yaron");
        request.setEmail("yaron@example.com");
        request.setPassword("password123");

        // הגדרת ההתנהגות המזויפת של התלויות
        when(userRepository.existsByEmail("yaron@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_pwd");
        when(jwtUtil.generateToken(any(), any(), anyBoolean())).thenReturn("mocked_jwt_token");

        // Act - ביצוע ההרשמה
        AuthResponse response = authService.register(request);

        // Assert - וידוא התוצאות והתנהגות המערכת
        assertThat(response.getToken()).isEqualTo("mocked_jwt_token");
        assertThat(response.getName()).isEqualTo("Yaron");

        // מוודאים שפונקציית השמירה בדאטה-בייס אכן נקראה פעם אחת בדיוק
        verify(userRepository, times(1)).save(any(User.class));

        System.out.println("🎉 Test passed: Registration logic works!");
        System.out.println("---------------------------------------------------");
    }

    @Test
    @DisplayName("✅ Prevent registration if email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        System.out.println("⏳ Starting test: Prevent duplicate email registration...");

        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        // אומרים למסד הנתונים המזויף לענות "כן, האימייל קיים"
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert - מוודאים שהשירות אכן זורק שגיאה ולא ממשיך
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email is already registered");

        // מוודאים שפונקציית השמירה במסד הנתונים מעולם לא נקראה
        verify(userRepository, never()).save(any(User.class));

        System.out.println("🎉 Test passed: Duplicate email blocked successfully!");
        System.out.println("---------------------------------------------------");
    }

    @Test
    @DisplayName("✅ Login successfully with correct credentials")
    void shouldLoginSuccessfully() {
        System.out.println("⏳ Starting test: Successful login...");

        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("correct_password");

        User fakeUser = User.builder()
                .id("123")
                .name("Test User")
                .email("user@example.com")
                .passwordHash("db_hash")
                .isAdmin(false)
                .build();

        // מגדירים שכשמחפשים את האימייל, חוזר המשתמש המזויף
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(fakeUser));
        // מגדירים שהסיסמה תואמת למה שיש במסד
        when(passwordEncoder.matches("correct_password", "db_hash")).thenReturn(true);
        when(jwtUtil.generateToken("user@example.com", "123", false)).thenReturn("login_jwt_token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response.getToken()).isEqualTo("login_jwt_token");
        assertThat(response.getName()).isEqualTo("Test User");

        System.out.println("🎉 Test passed: Login logic is secure and working!");
        System.out.println("---------------------------------------------------");
    }

    @Test
    @DisplayName("✅ Fail login with incorrect password")
    void shouldFailLoginWithBadPassword() {
        System.out.println("⏳ Starting test: Reject wrong password...");

        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong_password");

        User fakeUser = User.builder()
                .email("user@example.com")
                .passwordHash("db_hash")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(fakeUser));
        // מגדירים שהסיסמה לא תואמת
        when(passwordEncoder.matches("wrong_password", "db_hash")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");

        System.out.println("🎉 Test passed: Bad passwords rejected!");
        System.out.println("---------------------------------------------------");
    }
}
