package com.cooksync_server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.dtos.request.auth.LoginRequestDTO;
import com.cooksync_server.dtos.request.auth.RegisterRequestDTO;
import com.cooksync_server.dtos.request.auth.TokenRefreshRequestDTO;
import com.cooksync_server.dtos.response.ApiResponse;
import com.cooksync_server.dtos.response.auth.AuthResponse;
import com.cooksync_server.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "User logged in successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "Token refreshed successfully"));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<AuthResponse>> validateToken(Authentication authentication) {
        System.out.println("Authentication object: " + authentication);
        
        String userEmail = authentication.getName();
        AuthResponse response = authService.validateToken(userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "Token is valid"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        String userEmail = authentication.getName();
        authService.logout(userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "User logged out successfully"));
    }
}
