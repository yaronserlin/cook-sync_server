package com.cooksync_server.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cooksync_server.dtos.request.CreateRecipeRequest;
import com.cooksync_server.dtos.request.IngredientDto;
import com.cooksync_server.dtos.request.InstructionDto;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("🧪 Business Logic Tests - RecipeService")
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private InstructionRepository instructionRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("✅ getAllRecipes returns recipe list")
    void shouldReturnAllRecipes() {
        Recipe recipe = Recipe.builder().id("r1").title("Test").build();
        when(recipeRepository.findAll()).thenReturn(List.of(recipe));

        List<Recipe> recipes = recipeService.getAllRecipes();

        assertThat(recipes).hasSize(1);
        assertThat(recipes.get(0).getId()).isEqualTo("r1");
        verify(recipeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("✅ getRecipeById returns a recipe when present")
    void shouldReturnRecipeWhenFound() {
        Recipe recipe = Recipe.builder().id("r1").title("Test").build();
        when(recipeRepository.findById("r1")).thenReturn(Optional.of(recipe));

        Recipe result = recipeService.getRecipeById("r1");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test");
    }

    @Test
    @DisplayName("✅ getRecipeById throws when recipe is missing")
    void shouldThrowWhenRecipeNotFound() {
        when(recipeRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getRecipeById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recipe not found: missing");
    }

    @Test
    @DisplayName("✅ createRecipe saves recipe with ingredients and instructions")
    void shouldCreateRecipeSuccessfully() {
        User creator = User.builder().id("u1").email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(creator));
        when(tagRepository.findByNameIgnoreCase("Dessert")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag saved = invocation.getArgument(0);
            saved.setId("t1");
            return saved;
        });
        when(unitRepository.findByCode("g")).thenReturn(Optional.of(Unit.builder().id("u1").code("g").name("Grams").build()));

        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe recipe = invocation.getArgument(0);
            if (recipe.getId() == null) {
                recipe.setId("r1");
            }
            return recipe;
        });
        when(ingredientRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(instructionRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setTitle("Cake");
        request.setDescription("Delicious");
        request.setDifficulty(Recipe.Difficulty.MEDIUM);
        request.setPrepTimeMinutes(10);
        request.setCookTimeMinutes(45);
        request.setServings(4);
        request.setTagNames(Set.of("Dessert"));
        IngredientDto ingredientDto = new IngredientDto();
        ingredientDto.setName("Flour");
        ingredientDto.setQuantity(new BigDecimal("200"));
        ingredientDto.setUnitCode("g");
        request.setIngredients(List.of(ingredientDto));
        InstructionDto instructionDto = new InstructionDto();
        instructionDto.setStepNumber(1);
        instructionDto.setDescription("Mix ingredients");
        instructionDto.setHasTimer(false);
        request.setInstructions(List.of(instructionDto));

        Recipe result = recipeService.createRecipe(request, "user@example.com");

        assertThat(result.getId()).isEqualTo("r1");
        assertThat(result.getTitle()).isEqualTo("Cake");
        assertThat(result.getIngredients()).hasSize(1);
        assertThat(result.getInstructions()).hasSize(1);
        assertThat(result.getTags()).hasSize(1);
    }

    @Test
    @DisplayName("✅ updateRecipe throws when user is not authorized")
    void shouldThrowWhenUpdatingUnauthorized() {
        User owner = User.builder().id("u1").email("owner@example.com").build();
        Recipe recipe = Recipe.builder().id("r1").createdBy(owner).build();
        when(recipeRepository.findById("r1")).thenReturn(Optional.of(recipe));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(User.builder().id("u2").email("other@example.com").isAdmin(false).build()));

        CreateRecipeRequest request = new CreateRecipeRequest();

        assertThatThrownBy(() -> recipeService.updateRecipe("r1", request, "other@example.com"))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("You are not allowed to update this recipe");
    }
}
