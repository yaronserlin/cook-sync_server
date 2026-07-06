package com.cooksync_server.exceptions;

/**
 * Custom runtime exception thrown when a requested resource cannot be located
 * in the system.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with a dynamically formatted
     * detail message.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * throw new ResourceNotFoundException("Recipe", "uuid-1234");
     * // Results in message: "Recipe not found: uuid-1234"
     * }</pre>
     *
     * @param resourceName The type or name of the missing resource (e.g.,
     * "User", "Recipe", "Ingredient").
     * @param resourceId The unique identifier that was used in the failed
     * lookup attempt.
     */
    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(String.format("%s not found: %s", resourceName, resourceId));
    }
}
