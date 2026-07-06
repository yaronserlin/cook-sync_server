package com.cooksync_server.exceptions.auth;

/**
 * Custom runtime exception thrown when attempting to register a user with an
 * email or identifier that already exists in the system.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new UserAlreadyExistsException with the specified detail
     * message.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * throw new UserAlreadyExistsException("Email is already registered");
     * }</pre>
     *
     * @param message The detail message explaining the specific reason for the
     * exception.
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
