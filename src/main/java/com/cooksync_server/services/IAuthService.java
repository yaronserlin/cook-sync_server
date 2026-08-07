package com.cooksync_server.services;

import com.dtos.request.auth.ForgotPasswordRequestDTO;
import com.dtos.request.auth.ProfileUpdateRequestDTO;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dtos.request.auth.EmailUpdateRequestDTO;
import com.cooksync_server.entities.PasswordResetToken;
import com.cooksync_server.repositories.PasswordResetTokenRepository;
import com.cooksync_server.exceptions.auth.InvalidCredentialsException;
import com.dtos.request.auth.ResetPasswordRequestDTO;
import com.dtos.request.auth.ChangePasswordRequestDTO;
import com.dtos.request.auth.TokenRefreshRequestDTO;
import com.dtos.response.auth.AuthResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.cooksync_server.exceptions.auth.UserAlreadyExistsException;
import com.cooksync_server.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import com.cooksync_server.entities.RefreshToken;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.config.JwtUtil;
import com.dtos.request.auth.LoginRequestDTO;
import com.dtos.request.auth.RegisterRequestDTO;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.util.Optional;

/**
 * Interface for AuthService.
 */
public interface IAuthService {
    AuthResponse register(RegisterRequestDTO request);

    AuthResponse login(LoginRequestDTO request);

    AuthResponse refreshToken(TokenRefreshRequestDTO request);

    AuthResponse validateToken(String userEmail);

    void logout(String userEmail);

    void updateAvatar(String userEmail, String avatarUrl);

    void updateProfile(String userEmail, ProfileUpdateRequestDTO request);

    void changePassword(String userEmail, ChangePasswordRequestDTO request);

    AuthResponse updateEmail(String userEmail, EmailUpdateRequestDTO request);

    void deactivateAccount(String userEmail);

    void forgotPassword(ForgotPasswordRequestDTO request);

    void resetPassword(ResetPasswordRequestDTO request);

}
