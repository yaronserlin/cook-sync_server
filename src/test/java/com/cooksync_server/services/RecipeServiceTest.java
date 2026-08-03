package com.cooksync_server.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;

import com.cooksync_server.entities.Recipe;
import com.cooksync_server.repositories.IngredientRepository;
import com.cooksync_server.repositories.InstructionRepository;
import com.cooksync_server.repositories.RecipeImageRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.TagRepository;
import com.cooksync_server.repositories.UnitRepository;
import com.cooksync_server.repositories.UserRepository;
import com.dtos.response.PagedResponse;
import com.dtos.response.recipe.RecipePreviewResponse;

/**
 * Unit test for RecipeService managing recipe queries, pagination, and multi-criteria searching.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@ExtendWith(MockitoExtension.class)
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
    private RecipeImageRepository recipeImageRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private UnitRepository unitRepository;

    private RecipeService recipeService;

    /**
     * Initializes service with mocked dependencies before each test.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @BeforeEach
    void setUp() {
        recipeService = new RecipeService(recipeRepository, userRepository, ingredientRepository,
                instructionRepository, recipeImageRepository, tagRepository, unitRepository);
    }

    /**
     * Verifies that paginated recipe queries map metadata into PagedResponse DTOs.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getAllRecipesPaged_mapsPageMetadataOntoPagedResponse() {
        Recipe r1 = Recipe.builder().id("r1").title("Pasta").visibility(Recipe.Visibility.PUBLIC).build();
        Recipe r2 = Recipe.builder().id("r2").title("Soup").visibility(Recipe.Visibility.PUBLIC).build();
        when(recipeRepository.findByVisibility(eq(Recipe.Visibility.PUBLIC), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(1);
                    return new PageImpl<>(List.of(r1, r2), pageable, 7);
                });

        PagedResponse<RecipePreviewResponse> result = recipeService.getAllRecipesPaged(0, 2);

        assertThat(result.content()).extracting(RecipePreviewResponse::title).containsExactly("Pasta", "Soup");
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(7);
        assertThat(result.last()).isFalse();
    }

    /**
     * Verifies that search requests execute JPA Specifications even with blank filter criteria.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @SuppressWarnings("unchecked")
    @Test
    void searchRecipes_blankAuthorAndIngredient_stillDelegatesToRepositoryWithSpecification() {
        when(recipeRepository.findAll(any(Specification.class))).thenReturn(List.of());

        recipeService.searchRecipes("pasta", "  ", "");

        verify(recipeRepository).findAll(any(Specification.class));
    }

    /**
     * Verifies author-only recipe searches with empty keyword string.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @SuppressWarnings("unchecked")
    @Test
    void searchRecipes_blankKeywordWithAuthorOnly_stillSearchesByAuthorAlone() {
        ArgumentCaptor<Specification<Recipe>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(recipeRepository.findAll(specCaptor.capture())).thenReturn(List.of());

        recipeService.searchRecipes("", "Chef John", null);

        assertThat(specCaptor.getValue()).isNotNull();
        verify(recipeRepository).findAll(any(Specification.class));
    }

    /**
     * Verifies combined multi-criteria searches matching keyword, author, and ingredient filter terms.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @SuppressWarnings("unchecked")
    @Test
    void searchRecipes_allCriteriaProvided_mapsRepositoryResultsToPreviews() {
        Recipe match = Recipe.builder().id("r1").title("Creamy Tomato Pasta").visibility(Recipe.Visibility.PUBLIC).build();
        when(recipeRepository.findAll(any(Specification.class))).thenReturn(List.of(match));

        List<RecipePreviewResponse> results = recipeService.searchRecipes("pasta", "chef", "tomato");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo("Creamy Tomato Pasta");
    }
}
