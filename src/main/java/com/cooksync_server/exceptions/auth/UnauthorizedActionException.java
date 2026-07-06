package com.cooksync_server.exceptions.auth;

/**
 * Custom runtime exception thrown when a user attempts to perform an action
 * without sufficient permissions or authorization.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
public class UnauthorizedActionException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedActionException with the specified detail
     * message.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * throw new UnauthorizedActionException("You do not have permission to delete this recipe");
     * }</pre>
     *
     * @param message The detail message explaining the specific reason for the
     * authorization failure.
     */
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
