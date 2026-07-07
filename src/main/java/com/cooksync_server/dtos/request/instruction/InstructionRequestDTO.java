package com.cooksync_server.dtos.request.instruction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for creating or updating a recipe instruction step.
 * Uses Java records for an immutable data carrier.
 */
public record InstructionRequestDTO(
    @Positive(message = "Step number must be positive")
    int stepNumber,
    
    @NotBlank(message = "Description is required") 
    String description,
    
    boolean hasTimer,
    
    Integer timeSeconds,
    
    List<UUID> ingredientIds // מזהי מצרכים המשויכים לשלב זה
) {}