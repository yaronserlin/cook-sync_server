package com.cooksync_server.dtos.response;

/**
 * A generic Data Transfer Object (DTO) used as a standardized wrapper for all
 * API responses. Ensures a consistent response structure across the
 * application, encapsulating success status, data payloads, error details, and
 * contextual messages.
 *
 * <p>
 * <b>Example:</b></p>
 * <pre>{@code
 * ApiResponse<List<TagResponse>> response = new ApiResponse<>(true, tags, null, "All tags retrieved successfully");
 * }</pre>
 *
 * @param <T> The generic type of the expected data payload and error object.
 * @param success Indicates whether the API request was processed successfully
 * ({@code true}) or failed ({@code false}).
 * @param data The primary payload or requested resource returned upon a
 * successful operation.
 * @param error The error details or related payload returned upon a failed
 * operation.
 * @param message A descriptive human-readable message providing additional
 * context about the result.
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        T error,
        String message
        ) {

}
