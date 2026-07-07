package com.cooksync_server.dtos.response.tags;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.cooksync_server.entities.Tag;

/**
 * Data Transfer Object representing a tag in API responses.
 * Uses Java records for an immutable data carrier.
 */
public record TagResponse(
    String id,
    String name,
    String createdAt,
    String updatedAt
) {
    /**
     * Maps a persistent Tag entity into a TagResponse DTO.
     */
    public static TagResponse fromEntity(Tag tag) {
        return new TagResponse(
            tag.getId(),
            tag.getName(),
            tag.getCreatedAt() != null ? tag.getCreatedAt().toString() : null,
            tag.getUpdatedAt() != null ? tag.getUpdatedAt().toString() : null
        );
    }

    /**
     * Maps a collection of Tag entities to a List of TagResponse DTOs.
     */
    public static List<TagResponse> fromEntities(Collection<Tag> tags) {
        if (tags == null) return List.of();
        
        return tags.stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }
}