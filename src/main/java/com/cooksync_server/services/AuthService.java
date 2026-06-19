package com.cooksync_server.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cooksync_server.config.JwtUtil;
import com.cooksync_server.dtos.request.LoginRequest;
import com.cooksync_server.dtos.request.RegisterRequest;
import com.cooksync_server.dtos.response.AuthResponse;
import com.cooksync_server.entities.User;
import com.cooksync_server.repositories.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; // הזרקת מפעל הטוקנים

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * הרשמת משתמש חדש
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

        userRepository.save(newUser);

        // יצירת טוקן JWT אמיתי המכיל אימייל, מזהה, והרשאות
        String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getId(), newUser.isAdmin());

        return new AuthResponse(token, newUser.getId(), newUser.getName(), newUser.isAdmin());
    }

    /**
     * התחברות למשתמש קיים
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // יצירת טוקן JWT אמיתי לאחר אימות מוצלח
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.isAdmin());

        return new AuthResponse(token, user.getId(), user.getName(), user.isAdmin());
    }
}
