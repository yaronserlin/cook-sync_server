package com.cooksync_server.dtos.request.unit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for handling unit creation requests. Encapsulates
 * the required data payload and validation constraints for defining a new
 * measurement unit in the system.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Data
public class CreateUnitRequest {

    /**
     * The full name of the measurement unit (e.g., "Kilogram", "Teaspoon").
     * Must not be blank and is restricted to a length between 2 and 50
     * characters.
     */
    @NotBlank(message = "Unit name cannot be blank")
    @Size(min = 2, max = 50, message = "Unit name must be between 2 and 50 characters")
    private String name;

    /**
     * The abbreviated code or symbol representing the measurement unit (e.g.,
     * "kg", "tsp"). Must not be blank and is restricted to a maximum length of
     * 10 characters.
     */
    @NotBlank(message = "Unit code cannot be blank")
    @Size(max = 10, message = "Unit code must be at most 10 characters")
    private String code;
}
