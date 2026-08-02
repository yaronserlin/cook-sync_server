package com.cooksync_server.mappers;

import com.cooksync_server.entities.Instruction;
import com.dtos.response.ingredient.IngredientResponse;
import com.dtos.response.instruction.InstructionResponse;
import java.util.Set;
import java.util.stream.Collectors;

public final class InstructionMapper {

    private InstructionMapper() {
    }

    public static InstructionResponse toResponse(Instruction instruction) {
        if (instruction == null) {
            return null;
        }
        Set<IngredientResponse> ingredients = instruction.getIngredients() == null ? null
                : instruction.getIngredients().stream().map(IngredientMapper::toResponse).collect(Collectors.toSet());
        String created = MapperUtils.toIsoStringOrNull(instruction.getCreatedAt());
        String updated = MapperUtils.toIsoStringOrNull(instruction.getUpdatedAt());
        return new InstructionResponse(instruction.getId(), instruction.getStepNumber(), instruction.getDescription(),
                instruction.isHasTimer(), instruction.getTimeSeconds(), created, updated, ingredients,
                instruction.getImageUrl());
    }
}
