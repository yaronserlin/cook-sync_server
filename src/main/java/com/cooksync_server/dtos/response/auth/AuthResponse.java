package com.cooksync_server.dtos.response.auth;

/**
 * Data Transfer Object for authentication responses. Returned to the client
 * after a successful login, registration, or token refresh. Uses Java records
 * for an immutable data carrier.
 */
public record AuthResponse(
        String token,
        String refreshToken,
        String userId,
        String name,
        boolean isAdmin
        ) {

}
