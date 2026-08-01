package com.cooksync_server.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.request.ingredient.IngredientRequestDTO;
import com.dtos.request.instruction.InstructionRequestDTO;
import com.dtos.request.recipe.RecipeCreateRequestDTO;
import com.dtos.response.PagedResponse;
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
        return recipeRepository.findByVisibility(Recipe.Visibility.PUBLIC).stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
    }

    /** Paged variant used by the Home feed's infinite scroll; the unpaged {@link #getAllRecipes()} above is unchanged for Search/Filters. */
    public PagedResponse<RecipePreviewResponse> getAllRecipesPaged(int page, int size) {
        Page<Recipe> result = recipeRepository.findByVisibility(Recipe.Visibility.PUBLIC,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<RecipePreviewResponse> content = result.getContent().stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
        return new PagedResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast());
    }

    public RecipeResponse getRecipeById(String id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", id));
        return RecipeMapper.toResponse(recipe);
    }

    /** Advanced search: title/author/ingredient are each optional and ANDed together when present. */
    public List<RecipePreviewResponse> searchRecipes(String keyword, String author, String ingredient) {
        String title = (keyword == null || keyword.isBlank()) ? null : keyword;
        String authorFilter = (author == null || author.isBlank()) ? null : author;
        String ingredientFilter = (ingredient == null || ingredient.isBlank()) ? null : ingredient;
        return recipeRepository.searchRecipesAdvanced(title, authorFilter, ingredientFilter, Recipe.Visibility.PUBLIC)
                .stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
    }

    public List<RecipePreviewResponse> findRecipesByTag(String tagName) {
        return recipeRepository.findByTagNameAndVisibility(tagName, Recipe.Visibility.PUBLIC)
                .stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
    }

    public List<RecipePreviewResponse> getMyRecipes(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        return recipeRepository.findByCreatedById(user.getId()).stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
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
                .visibility(parseVisibility(request.visibility()))
                .prepTimeMinutes(request.prepTimeMinutes())
                .cookTimeMinutes(request.cookTimeMinutes())
                .servings(request.servings())
                .reviewCount(0)
                .tags(tags)
                .build();

        Recipe savedRecipe = recipeRepository.save(recipe);

        Map<String, Ingredient> tmpIdToIngredient = new HashMap<>();
        savedRecipe.setIngredients(saveIngredients(request.ingredients(), savedRecipe, tmpIdToIngredient));
        savedRecipe.setInstructions(saveInstructions(request.instructions(), savedRecipe, tmpIdToIngredient));
        saveImages(savedRecipe, request.primaryImageUrl(), request.additionalImageUrls());

        return RecipeMapper.toResponse(savedRecipe);
    }

    @Transactional
    public RecipeResponse updateRecipe(String recipeId, RecipeCreateRequestDTO request, String userEmail) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        OwnershipValidator.requireOwnerOrAdmin(recipe.getCreatedBy().getId(), currentUser,
                "You are not allowed to edit this recipe.");

        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setDifficulty(Recipe.Difficulty.valueOf(request.difficulty().toUpperCase()));
        recipe.setVisibility(parseVisibility(request.visibility()));
        recipe.setPrepTimeMinutes(request.prepTimeMinutes());
        recipe.setCookTimeMinutes(request.cookTimeMinutes());
        recipe.setServings(request.servings());
        recipe.setTags(fetchTags(request.tagIds()));


        Map<String, Ingredient> tmpIdToIngredient = new HashMap<>();
        recipe.getIngredients().clear();
        recipe.getIngredients().addAll(saveIngredients(request.ingredients(), recipe, tmpIdToIngredient));

        recipe.getInstructions().clear();
        recipe.getInstructions().addAll(saveInstructions(request.instructions(), recipe, tmpIdToIngredient));
        saveImages(recipe, request.primaryImageUrl(), request.additionalImageUrls());

        return RecipeMapper.toResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public void deleteRecipe(String recipeId, String userEmail) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        OwnershipValidator.requireOwnerOrAdmin(recipe.getCreatedBy().getId(), currentUser,
                "You are not allowed to delete this recipe.");

        recipeRepository.delete(recipe);
    }

    private Recipe.Visibility parseVisibility(String visibility) {
        if (visibility == null || visibility.isBlank()) {
            return Recipe.Visibility.PUBLIC;
        }
        return Recipe.Visibility.valueOf(visibility.toUpperCase());
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

    private Set<Ingredient> saveIngredients(List<IngredientRequestDTO> dtoList, Recipe recipe,
            Map<String, Ingredient> tmpIdToIngredient) {
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
            ingredients.add(ingredient); // Persisted via the recipe's CascadeType.ALL, no explicit save needed here.
            if (ingDto.tmpId() != null) {
                tmpIdToIngredient.put(ingDto.tmpId(), ingredient);
            }
        }
        return ingredients;
    }

    private List<Instruction> saveInstructions(List<InstructionRequestDTO> dtoList, Recipe recipe,
            Map<String, Ingredient> tmpIdToIngredient) {
        List<Instruction> instructions = new ArrayList<>();
        for (InstructionRequestDTO instDto : dtoList) {
            Set<Ingredient> stepIngredients = new HashSet<>();
            if (instDto.ingredientIds() != null) {
                for (UUID ingredientId : instDto.ingredientIds()) {
                    Ingredient ingredient = tmpIdToIngredient.get(ingredientId.toString());
                    if (ingredient != null) {
                        stepIngredients.add(ingredient);
                    }
                }
            }
            Instruction instruction = Instruction.builder()
                    .recipe(recipe)
                    .stepNumber(instDto.stepNumber())
                    .description(instDto.description())
                    .imageUrl(instDto.imageUrl())
                    .hasTimer(instDto.hasTimer())
                    .timeSeconds(instDto.timeSeconds())
                    .ingredients(stepIngredients)
                    .build();
            instructions.add(instruction); // Persisted via the recipe's CascadeType.ALL, no explicit save needed here.
        }
        return instructions;
    }
}
