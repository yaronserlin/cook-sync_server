package com.cooksync_server.dtos.response;

import java.util.List;
import java.util.Set;

import com.cooksync_server.dtos.response.ingredient.IngredientResponse;
import com.cooksync_server.entities.Instruction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructionResponse {

    private String id;
    private int stepNumber;
    private String description;
    private Boolean HasTimer;
    private Integer timeSeconds;
    private String createdAt;
    private String updatedAt;

    private Set<IngredientResponse> ingredients;

    public static List<InstructionResponse> fromEntities(List<Instruction> instructions) {
        return instructions.stream()
                .map(instruction -> InstructionResponse.builder()
                .id(instruction.getId())
                .stepNumber(instruction.getStepNumber())
                .description(instruction.getDescription())
                .HasTimer(instruction.isHasTimer())
                .timeSeconds(instruction.getTimeSeconds())
                .createdAt(instruction.getCreatedAt().toString())
                .updatedAt(instruction.getUpdatedAt().toString())
                .ingredients(IngredientResponse.fromEntities(instruction.getIngredients()))
                .build())
                .toList();
    }
}
