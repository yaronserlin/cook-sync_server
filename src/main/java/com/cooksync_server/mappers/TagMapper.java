package com.cooksync_server.mappers;

import com.cooksync_server.entities.Tag;
import com.dtos.response.tags.TagResponse;

public final class TagMapper {

    private TagMapper() {
    }

    public static TagResponse toResponse(Tag tag) {
        if (tag == null) {
            return null;
        }
        String created = MapperUtils.toIsoStringOrNull(tag.getCreatedAt());
        String updated = MapperUtils.toIsoStringOrNull(tag.getUpdatedAt());
        return new TagResponse(tag.getId(), tag.getName(), created, updated);
    }
}
