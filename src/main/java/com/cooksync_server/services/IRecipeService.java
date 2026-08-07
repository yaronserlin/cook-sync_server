package com.cooksync_server.services;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cooksync_server.repositories.RecipeImageRepository;
import com.cooksync_server.entities.RecipeImage;
import com.dtos.request.instruction.InstructionRequestDTO;
import com.dtos.response.recipe.DescriptionBlockDTO;
import java.math.BigDecimal;
import com.dtos.request.ingredient.IngredientRequestDTO;
import java.util.stream.Collectors;
import com.cooksync_server.repositories.IngredientRepository;
import com.cooksync_server.repositories.RecipeRepository;
import org.springframework.data.domain.Sort;
import com.cooksync_server.repositories.UnitRepository;
import com.cooksync_server.entities.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import com.cooksync_server.repositories.TagRepository;
import com.cooksync_server.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import com.dtos.response.recipe.RecipeResponse;
import java.util.ArrayList;
import java.util.HashSet;
import com.dtos.request.recipe.RecipeVisibilityUpdateRequestDTO;
import java.util.Map;
import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.entities.DescriptionBlock;
import com.cooksync_server.mappers.RecipeMapper;
import com.cooksync_server.entities.Recipe;
import com.dtos.response.PagedResponse;
import com.dtos.request.recipe.RecipeCreateRequestDTO;
import com.cooksync_server.entities.User;
import com.cooksync_server.repositories.RecipeSpecifications;
import com.cooksync_server.repositories.InstructionRepository;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import com.dtos.response.recipe.RecipePreviewResponse;
import com.cooksync_server.entities.Instruction;
import com.cooksync_server.entities.Unit;
import org.springframework.data.jpa.domain.Specification;

/**
 * Interface for RecipeService.
 */
public interface IRecipeService {
    List<RecipePreviewResponse> getAllRecipes();

    PagedResponse<RecipePreviewResponse> getAllRecipesPaged(int page, int size, String sortBy, String difficulty, Double minRating);

    RecipeResponse getRecipeById(String id);

    PagedResponse<RecipePreviewResponse> searchRecipes(String keyword, String author, String ingredient, String sortBy, String difficulty, Double minRating, int page, int size);

    PagedResponse<RecipePreviewResponse> findRecipesByTag(String tagName, String sortBy, String difficulty, Double minRating, int page, int size);

    PagedResponse<RecipePreviewResponse> getMyRecipes(String userEmail, int page, int size);

    RecipeResponse createRecipe(RecipeCreateRequestDTO request, String userEmail);

    RecipeResponse updateRecipe(String recipeId, RecipeCreateRequestDTO request, String userEmail);

    RecipeResponse updateVisibility(String recipeId, RecipeVisibilityUpdateRequestDTO request, String userEmail);

    void deleteRecipe(String recipeId, String userEmail);

}
