package com.cooksync_server.mappers;

import com.cooksync_server.entities.Instruction;
import com.dtos.response.ingredient.IngredientResponse;
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
        Set<IngredientResponse> ingr = ins.getIngredients() == null ? null
                : ins.getIngredients().stream().map(IngredientMapper::toResponse).collect(Collectors.toSet());
        String created = MapperUtils.toIsoStringOrNull(ins.getCreatedAt());
        String updated = MapperUtils.toIsoStringOrNull(ins.getUpdatedAt());
        return new InstructionResponse(ins.getId(), ins.getStepNumber(), ins.getDescription(), ins.isHasTimer(),
                ins.getTimeSeconds(), created, updated, ingr, ins.getImageUrl());
    }
}
