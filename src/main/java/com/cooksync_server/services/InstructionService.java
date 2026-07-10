package com.cooksync_server.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.request.instruction.InstructionRequestDTO;
import com.dtos.response.instruction.InstructionResponse;
import com.cooksync_server.entities.Instruction;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.repositories.InstructionRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstructionService {

    private final InstructionRepository instructionRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Transactional
    public InstructionResponse addInstructionToRecipe(String recipeId, InstructionRequestDTO request, String userEmail) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        // אימות הרשאות: רק יוצר המתכון או מנהל יכולים לשנות שלבים
        if (!recipe.getCreatedBy().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new UnauthorizedActionException("You are not allowed to modify this recipe.");
        }

        Instruction instruction = Instruction.builder()
                .recipe(recipe)
                .stepNumber(request.stepNumber())
                .description(request.description())
                .hasTimer(request.hasTimer())
                .timeSeconds(request.timeSeconds())
                .build();

        return com.cooksync_server.mappers.InstructionMapper.toResponse(instructionRepository.save(instruction));
    }

    @Transactional
    public InstructionResponse updateInstruction(String instructionId, InstructionRequestDTO request, String userEmail) {
        Instruction instruction = instructionRepository.findById(instructionId)
                .orElseThrow(() -> new ResourceNotFoundException("Instruction", instructionId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (!instruction.getRecipe().getCreatedBy().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new UnauthorizedActionException("You are not allowed to modify this instruction.");
        }

        instruction.setStepNumber(request.stepNumber());
        instruction.setDescription(request.description());
        instruction.setHasTimer(request.hasTimer());
        instruction.setTimeSeconds(request.timeSeconds());

        return com.cooksync_server.mappers.InstructionMapper.toResponse(instructionRepository.save(instruction));
    }

    @Transactional
    public void deleteInstruction(String instructionId, String userEmail) {
        Instruction instruction = instructionRepository.findById(instructionId)
                .orElseThrow(() -> new ResourceNotFoundException("Instruction", instructionId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (!instruction.getRecipe().getCreatedBy().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new UnauthorizedActionException("You are not allowed to delete this instruction.");
        }

        instructionRepository.delete(instruction);
    }
}
