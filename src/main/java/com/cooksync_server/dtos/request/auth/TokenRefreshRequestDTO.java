package com.cooksync_server.dtos.request.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for requesting a new access token using a refresh token.
 */
public record TokenRefreshRequestDTO(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
        ) {

}
