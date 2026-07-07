package com.cooksync_server.dtos.request.unit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for handling unit creation requests.
 * Encapsulates the required data payload and validation constraints for defining a new measurement unit.
 * Uses Java records for an immutable data carrier.
 */
public record UnitRequestDTO(
    @NotBlank(message = "Unit name cannot be blank")
    @Size(min = 2, max = 50, message = "Unit name must be between 2 and 50 characters")
    String name,

    @NotBlank(message = "Unit code cannot be blank")
    @Size(max = 10, message = "Unit code must be at most 10 characters")
    String code
) {}