package com.cooksync_server.exceptions;

public class ResourceAllReadyExistsException extends RuntimeException {

    public ResourceAllReadyExistsException(String resourceName, String resourceId) {
        super(String.format("%s already exists: %s", resourceName, resourceId));
    }
}
