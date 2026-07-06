package com.cooksync_server.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooksync_server.dtos.request.CreateRecipeRequest;
import com.cooksync_server.dtos.request.IngredientDto;
import com.cooksync_server.dtos.request.InstructionDto;
import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.entities.Instruction;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.Tag;
import com.cooksync_server.entities.Unit;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.repositories.IngredientRepository;
import com.cooksync_server.repositories.InstructionRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.TagRepository;
import com.cooksync_server.repositories.UnitRepository;
import com.cooksync_server.repositories.UserRepository;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final InstructionRepository instructionRepository;
    private final TagRepository tagRepository;
    private final UnitRepository unitRepository;

    public RecipeService(
            RecipeRepository recipeRepository,
            UserRepository userRepository,
            IngredientRepository ingredientRepository,
            InstructionRepository instructionRepository,
            TagRepository tagRepository,
            UnitRepository unitRepository
    ) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.ingredientRepository = ingredientRepository;
        this.instructionRepository = instructionRepository;
        this.tagRepository = tagRepository;
        this.unitRepository = unitRepository;
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Recipe getRecipeById(String id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", id));
    }

    public List<Recipe> searchRecipes(String keyword) {
        return recipeRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public List<Recipe> findRecipesByTag(String tagName) {
        return recipeRepository.findByTagName(tagName);
    }

    @Transactional
    public Recipe createRecipe(CreateRecipeRequest request, String userEmail) {
        User creator = getUserByEmail(userEmail);
        List<Tag> tags = resolveTags(request.getTagNames());

        Recipe recipe = Recipe.builder()
                .createdBy(creator)
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .prepTimeMinutes(request.getPrepTimeMinutes())
                .cookTimeMinutes(request.getCookTimeMinutes())
                .servings(request.getServings())
                .reviewCount(0)
                .tags(tags)
                .build();

        Recipe savedRecipe = recipeRepository.save(recipe);
        Set<Ingredient> savedIngredients = (Set<Ingredient>) ingredientRepository.saveAll(buildIngredients(savedRecipe, request.getIngredients()));
        List<Instruction> savedInstructions = instructionRepository.saveAll(buildInstructions(savedRecipe, request.getInstructions()));

        savedRecipe.setIngredients(savedIngredients);
        savedRecipe.setInstructions(savedInstructions);

        return recipeRepository.save(savedRecipe);
    }

    @Transactional
    public Recipe updateRecipe(String recipeId, CreateRecipeRequest request, String userEmail) {
        Recipe existingRecipe = getRecipeById(recipeId);
        User currentUser = getUserByEmail(userEmail);

        if (!canModifyRecipe(existingRecipe, currentUser)) {
            throw new UnauthorizedActionException("You are not allowed to update this recipe.");
        }

        existingRecipe.setTitle(request.getTitle());
        existingRecipe.setDescription(request.getDescription());
        existingRecipe.setDifficulty(request.getDifficulty());
        existingRecipe.setPrepTimeMinutes(request.getPrepTimeMinutes());
        existingRecipe.setCookTimeMinutes(request.getCookTimeMinutes());
        existingRecipe.setServings(request.getServings());
        existingRecipe.setTags(resolveTags(request.getTagNames()));

        if (request.getIngredients() != null) {
            existingRecipe.getIngredients().clear();
            existingRecipe.getIngredients().addAll(buildIngredients(existingRecipe, request.getIngredients()));
        }

        if (request.getInstructions() != null) {
            existingRecipe.getInstructions().clear();
            existingRecipe.getInstructions().addAll(buildInstructions(existingRecipe, request.getInstructions()));
        }

        return recipeRepository.save(existingRecipe);
    }

    @Transactional
    public void deleteRecipe(String recipeId, String userEmail) {
        Recipe recipe = getRecipeById(recipeId);
        User currentUser = getUserByEmail(userEmail);

        if (!canModifyRecipe(recipe, currentUser)) {
            throw new UnauthorizedActionException("You are not allowed to delete this recipe.");
        }

        recipeRepository.delete(recipe);
    }

    private boolean canModifyRecipe(Recipe recipe, User user) {
        return recipe.getCreatedBy().getId().equals(user.getId()) || user.isAdmin();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private List<Tag> resolveTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        return tagNames.stream()
                .map(this::findOrCreateTag)
                .collect(Collectors.toList());
    }

    private Tag findOrCreateTag(String tagName) {
        if (tagName == null || tagName.isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be blank.");
        }

        return tagRepository.findByNameIgnoreCase(tagName.trim())
                .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName.trim()).build()));
    }

    private List<Ingredient> buildIngredients(Recipe recipe, List<IngredientDto> ingredients) {
        if (ingredients == null) {
            return new ArrayList<>();
        }

        return ingredients.stream().map(dto -> {
            Unit unit = unitRepository.findByCode(dto.getUnitCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit", dto.getUnitCode()));
            return Ingredient.builder()
                    .recipe(recipe)
                    .name(dto.getName())
                    .quantity(dto.getQuantity())
                    .unit(unit)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<Instruction> buildInstructions(Recipe recipe, List<InstructionDto> instructions) {
        if (instructions == null) {
            return new ArrayList<>();
        }

        return instructions.stream().map(dto -> Instruction.builder()
                .recipe(recipe)
                .stepNumber(dto.getStepNumber())
                .description(dto.getDescription())
                .hasTimer(dto.isHasTimer())
                .timeSeconds(dto.getTimeSeconds())
                .build())
                .collect(Collectors.toList());
    }
}
