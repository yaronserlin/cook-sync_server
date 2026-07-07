package com.cooksync_server.dtos.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration requests.
 * Uses Java records for an immutable data carrier.
 */
public record RegisterRequestDTO(
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, message = "Name must be at least 2 characters long")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    String name,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email,

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Size(max = 100, message = "Password cannot exceed 100 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", 
             message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    String password
) {}