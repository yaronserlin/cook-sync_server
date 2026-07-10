package com.cooksync_server.mappers;

import com.cooksync_server.entities.Instruction;
import com.dtos.response.instruction.InstructionResponse;
import java.util.Set;
import java.util.stream.Collectors;

public final class InstructionMapper {

    private InstructionMapper() {
    }

    public static InstructionResponse toResponse(Instruction ins) {
        if (ins == null) {
            return null;
        }
        Set<com.dtos.response.ingredient.IngredientResponse> ingr = ins.getIngredients() == null ? null : ins.getIngredients().stream().map(IngredientMapper::toResponse).collect(Collectors.toSet());
        String created = ins.getCreatedAt() == null ? null : ins.getCreatedAt().toString();
        String updated = ins.getUpdatedAt() == null ? null : ins.getUpdatedAt().toString();
        return new InstructionResponse(ins.getId(), ins.getStepNumber(), ins.getDescription(), ins.isHasTimer(), ins.getTimeSeconds(), created, updated, ingr);
    }
}
