package com.cooksync_server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.services.AuthService;
import com.dtos.request.auth.AvatarUpdateRequestDTO;
import com.dtos.request.auth.ChangePasswordRequestDTO;
import com.dtos.request.auth.EmailUpdateRequestDTO;
import com.dtos.request.auth.ForgotPasswordRequestDTO;
import com.dtos.request.auth.LoginRequestDTO;
import com.dtos.request.auth.ProfileUpdateRequestDTO;
import com.dtos.request.auth.RegisterRequestDTO;
import com.dtos.request.auth.ResetPasswordRequestDTO;
import com.dtos.request.auth.TokenRefreshRequestDTO;
import com.dtos.response.ApiResponse;
import com.dtos.response.auth.AuthResponse;

import jakarta.validation.Valid;

/**
 * REST Controller exposing user authentication, registration, session management, and profile settings endpoints.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructs AuthController with AuthService dependency.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param authService auth domain service instance
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account with provided credentials.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request registration details payload DTO
     * @return response entity containing AuthResponse payload
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "User registered successfully"));
    }

    /**
     * Authenticates existing user with email and password credentials.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request login credentials payload DTO
     * @return response entity containing AuthResponse payload
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "User logged in successfully"));
    }

    /**
     * Generates a new JWT access token using a valid refresh token payload.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request refresh token request payload DTO
     * @return response entity containing renewed AuthResponse payload
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "Token refreshed successfully"));
    }

    /**
     * Validates active JWT authentication token and returns user profile payload.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param authentication active Spring Security authentication token
     * @return response entity containing AuthResponse payload
     */
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<AuthResponse>> validateToken(Authentication authentication) {
        String userEmail = authentication.getName();
        AuthResponse response = authService.validateToken(userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "Token is valid"));
    }

    /**
     * Invalidates active user refresh token session upon logout.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param authentication active Spring Security authentication token
     * @return response entity acknowledging logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        String userEmail = authentication.getName();
        authService.logout(userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "User logged out successfully"));
    }

    /**
     * Updates profile picture avatar URL for authenticated user.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request avatar URL update payload DTO
     * @param authentication active Spring Security authentication token
     * @return response entity acknowledging avatar update
     */
    @PutMapping("/avatar")
    public ResponseEntity<ApiResponse<Void>> updateAvatar(
            @Valid @RequestBody AvatarUpdateRequestDTO request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        authService.updateAvatar(userEmail, request.avatarUrl());
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Avatar updated successfully"));
    }

    /**
     * Updates first and last name profile details for authenticated user.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request profile details update payload DTO
     * @param authentication active Spring Security authentication token
     * @return response entity acknowledging profile update
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequestDTO request,
            Authentication authentication) {
        authService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Profile updated successfully"));
    }

    /**
     * Updates password for authenticated user following password verification.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request change password payload DTO
     * @param authentication active Spring Security authentication token
     * @return response entity acknowledging password update
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            Authentication authentication) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Password updated successfully"));
    }

    /**
     * Updates email address for authenticated user and issues new JWT tokens.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request email update payload DTO
     * @param authentication active Spring Security authentication token
     * @return response entity containing new AuthResponse payload
     */
    @PutMapping("/email")
    public ResponseEntity<ApiResponse<AuthResponse>> updateEmail(
            @Valid @RequestBody EmailUpdateRequestDTO request,
            Authentication authentication) {
        AuthResponse response = authService.updateEmail(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "Email updated successfully"));
    }

    /**
     * Deactivates account for authenticated user.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param authentication active Spring Security authentication token
     * @return response entity acknowledging account deactivation
     */
    @PatchMapping("/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(Authentication authentication) {
        authService.deactivateAccount(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Account deactivated"));
    }

    /**
     * Initiates the forgot-password flow by emailing a one-time reset token, if the given
     * email belongs to a registered account. Always returns success regardless of whether the
     * email is registered, so the response never reveals account existence.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request forgot-password request payload DTO
     * @return response entity acknowledging the request
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null,
                "If that email is registered, a password reset link has been sent"));
    }

    /**
     * Completes the forgot-password flow by consuming a valid reset token and setting a new
     * account password.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request reset-password request payload DTO
     * @return response entity acknowledging the password reset
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Password reset successfully"));
    }
}
