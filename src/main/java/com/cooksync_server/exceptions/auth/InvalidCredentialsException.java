package com.cooksync_server.exceptions.auth;

/**
 * Custom runtime exception thrown when user authentication fails due to
 * incorrect or invalid credentials.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
public class InvalidCredentialsException extends RuntimeException {

    /**
     * Constructs a new InvalidCredentialsException with the specified detail
     * message.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * throw new InvalidCredentialsException("Account locked due to too many failed attempts");
     * }</pre>
     *
     * @param message The detail message explaining the specific reason for the
     * authentication failure.
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidCredentialsException with a default error message
     * ("Invalid email or password").
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * if (!isPasswordMatch) {
     * throw new InvalidCredentialsException();
     * }
     * }</pre>
     */
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
