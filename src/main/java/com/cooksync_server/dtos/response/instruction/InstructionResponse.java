package com.cooksync_server.dtos.response.instruction;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cooksync_server.dtos.response.ingredient.IngredientResponse;
import com.cooksync_server.entities.Instruction;

/**
 * Data Transfer Object for a cooking instruction step response.
 */
public record InstructionResponse(
    String id,
    int stepNumber,
    String description,
    Boolean hasTimer,
    Integer timeSeconds,
    String createdAt,
    String updatedAt,
    Set<IngredientResponse> ingredients
) {
    /**
     * Maps a persistent Instruction entity to an InstructionResponse DTO.
     */
    public static InstructionResponse fromEntity(Instruction instruction) {
        return new InstructionResponse(
            instruction.getId(),
            instruction.getStepNumber(),
            instruction.getDescription(),
            instruction.isHasTimer(),
            instruction.getTimeSeconds(),
            instruction.getCreatedAt() != null ? instruction.getCreatedAt().toString() : null,
            instruction.getUpdatedAt() != null ? instruction.getUpdatedAt().toString() : null,
            IngredientResponse.fromEntities(instruction.getIngredients())
        );
    }

    /**
     * Maps a collection of Instruction entities to a List of InstructionResponse DTOs.
     */
    public static List<InstructionResponse> fromEntities(Collection<Instruction> instructions) {
        if (instructions == null) return List.of();
        
        return instructions.stream()
                .map(InstructionResponse::fromEntity)
                .collect(Collectors.toList());
    }
}