package com.cooksync_server.dtos.request.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Data Transfer Object for creating or updating a personal note on a recipe or specific instruction.
 */
public record NoteRequestDTO(
    @NotNull(message = "Recipe ID is required")
    UUID recipeId,
    
    UUID instructionId, // Optional: if null, the note is for the whole recipe
    
    @NotBlank(message = "Note content cannot be empty")
    String note
) {}