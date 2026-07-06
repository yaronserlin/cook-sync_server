package com.cooksync_server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.dtos.request.auth.LoginRequest;
import com.cooksync_server.dtos.request.auth.RegisterRequest;
import com.cooksync_server.dtos.response.ApiResponse;
import com.cooksync_server.dtos.response.auth.AuthResponse;
import com.cooksync_server.services.AuthService;

import jakarta.validation.Valid;

/**
 * REST controller handling public authentication endpoints including user
 * registration and login. Exposes endpoints to establish user sessions and
 * issue JWTs wrapped in standardized API responses.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Initializes the authentication controller with the required
     * authentication service.
     *
     * @param authService Service class responsible for executing the core
     * business logic of registration and authentication.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account in the system and returns an authentication
     * token upon success.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "SecurePass1!");
     * ResponseEntity<ApiResponse<AuthResponse>> response = authController.register(request);
     * }</pre>
     *
     * @param request The validated data transfer object containing the user's
     * registration details.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping the {@link AuthResponse} and an HTTP 200 OK status.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "User registered successfully"));
    }

    /**
     * Authenticates a user's credentials and returns a valid JSON Web Token
     * upon successful verification.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * LoginRequest request = new LoginRequest("alice@example.com", "SecurePass1!");
     * ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);
     * }</pre>
     *
     * @param request The validated data transfer object containing the user's
     * login credentials.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping the {@link AuthResponse} and an HTTP 200 OK status.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "User logged in successfully"));
    }
}
