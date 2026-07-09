package com.cooksync_server.services;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cooksync_server.config.JwtUtil;
import com.cooksync_server.dtos.request.auth.LoginRequestDTO;
import com.cooksync_server.dtos.request.auth.RegisterRequestDTO;
import com.cooksync_server.dtos.request.auth.TokenRefreshRequestDTO;
import com.cooksync_server.dtos.response.auth.AuthResponse;
import com.cooksync_server.entities.RefreshToken;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.exceptions.auth.InvalidCredentialsException;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.exceptions.auth.UserAlreadyExistsException;
import com.cooksync_server.repositories.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final String dummyPasswordHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.dummyPasswordHash = passwordEncoder.encode("dummy_password_for_timing_attack_prevention");
    }

    public AuthResponse register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        User newUser = User.builder()
                .name(request.name())
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

        return new AuthResponse(token, refreshToken.getToken(), newUser.getId(), newUser.getName(), newUser.isAdmin());
    }

    public AuthResponse login(LoginRequestDTO request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.email());

        String hashToTest = optionalUser.map(User::getPasswordHash).orElse(this.dummyPasswordHash);
        boolean isPasswordMatch = passwordEncoder.matches(request.password(), hashToTest);

        if (optionalUser.isEmpty() || !isPasswordMatch) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = optionalUser.get();
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.isAdmin());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(token, refreshToken.getToken(), user.getId(), user.getName(), user.isAdmin());
    }

    public AuthResponse refreshToken(TokenRefreshRequestDTO request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.isAdmin());
                    return new AuthResponse(token, requestRefreshToken, user.getId(), user.getName(), user.isAdmin());
                })
                .orElseThrow(() -> new UnauthorizedActionException("Refresh token is not in database or is invalid!"));
    }

    public AuthResponse validateToken(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        // אנו מחזירים תשובה ללא טוקנים חדשים, רק כדי לאשר שהטוקן הקיים תקין ולהחזיר פרטי משתמש
        return new AuthResponse(null, null, user.getId(), user.getName(), user.isAdmin());
    }
}
