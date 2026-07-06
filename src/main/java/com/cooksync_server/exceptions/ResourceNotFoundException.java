package com.cooksync_server.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(String.format("%s not found: %s", resourceName, resourceId));
    }
}
