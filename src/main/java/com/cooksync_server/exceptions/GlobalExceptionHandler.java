package com.cooksync_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cooksync_server.dtos.response.errors.ErrorResponse;
import com.cooksync_server.exceptions.auth.InvalidCredentialsException;
import com.cooksync_server.exceptions.auth.UserAlreadyExistsException;

/**
 * Global exception handler to intercept and process exceptions thrown across
 * the application. Translates specific runtime exceptions into standardized
 * HTTP responses.
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
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with
     * a 404 Not Found status.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Not Found", ex.getMessage()));
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
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with
     * a 403 Forbidden status.
     */
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAction(UnauthorizedActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Forbidden", ex.getMessage()));
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
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with
     * a 401 Unauthorized status.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Unauthorized", ex.getMessage()));
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
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with
     * a 409 Conflict status.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Conflict", ex.getMessage()));
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
     * @return A {@link ResponseEntity} containing a generic
     * {@link ErrorResponse} with a 500 Internal Server Error status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "An unexpected error occurred. Please try again later."));
    }
}
