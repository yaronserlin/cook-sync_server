package com.cooksync_server.mappers;

import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.RecipeImage;
import com.dtos.response.ingredient.IngredientResponse;
import com.dtos.response.instruction.InstructionResponse;
import com.dtos.response.recipe.RecipePreviewResponse;
import com.dtos.response.recipe.RecipeResponse;
import com.dtos.response.review.ReviewResponse;
import com.dtos.response.tags.TagResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper utility class transforming Recipe entities into RecipeResponse and RecipePreviewResponse DTOs.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
public final class RecipeMapper {

    private RecipeMapper() {
    }

    /**
     * Converts a Recipe entity into a full detail RecipeResponse DTO.
     *
     * Complexity:
     * Time: O(R + T + I + S) where R=reviews, T=tags, I=ingredients, S=instructions
     * Space: O(R + T + I + S)
     *
     * @param recipe target Recipe entity
     * @return populated RecipeResponse instance or null
     */
    public static RecipeResponse toResponse(Recipe recipe) {
        if (recipe == null) {
            return null;
        }
        String primaryImageUrl = resolvePrimaryImageUrl(recipe);
        List<String> imageUrls = resolveOrderedImageUrls(recipe, primaryImageUrl);

        return new RecipeResponse(
                recipe.getId(),
                UserMapper.toResponse(recipe.getCreatedBy()),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getDifficulty() == null ? null : recipe.getDifficulty().name(),
                recipe.getVisibility() == null ? null : recipe.getVisibility().name(),
                recipe.getPrepTimeMinutes(),
                recipe.getCookTimeMinutes(),
                recipe.getServings(),
                recipe.getReviewCount(),
                recipe.getAverageRating(),
                mapReviews(recipe),
                MapperUtils.toIsoStringOrNull(recipe.getCreatedAt()),
                MapperUtils.toIsoStringOrNull(recipe.getUpdatedAt()),
                mapTags(recipe),
                mapIngredients(recipe),
                mapInstructions(recipe),
                primaryImageUrl,
                imageUrls
        );
    }

    /**
     * Converts a Recipe entity into a lightweight RecipePreviewResponse DTO.
     *
     * Complexity:
     * Time: O(T) where T is tag count
     * Space: O(T)
     *
     * @param recipe target Recipe entity
     * @return populated RecipePreviewResponse instance
     */
    public static RecipePreviewResponse toPreview(Recipe recipe) {
        return toPreview(recipe, false, null);
    }

    /**
     * Converts a Recipe entity into a RecipePreviewResponse with personal note flag.
     *
     * Complexity:
     * Time: O(T) where T is tag count
     * Space: O(T)
     *
     * @param recipe target Recipe entity
     * @param hasPersonalNote flag indicating user attached note
     * @return populated RecipePreviewResponse instance
     */
    public static RecipePreviewResponse toPreview(Recipe recipe, boolean hasPersonalNote) {
        return toPreview(recipe, hasPersonalNote, null);
    }

    /**
     * Converts a Recipe entity into a RecipePreviewResponse with personal note text.
     *
     * Complexity:
     * Time: O(T) where T is tag count
     * Space: O(T)
     *
     * @param recipe target Recipe entity
     * @param hasPersonalNote flag indicating user attached note
     * @param personalNoteText personal note content
     * @return populated RecipePreviewResponse instance
     */
    public static RecipePreviewResponse toPreview(Recipe recipe, boolean hasPersonalNote, String personalNoteText) {
        if (recipe == null) {
            return null;
        }
        String authorName = recipe.getCreatedBy() == null ? null : recipe.getCreatedBy().getFullName();

        return new RecipePreviewResponse(
                recipe.getId(),
                authorName,
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getDifficulty() == null ? null : recipe.getDifficulty().name(),
                recipe.getVisibility() == null ? null : recipe.getVisibility().name(),
                recipe.getPrepTimeMinutes(),
                recipe.getCookTimeMinutes(),
                recipe.getReviewCount(),
                recipe.getAverageRating(),
                MapperUtils.toIsoStringOrNull(recipe.getCreatedAt()),
                mapTags(recipe),
                resolvePrimaryImageUrl(recipe),
                hasPersonalNote,
                personalNoteText
        );
    }

    private static List<ReviewResponse> mapReviews(Recipe recipe) {
        return recipe.getReviews() == null ? List.of()
                : recipe.getReviews().stream().map(ReviewMapper::toResponse).collect(Collectors.toList());
    }

    private static List<TagResponse> mapTags(Recipe recipe) {
        return recipe.getTags() == null ? List.of()
                : recipe.getTags().stream().map(TagMapper::toResponse).collect(Collectors.toList());
    }

    private static Set<IngredientResponse> mapIngredients(Recipe recipe) {
        return recipe.getIngredients() == null ? Set.of()
                : recipe.getIngredients().stream().map(IngredientMapper::toResponse).collect(Collectors.toSet());
    }

    private static List<InstructionResponse> mapInstructions(Recipe recipe) {
        return recipe.getInstructions() == null ? List.of()
                : recipe.getInstructions().stream().map(InstructionMapper::toResponse).collect(Collectors.toList());
    }

    private static String resolvePrimaryImageUrl(Recipe recipe) {
        if (recipe.getImages() == null) {
            return null;
        }
        return recipe.getImages().stream()
                .filter(image -> image != null && image.isPrimary())
                .map(RecipeImage::getImageUrl)
                .findFirst()
                .orElse(null);
    }

    private static List<String> resolveOrderedImageUrls(Recipe recipe, String primaryImageUrl) {
        if (recipe.getImages() == null) {
            return List.of();
        }
        List<String> imageUrls = recipe.getImages().stream()
                .filter(image -> image != null)
                .map(RecipeImage::getImageUrl)
                .toList();

        if (primaryImageUrl != null && !imageUrls.isEmpty() && !imageUrls.get(0).equals(primaryImageUrl)) {
            imageUrls = new ArrayList<>(imageUrls);
            imageUrls.remove(primaryImageUrl);
            imageUrls.add(0, primaryImageUrl);
        }
        return imageUrls;
    }
}
