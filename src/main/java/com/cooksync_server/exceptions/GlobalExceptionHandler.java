package com.cooksync_server.exceptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cooksync_server.dtos.response.ApiResponse;
import com.cooksync_server.dtos.response.errors.ApiErrorResponse;
import com.cooksync_server.exceptions.auth.InvalidCredentialsException;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.exceptions.auth.UserAlreadyExistsException;

/**
 * Global exception handler to intercept and process exceptions thrown across
 * the application. Translates specific runtime exceptions into standardized
 * HTTP responses using the generic ApiResponse wrapper.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions thrown when a requested resource cannot be found in
     * the system.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * // Thrown in a service layer:
     * throw new ResourceNotFoundException("Recipe not found");
     * // Caught here and mapped to 404 NOT_FOUND.
     * }</pre>
     *
     * @param ex The exception containing the missing resource details.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping an {@link ApiErrorResponse} with a 404 Not Found status.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, null, new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        "RESOURCE_NOT_FOUND",
                        ex.getMessage(),
                        "",
                        null
                ), null
                ));
    }

    /**
     * Handles exceptions thrown when a user attempts to create a resource that
     * already exists in the system.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * // Thrown in a service layer:
     * throw new ResourceAlreadyExistsException("Tag", "Vegan");
     * // Caught here and mapped to 409 CONFLICT.
     * }</pre>
     *
     * @param ex The exception containing the duplication conflict details.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping an {@link ApiErrorResponse} with a 409 Conflict status.
     */
    @ExceptionHandler(ResourceAllReadyExistsException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleResourceAlreadyExistsException(ResourceAllReadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, null, new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.CONFLICT.value(),
                        "Conflict",
                        "RESOURCE_ALREADY_EXISTS",
                        ex.getMessage(),
                        "",
                        null
                ), null
                ));
    }

    /**
     * Handles exceptions thrown when a user attempts to perform an action
     * without sufficient permissions.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * throw new UnauthorizedActionException("You cannot edit this recipe");
     * // Mapped to 403 FORBIDDEN.
     * }</pre>
     *
     * @param ex The exception containing the authorization failure details.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping an {@link ApiErrorResponse} with a 403 Forbidden status.
     */
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleUnauthorizedAction(UnauthorizedActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, null, new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden",
                        "UNAUTHORIZED_ACTION",
                        ex.getMessage(),
                        "",
                        null
                ), null
                ));
    }

    /**
     * Handles authentication exceptions caused by incorrect user credentials.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * throw new InvalidCredentialsException();
     * // Mapped to 401 UNAUTHORIZED.
     * }</pre>
     *
     * @param ex The exception triggered by the failed login attempt.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping an {@link ApiErrorResponse} with a 401 Unauthorized status.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, null, new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        "INVALID_CREDENTIALS",
                        ex.getMessage(),
                        "",
                        null
                ), null
                ));
    }

    /**
     * Handles data integrity conflicts, specifically when a user attempts to
     * register with an existing identifier.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * throw new UserAlreadyExistsException("Email is already registered");
     * // Mapped to 409 CONFLICT.
     * }</pre>
     *
     * @param ex The exception containing the duplication conflict details.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping an {@link ApiErrorResponse} with a 409 Conflict status.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, null, new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.CONFLICT.value(),
                        "Conflict",
                        "USER_ALREADY_EXISTS",
                        ex.getMessage(),
                        "",
                        null
                ), null
                ));
    }

    /**
     * Handles validation exceptions thrown when a request payload fails to pass
     * @Valid constraints (e.g., Jakarta Validation).
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * // Thrown when a client submits a CreateTagRequest with a blank name.
     * // Mapped to 400 BAD_REQUEST with a list of specific error messages.
     * }</pre>
     *
     * @param ex The exception containing the validation errors.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping a list of {@link ApiErrorResponse} objects with a 400 Bad
     * Request status.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ApiErrorResponse>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse> errors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String errorMessage = error.getDefaultMessage();
            errors.add(new ApiErrorResponse(
                    Instant.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    "VALIDATION_ERROR",
                    errorMessage,
                    "",
                    null
            ));
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, null, errors, null));
    }

    /**
     * Fallback exception handler catching all unanticipated server errors to
     * prevent sensitive information leakage.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * // An unexpected NullPointerException occurs
     * // Mapped safely to 500 INTERNAL_SERVER_ERROR without exposing stack traces to the client.
     * }</pre>
     *
     * @param ex The unhandled exception that reached the controller tier.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping a generic {@link ApiErrorResponse} with a 500 Internal Server
     * Error status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleGenericException(Exception ex) {
        // System.err.println("Unhandled exception: " + ex.getMessage());
        // ex.printStackTrace(); // Print the full stack trace for debugging purposes
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "INTERNAL_SERVER_ERROR",
                        ex.getMessage(),
                        "",
                        null
                ), null));
    }
}
