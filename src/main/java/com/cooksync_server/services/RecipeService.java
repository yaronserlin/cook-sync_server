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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.request.ingredient.IngredientRequestDTO;
import com.dtos.request.instruction.InstructionRequestDTO;
import com.dtos.request.recipe.RecipeCreateRequestDTO;
import com.dtos.request.recipe.RecipeVisibilityUpdateRequestDTO;
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
import com.cooksync_server.repositories.RecipeSpecifications;
import com.cooksync_server.repositories.TagRepository;
import com.cooksync_server.repositories.UnitRepository;
import com.cooksync_server.repositories.UserRepository;
import com.cooksync_server.mappers.RecipeMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class handling core recipe management business logic including catalog listing, search, creation, updates, and deletion.
 * Enforces transactional read-only boundaries and structured SLF4J logging for monitoring.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Slf4j
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

    /**
     * Retrieves all public recipes for general catalog display.
     *
     * Complexity:
     * Time: O(N) where N is total public recipe count
     * Space: O(N)
     *
     * @return list of RecipePreviewResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<RecipePreviewResponse> getAllRecipes() {
        log.debug("Fetching all public recipes");
        return recipeRepository.findByVisibility(Recipe.Visibility.PUBLIC).stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
    }

    /**
     * Retrieves paginated slice of public recipes for feed infinite scrolling.
     *
     * Complexity:
     * Time: O(S) where S is page size limit
     * Space: O(S)
     *
     * @param page page index
     * @param size page size limit
     * @return PagedResponse containing RecipePreviewResponse DTOs
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipePreviewResponse> getAllRecipesPaged(int page, int size) {
        log.debug("Fetching paginated public recipes. Page: {}, Size: {}", page, size);
        Page<Recipe> result = recipeRepository.findByVisibility(Recipe.Visibility.PUBLIC,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<RecipePreviewResponse> content = result.getContent().stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
        return new PagedResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast());
    }

    /**
     * Retrieves full detail view of a single recipe by ID using optimized fetch join.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target recipe ID
     * @return RecipeResponse DTO
     */
    @Transactional(readOnly = true)
    public RecipeResponse getRecipeById(String id) {
        log.debug("Fetching detailed recipe by ID: {}", id);
        Recipe recipe = recipeRepository.findByIdWithDetails(id)
                .orElseGet(() -> recipeRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Recipe", id)));
        return RecipeMapper.toResponse(recipe);
    }

    /**
     * Unified multi-token search filtering by keyword, author, and ingredient criteria.
     *
     * Complexity:
     * Time: O(M) where M is matching recipe count
     * Space: O(M)
     *
     * @param keyword search keyword
     * @param author author name filter
     * @param ingredient ingredient filter
     * @return list of RecipePreviewResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<RecipePreviewResponse> searchRecipes(String keyword, String author, String ingredient) {
        log.debug("Executing recipe search. Keyword: {}, Author: {}, Ingredient: {}", keyword, author, ingredient);
        Specification<Recipe> spec = RecipeSpecifications.combine(
                RecipeSpecifications.isPublicAndEnabled(),
                RecipeSpecifications.matchesUnifiedQuery(keyword),
                RecipeSpecifications.hasAuthor(author),
                RecipeSpecifications.hasIngredient(ingredient));
        return recipeRepository.findAll(spec)
                .stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
    }

    /**
     * Retrieves public recipes tagged with specified tag name.
     *
     * Complexity:
     * Time: O(T) where T is matching recipe count
     * Space: O(T)
     *
     * @param tagName target tag label name
     * @return list of RecipePreviewResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<RecipePreviewResponse> findRecipesByTag(String tagName) {
        log.debug("Fetching recipes by tag name: {}", tagName);
        return recipeRepository.findByTagNameAndVisibility(tagName, Recipe.Visibility.PUBLIC)
                .stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
    }

    /**
     * Retrieves all recipes authored by the authenticated user.
     *
     * Complexity:
     * Time: O(U) where U is user authored recipe count
     * Space: O(U)
     *
     * @param userEmail user email address
     * @return list of RecipePreviewResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<RecipePreviewResponse> getMyRecipes(String userEmail) {
        log.debug("Fetching recipes for user email: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        return recipeRepository.findByCreatedById(user.getId()).stream().map(RecipeMapper::toPreview).collect(Collectors.toList());
    }

    /**
     * Creates a new recipe with nested ingredients, instructions, tags, and images.
     *
     * Complexity:
     * Time: O(I + S + T) where I=ingredients, S=instructions, T=tags
     * Space: O(I + S + T)
     *
     * @param request recipe creation request DTO
     * @param userEmail creator user email address
     * @return created RecipeResponse DTO
     */
    @Transactional
    public RecipeResponse createRecipe(RecipeCreateRequestDTO request, String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        Set<Tag> tags = fetchTags(request.tagIds());

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

    /**
     * Updates existing recipe attributes, ingredients, instructions, tags, and images.
     *
     * Complexity:
     * Time: O(I + S + T) where I=ingredients, S=instructions, T=tags
     * Space: O(I + S + T)
     *
     * @param recipeId target recipe ID
     * @param request recipe update request DTO
     * @param userEmail user email address
     * @return updated RecipeResponse DTO
     */
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

    /**
     * Updates only a recipe's visibility, without touching its other fields.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param recipeId target recipe ID
     * @param request visibility update request DTO
     * @param userEmail user email address
     * @return updated RecipeResponse DTO
     */
    @Transactional
    public RecipeResponse updateVisibility(String recipeId, RecipeVisibilityUpdateRequestDTO request, String userEmail) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", recipeId));
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        OwnershipValidator.requireOwnerOrAdmin(recipe.getCreatedBy().getId(), currentUser,
                "You are not allowed to edit this recipe.");

        recipe.setVisibility(parseVisibility(request.visibility()));

        return RecipeMapper.toResponse(recipeRepository.save(recipe));
    }

    /**
     * Deletes a recipe by ID following ownership validation.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param recipeId target recipe ID
     * @param userEmail user email address
     */
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

    private Set<Tag> fetchTags(List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new java.util.LinkedHashSet<>();
        }
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != new HashSet<>(tagIds).size()) {
            Set<String> foundIds = tags.stream().map(Tag::getId).collect(Collectors.toSet());
            String missingId = tagIds.stream().filter(tagId -> !foundIds.contains(tagId)).findFirst().orElse(null);
            throw new ResourceNotFoundException("Tag", missingId);
        }
        return new java.util.LinkedHashSet<>(tags);
    }

    private Map<String, Unit> fetchUnitsById(List<IngredientRequestDTO> dtoList) {
        Set<String> unitIds = dtoList.stream().map(IngredientRequestDTO::unitId).collect(Collectors.toSet());
        return unitRepository.findAllById(unitIds).stream()
                .collect(Collectors.toMap(Unit::getId, unit -> unit));
    }

    private Set<Ingredient> saveIngredients(List<IngredientRequestDTO> dtoList, Recipe recipe,
            Map<String, Ingredient> tmpIdToIngredient) {
        Map<String, Unit> unitsById = fetchUnitsById(dtoList);

        Set<Ingredient> ingredients = new java.util.LinkedHashSet<>();
        for (IngredientRequestDTO ingDto : dtoList) {
            Unit unit = unitsById.get(ingDto.unitId());
            if (unit == null) {
                throw new ResourceNotFoundException("Unit", ingDto.unitId());
            }
            Ingredient ingredient = Ingredient.builder()
                    .recipe(recipe)
                    .name(ingDto.name())
                    .quantity(BigDecimal.valueOf(ingDto.quantity()))
                    .unit(unit)
                    .build();
            ingredients.add(ingredient);
            if (ingDto.tmpId() != null) {
                tmpIdToIngredient.put(ingDto.tmpId(), ingredient);
            }
        }
        return ingredients;
    }

    private Set<Instruction> saveInstructions(List<InstructionRequestDTO> dtoList, Recipe recipe,
            Map<String, Ingredient> tmpIdToIngredient) {
        Set<Instruction> instructions = new java.util.LinkedHashSet<>();
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
            instructions.add(instruction);
        }
        return instructions;
    }
}
