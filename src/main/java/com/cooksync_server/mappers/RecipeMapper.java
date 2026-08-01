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

public final class RecipeMapper {

    private RecipeMapper() {
    }

    public static RecipeResponse toResponse(Recipe r) {
        if (r == null) {
            return null;
        }
        String primaryImageUrl = resolvePrimaryImageUrl(r);
        List<String> imageUrls = resolveOrderedImageUrls(r, primaryImageUrl);

        return new RecipeResponse(
                r.getId(),
                UserMapper.toResponse(r.getCreatedBy()),
                r.getTitle(),
                r.getDescription(),
                r.getDifficulty() == null ? null : r.getDifficulty().name(),
                r.getVisibility() == null ? null : r.getVisibility().name(),
                r.getPrepTimeMinutes(),
                r.getCookTimeMinutes(),
                r.getServings(),
                r.getReviewCount(),
                r.getAverageRating(),
                mapReviews(r),
                MapperUtils.toIsoStringOrNull(r.getCreatedAt()),
                MapperUtils.toIsoStringOrNull(r.getUpdatedAt()),
                mapTags(r),
                mapIngredients(r),
                mapInstructions(r),
                primaryImageUrl,
                imageUrls
        );
    }

    public static RecipePreviewResponse toPreview(Recipe r) {
        return toPreview(r, false, null);
    }

    public static RecipePreviewResponse toPreview(Recipe r, boolean hasPersonalNote) {
        return toPreview(r, hasPersonalNote, null);
    }

    public static RecipePreviewResponse toPreview(Recipe r, boolean hasPersonalNote, String personalNoteText) {
        if (r == null) {
            return null;
        }
        String authorName = r.getCreatedBy() == null ? null
                : r.getCreatedBy().getFirstName() + " " + r.getCreatedBy().getLastName();

        return new RecipePreviewResponse(
                r.getId(),
                authorName,
                r.getTitle(),
                r.getDescription(),
                r.getDifficulty() == null ? null : r.getDifficulty().name(),
                r.getVisibility() == null ? null : r.getVisibility().name(),
                r.getPrepTimeMinutes(),
                r.getCookTimeMinutes(),
                r.getReviewCount(),
                r.getAverageRating(),
                MapperUtils.toIsoStringOrNull(r.getCreatedAt()),
                mapTags(r),
                resolvePrimaryImageUrl(r),
                hasPersonalNote,
                personalNoteText
        );
    }

    private static List<ReviewResponse> mapReviews(Recipe r) {
        return r.getReviews() == null ? List.of()
                : r.getReviews().stream().map(ReviewMapper::toResponse).collect(Collectors.toList());
    }

    private static List<TagResponse> mapTags(Recipe r) {
        return r.getTags() == null ? List.of()
                : r.getTags().stream().map(TagMapper::toResponse).collect(Collectors.toList());
    }

    private static Set<IngredientResponse> mapIngredients(Recipe r) {
        return r.getIngredients() == null ? Set.of()
                : r.getIngredients().stream().map(IngredientMapper::toResponse).collect(Collectors.toSet());
    }

    private static List<InstructionResponse> mapInstructions(Recipe r) {
        return r.getInstructions() == null ? List.of()
                : r.getInstructions().stream().map(InstructionMapper::toResponse).collect(Collectors.toList());
    }

    private static String resolvePrimaryImageUrl(Recipe r) {
        if (r.getImages() == null) {
            return null;
        }
        return r.getImages().stream()
                .filter(image -> image != null && image.isPrimary())
                .map(RecipeImage::getImageUrl)
                .findFirst()
                .orElse(null);
    }

    /**
     * Ensures the primary image (if any) is first in the list, since clients render
     * images.get(0) as the cover photo without re-checking which one is primary.
     */
    private static List<String> resolveOrderedImageUrls(Recipe r, String primaryImageUrl) {
        if (r.getImages() == null) {
            return List.of();
        }
        List<String> imageUrls = r.getImages().stream()
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
