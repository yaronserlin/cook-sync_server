package com.cooksync_server.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.request.instruction.InstructionRequestDTO;
import com.dtos.response.instruction.InstructionResponse;
import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.entities.Instruction;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.mappers.InstructionMapper;
import com.cooksync_server.repositories.IngredientRepository;
import com.cooksync_server.repositories.InstructionRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructionService {

    private final InstructionRepository instructionRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional
    public InstructionResponse addInstructionToRecipe(String recipeId, InstructionRequestDTO request, String userEmail) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        // Only the recipe's creator or an admin may change its steps.
        OwnershipValidator.requireOwnerOrAdmin(recipe.getCreatedBy().getId(), user,
                "You are not allowed to modify this recipe.");

        Instruction instruction = Instruction.builder()
                .recipe(recipe)
                .stepNumber(request.stepNumber())
                .description(request.description())
                .hasTimer(request.hasTimer())
                .timeSeconds(request.timeSeconds())
                .imageUrl(request.imageUrl())
                .ingredients(resolveIngredients(request.ingredientIds()))
                .build();

        return InstructionMapper.toResponse(instructionRepository.save(instruction));
    }

    @Transactional
    public InstructionResponse updateInstruction(String instructionId, InstructionRequestDTO request, String userEmail) {
        Instruction instruction = instructionRepository.findById(instructionId)
                .orElseThrow(() -> new ResourceNotFoundException("Instruction", instructionId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        OwnershipValidator.requireOwnerOrAdmin(instruction.getRecipe().getCreatedBy().getId(), user,
                "You are not allowed to modify this instruction.");

        instruction.setStepNumber(request.stepNumber());
        instruction.setDescription(request.description());
        instruction.setHasTimer(request.hasTimer());
        instruction.setTimeSeconds(request.timeSeconds());
        instruction.setImageUrl(request.imageUrl());
        instruction.setIngredients(resolveIngredients(request.ingredientIds()));

        return InstructionMapper.toResponse(instructionRepository.save(instruction));
    }

    @Transactional
    public void deleteInstruction(String instructionId, String userEmail) {
        Instruction instruction = instructionRepository.findById(instructionId)
                .orElseThrow(() -> new ResourceNotFoundException("Instruction", instructionId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        OwnershipValidator.requireOwnerOrAdmin(instruction.getRecipe().getCreatedBy().getId(), user,
                "You are not allowed to delete this instruction.");

        instructionRepository.delete(instruction);
    }

    private Set<Ingredient> resolveIngredients(List<UUID> ingredientIds) {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            return new HashSet<>();
        }
        List<String> ids = ingredientIds.stream().map(UUID::toString).collect(Collectors.toList());
        return new HashSet<>(ingredientRepository.findAllById(ids));
    }
}
