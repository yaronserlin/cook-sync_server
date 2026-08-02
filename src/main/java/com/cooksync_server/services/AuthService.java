package com.cooksync_server.services;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooksync_server.config.JwtUtil;
import com.dtos.request.auth.ChangePasswordRequestDTO;
import com.dtos.request.auth.EmailUpdateRequestDTO;
import com.dtos.request.auth.LoginRequestDTO;
import com.dtos.request.auth.ProfileUpdateRequestDTO;
import com.dtos.request.auth.RegisterRequestDTO;
import com.dtos.request.auth.TokenRefreshRequestDTO;
import com.dtos.response.auth.AuthResponse;
import com.cooksync_server.entities.RefreshToken;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.exceptions.auth.InvalidCredentialsException;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.exceptions.auth.UserAlreadyExistsException;
import com.cooksync_server.repositories.UserRepository;

/**
 * Service class handling user authentication, registration, token refresh, password changes, and account settings.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final String dummyPasswordHash;

    /**
     * Constructs AuthService with required dependencies and initializes timing attack dummy hash.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userRepository repository for user persistence
     * @param passwordEncoder encoder for BCrypt password hashing
     * @param jwtUtil utility for JWT generation and verification
     * @param refreshTokenService service for managing session refresh tokens
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.dummyPasswordHash = passwordEncoder.encode("dummy_password_for_timing_attack_prevention");
    }

    /**
     * Registers a new user account with encoded password and issues initial access and refresh tokens.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request registration details payload
     * @return AuthResponse containing access token, refresh token, and user info
     */
    public AuthResponse register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        User newUser = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .isAdmin(false)
                .build();

        try {
            userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getId(), newUser.isAdmin());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(newUser.getId());

        return new AuthResponse(token, refreshToken.getToken(), newUser.getId(), newUser.getFirstName(), newUser.getLastName(), newUser.isAdmin(), newUser.getAvatarUrl());
    }

    /**
     * Authenticates user credentials with constant-time password comparison to prevent timing attacks.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request login credentials payload
     * @return AuthResponse containing fresh tokens and user info
     */
    public AuthResponse login(LoginRequestDTO request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.email());

        String hashToTest = optionalUser.map(User::getPasswordHash).orElse(this.dummyPasswordHash);
        boolean isPasswordMatch = passwordEncoder.matches(request.password(), hashToTest);

        if (optionalUser.isEmpty() || !isPasswordMatch) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = optionalUser.get();
        if (!user.isEnabled()) {
            throw new UnauthorizedActionException("This account has been disabled.");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.isAdmin());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(token, refreshToken.getToken(), user.getId(), user.getFirstName(), user.getLastName(), user.isAdmin(), user.getAvatarUrl());
    }

    /**
     * Renews access token using a valid refresh token payload.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request refresh token request payload
     * @return AuthResponse containing new access token and existing refresh token
     */
    public AuthResponse refreshToken(TokenRefreshRequestDTO request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.isAdmin());
                    return new AuthResponse(token, requestRefreshToken, user.getId(), user.getFirstName(), user.getLastName(), user.isAdmin(), user.getAvatarUrl());
                })
                .orElseThrow(() -> new UnauthorizedActionException("Refresh token is not in database or is invalid!"));
    }

    /**
     * Validates active JWT token context and returns user profile details without issuing new tokens.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail authenticated user email
     * @return AuthResponse with profile details
     */
    public AuthResponse validateToken(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        return new AuthResponse(null, null, user.getId(), user.getFirstName(), user.getLastName(), user.isAdmin(), user.getAvatarUrl());
    }

    /**
     * Revokes active user refresh tokens upon logout.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail authenticated user email
     */
    public void logout(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        refreshTokenService.deleteByUserId(user.getId());
    }

    /**
     * Updates user avatar picture URL.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail target user email
     * @param avatarUrl new profile picture URL
     */
    @Transactional
    public void updateAvatar(String userEmail, String avatarUrl) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    /**
     * Updates user first and last name profile details.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail target user email
     * @param request profile update request DTO
     */
    @Transactional
    public void updateProfile(String userEmail, ProfileUpdateRequestDTO request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        userRepository.save(user);
    }

    /**
     * Changes user account password following verification of current password, revoking existing sessions.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail target user email
     * @param request password change request DTO
     */
    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequestDTO request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenService.deleteByUserId(user.getId());
    }

    /**
     * Updates user account email address following password verification and issues updated tokens.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail current authenticated user email
     * @param request email update request DTO
     * @return AuthResponse containing updated tokens reflecting new email address
     */
    @Transactional
    public AuthResponse updateEmail(String userEmail, EmailUpdateRequestDTO request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        String newEmail = request.newEmail().trim().toLowerCase();
        if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        user.setEmail(newEmail);
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.isAdmin());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new AuthResponse(token, refreshToken.getToken(), user.getId(), user.getFirstName(), user.getLastName(), user.isAdmin(), user.getAvatarUrl());
    }

    /**
     * Deactivates user account (soft delete) and revokes active refresh tokens.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail target user email
     */
    @Transactional
    public void deactivateAccount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        user.setEnabled(false);
        userRepository.save(user);
        refreshTokenService.deleteByUserId(user.getId());
    }
}
