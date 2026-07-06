package com.cooksync_server.services;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cooksync_server.config.JwtUtil;
import com.cooksync_server.dtos.request.auth.LoginRequest;
import com.cooksync_server.dtos.request.auth.RegisterRequest;
import com.cooksync_server.dtos.response.auth.AuthResponse;
import com.cooksync_server.entities.User;
import com.cooksync_server.repositories.UserRepository;

/**
 * Service class responsible for managing user authentication, including
 * registration, login verification, and security measures against timing
 * attacks.
 *
 * * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final String dummyPasswordHash;

    /**
     * Initializes the authentication service and pre-computes a dummy password
     * hash.
     *
     * @param userRepository Repository for accessing and persisting user data.
     * @param passwordEncoder Encoder for securely hashing and verifying
     * passwords.
     * @param jwtUtil Utility for creating JSON Web Tokens.
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;

        this.dummyPasswordHash = passwordEncoder.encode("dummy_password_for_timing_attack_prevention");
    }

    /**
     * Registers a new user account and returns an authentication token.
     * * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * RegisterRequest req = new RegisterRequest("Alice", "alice@example.com", "SecurePass1!");
     * AuthResponse response = authService.register(req);
     * }</pre>
     *
     * @param request The data transfer object containing the user's name,
     * email, and password.
     * @return An {@link AuthResponse} containing the generated JWT token and
     * the user's basic profile details.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isAdmin(false)
                .build();

        try {
            userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Email is already registered");
        }

        String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getId(), newUser.isAdmin());

        return new AuthResponse(token, newUser.getId(), newUser.getName(), newUser.isAdmin());
    }

    /**
     * Authenticates user credentials and prevents timing attacks via consistent
     * execution time.
     * * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * LoginRequest req = new LoginRequest("alice@example.com", "SecurePass1!");
     * AuthResponse response = authService.login(req);
     * }</pre>
     *
     * @param request The data transfer object containing the user's email and
     * plain-text password.
     * @return An {@link AuthResponse} containing the generated JWT token and
     * the user's basic profile details upon successful login.
     * @throws RuntimeException if the email or password is invalid.
     */
    public AuthResponse login(LoginRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        String hashToTest = optionalUser.map(user -> user.getPasswordHash()).orElse(this.dummyPasswordHash);

        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), hashToTest);

        if (optionalUser.isEmpty() || !isPasswordMatch) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = optionalUser.get();
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.isAdmin());

        return new AuthResponse(token, user.getId(), user.getName(), user.isAdmin());
    }
}
