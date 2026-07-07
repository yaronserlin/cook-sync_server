package com.cooksync_server.dtos.request.tags;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating or updating a recipe tag.
 * Typically restricted to administrator usage.
 * Uses Java records for an immutable data carrier.
 */
public record TagRequestDTO(
    @NotBlank(message = "Tag name is required") 
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    String name
) {}