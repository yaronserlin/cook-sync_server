package com.cooksync_server.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.request.ingredient.IngredientRequestDTO;
import com.dtos.request.instruction.InstructionRequestDTO;
import com.dtos.request.recipe.RecipeCreateRequestDTO;
import com.dtos.response.recipe.RecipeResponse;
import com.dtos.response.recipe.RecipePreviewResponse;
import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.entities.Instruction;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.RecipeImage;
import com.cooksync_server.entities.Tag;
import com.cooksync_server.entities.Unit;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.repositories.IngredientRepository;
import com.cooksync_server.repositories.InstructionRepository;
import com.cooksync_server.repositories.RecipeImageRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.TagRepository;
import com.cooksync_server.repositories.UnitRepository;
import com.cooksync_server.repositories.UserRepository;
import com.cooksync_server.mappers.RecipeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final InstructionRepository instructionRepository;
    private final RecipeImageRepository recipeImageRepository;
    private final TagRepository tagRepository;
    private final UnitRepository unitRepository;

    public List<RecipePreviewResponse> getAllRecipes() {
        return recipeRepository.findAll().stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
    }

    public RecipeResponse getRecipeById(String id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", id));
        return RecipeMapper.toResponse(recipe);
    }

    public List<RecipePreviewResponse> searchRecipes(String keyword) {
        return recipeRepository.findByTitleContainingIgnoreCase(keyword).stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
    }

    public List<RecipePreviewResponse> findRecipesByTag(String tagName) {
        return recipeRepository.findByTagName(tagName).stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
    }

    @Transactional
    public RecipeResponse createRecipe(RecipeCreateRequestDTO request, String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        List<Tag> tags = fetchTags(request.tagIds());

        Recipe recipe = Recipe.builder()
                .createdBy(creator)
                .title(request.title())
                .description(request.description())
                .difficulty(Recipe.Difficulty.valueOf(request.difficulty().toUpperCase()))
                .prepTimeMinutes(request.prepTimeMinutes())
                .cookTimeMinutes(request.cookTimeMinutes())
                .servings(request.servings())
                .reviewCount(0)
                .tags(tags)
                .build();

        Recipe savedRecipe = recipeRepository.save(recipe);

        savedRecipe.setIngredients(saveIngredients(request.ingredients(), savedRecipe));
        savedRecipe.setInstructions(saveInstructions(request.instructions(), savedRecipe));
        saveImages(savedRecipe, request.primaryImageUrl(), request.additionalImageUrls());

        return RecipeMapper.toResponse(savedRecipe);
    }

    @Transactional
    public RecipeResponse updateRecipe(String recipeId, RecipeCreateRequestDTO request, String userEmail) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (!recipe.getCreatedBy().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new UnauthorizedActionException("You are not allowed to edit this recipe.");
        }

        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setDifficulty(Recipe.Difficulty.valueOf(request.difficulty().toUpperCase()));
        recipe.setPrepTimeMinutes(request.prepTimeMinutes());
        recipe.setCookTimeMinutes(request.cookTimeMinutes());
        recipe.setServings(request.servings());
        recipe.setTags(fetchTags(request.tagIds()));


        recipe.getIngredients().clear();
        recipe.getIngredients().addAll(saveIngredients(request.ingredients(), recipe));

        recipe.getInstructions().clear();
        recipe.getInstructions().addAll(saveInstructions(request.instructions(), recipe));
        updateImages(recipe, request.primaryImageUrl());

        return RecipeMapper.toResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public void deleteRecipe(String recipeId, String userEmail) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (!recipe.getCreatedBy().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new UnauthorizedActionException("You are not allowed to delete this recipe.");
        }

        recipeRepository.delete(recipe);
    }

    private void saveImages(Recipe recipe, String primaryImageUrl, List<String> additionalImageUrls) {
        recipe.getImages().clear();
        if (primaryImageUrl != null && !primaryImageUrl.isBlank()) {
            recipe.getImages().add(RecipeImage.builder()
                    .recipe(recipe)
                    .imageUrl(primaryImageUrl)
                    .isPrimary(true)
                    .build());
        }

        if (additionalImageUrls != null) {
            for (String imageUrl : additionalImageUrls) {
                if (imageUrl != null && !imageUrl.isBlank()) {
                    recipe.getImages().add(RecipeImage.builder()
                            .recipe(recipe)
                            .imageUrl(imageUrl)
                            .isPrimary(false)
                            .build());
                }
            }
        }
    }

    private void updateImages(Recipe recipe, String primaryImageUrl) {
        recipe.getImages().clear();
        if (primaryImageUrl != null && !primaryImageUrl.isBlank()) {
            recipe.getImages().add(RecipeImage.builder()
                    .recipe(recipe)
                    .imageUrl(primaryImageUrl)
                    .isPrimary(true)
                    .build());
        }
    }

    private List<Tag> fetchTags(List<String> tagIds) {
        List<Tag> tags = new ArrayList<>();
        if (tagIds != null) {
            for (String tagId : tagIds) {
                tags.add(tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId)));
            }
        }
        return tags;
    }

    private Set<Ingredient> saveIngredients(List<IngredientRequestDTO> dtoList, Recipe recipe) {
        Set<Ingredient> ingredients = new HashSet<>();
        for (IngredientRequestDTO ingDto : dtoList) {
            Unit unit = unitRepository.findById(ingDto.unitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit", ingDto.unitId()));
            Ingredient ingredient = Ingredient.builder()
                    .recipe(recipe)
                    .name(ingDto.name())
                    .quantity(BigDecimal.valueOf(ingDto.quantity()))
                    .unit(unit)
                    .build();
            ingredients.add(ingredient); // שמירה דרך CascadeType.ALL
        }
        return ingredients;
    }

    private List<Instruction> saveInstructions(List<InstructionRequestDTO> dtoList, Recipe recipe) {
        List<Instruction> instructions = new ArrayList<>();
        for (InstructionRequestDTO instDto : dtoList) {
            Instruction instruction = Instruction.builder()
                    .recipe(recipe)
                    .stepNumber(instDto.stepNumber())
                    .description(instDto.description())
                    .imageUrl(instDto.imageUrl())
                    .hasTimer(instDto.hasTimer())
                    .timeSeconds(instDto.timeSeconds())
                    .build();
            instructions.add(instruction); // שמירה דרך CascadeType.ALL
        }
        return instructions;
    }
}
