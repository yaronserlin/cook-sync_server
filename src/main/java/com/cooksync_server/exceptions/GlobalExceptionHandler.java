package com.cooksync_server.exceptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dtos.response.ApiResponse;
import com.dtos.response.errors.ApiErrorResponse;
import com.cooksync_server.exceptions.auth.InvalidCredentialsException;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.exceptions.auth.UserAlreadyExistsException;

/**
 * Global REST exception advisor intercepting exceptions thrown across service and controller tiers.
 * Maps domain runtime exceptions into standardized HTTP ApiResponse payloads.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles ResourceNotFoundException and responds with HTTP 404 NOT_FOUND.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param ex target exception instance
     * @return response entity containing formatted error payload
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", "RESOURCE_NOT_FOUND", ex.getMessage());
    }

    /**
     * Handles ResourceAllReadyExistsException and responds with HTTP 409 CONFLICT.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param ex target exception instance
     * @return response entity containing formatted error payload
     */
    @ExceptionHandler(ResourceAllReadyExistsException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleResourceAlreadyExistsException(ResourceAllReadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", "RESOURCE_ALREADY_EXISTS", ex.getMessage());
    }

    /**
     * Handles UnauthorizedActionException and responds with HTTP 403 FORBIDDEN.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param ex target exception instance
     * @return response entity containing formatted error payload
     */
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleUnauthorizedAction(UnauthorizedActionException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", "UNAUTHORIZED_ACTION", ex.getMessage());
    }

    /**
     * Handles InvalidCredentialsException and responds with HTTP 401 UNAUTHORIZED.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param ex target exception instance
     * @return response entity containing formatted error payload
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "INVALID_CREDENTIALS", ex.getMessage());
    }

    /**
     * Handles UserAlreadyExistsException and responds with HTTP 409 CONFLICT.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param ex target exception instance
     * @return response entity containing formatted error payload
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", "USER_ALREADY_EXISTS", ex.getMessage());
    }

    /**
     * Handles payload validation failures from MethodArgumentNotValidException and responds with HTTP 400.
     *
     * Complexity:
     * Time: O(V) where V is total count of validation field errors
     * Space: O(V)
     *
     * @param ex target validation exception
     * @return response entity with validation errors list
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
     * Handles database constraint and integrity violations and responds with HTTP 409 CONFLICT.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param ex target data integrity exception
     * @return response entity with conflict error payload
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        LOG.warn("Database data integrity violation: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", "DATA_INTEGRITY_VIOLATION", "Database constraint or duplicate entry violation");
    }

    /**
     * Handles malformed request JSON bodies and responds with HTTP 400 BAD_REQUEST.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param ex message parsing exception
     * @return response entity with bad request payload
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        LOG.warn("Malformed HTTP message payload: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", "MALFORMED_JSON_PAYLOAD", "Malformed request payload format");
    }

    /**
     * Fallback exception handler catching uncaught exceptions and returning HTTP 500.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param ex unhandled exception instance
     * @return response entity containing generic error payload
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ApiErrorResponse>> handleGenericException(Exception ex) {
        LOG.error("Unhandled exception reached GlobalExceptionHandler", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "INTERNAL_SERVER_ERROR", ex.getMessage());
    }

    private ResponseEntity<ApiResponse<ApiErrorResponse>> buildErrorResponse(
            HttpStatus status, String error, String errorCode, String message) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, null, new ApiErrorResponse(
                        Instant.now(),
                        status.value(),
                        error,
                        errorCode,
                        message,
                        "",
                        null
                ), null));
    }
}
