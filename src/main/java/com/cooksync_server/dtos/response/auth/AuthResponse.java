package com.cooksync_server.dtos.response.auth;

/**
 * Data Transfer Object for authentication responses.
 * Returned to the client after a successful login or registration.
 * Uses Java records for an immutable data carrier.
 */
public record AuthResponse(
    String token,
    String userId,
    String name,
    boolean isAdmin
) {}