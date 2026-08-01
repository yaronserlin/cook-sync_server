package com.cooksync_server.mappers;

import com.cooksync_server.entities.Tag;
import com.dtos.response.tags.TagResponse;

public final class TagMapper {

    private TagMapper() {
    }

    public static TagResponse toResponse(Tag t) {
        if (t == null) {
            return null;
        }
        String created = MapperUtils.toIsoStringOrNull(t.getCreatedAt());
        String updated = MapperUtils.toIsoStringOrNull(t.getUpdatedAt());
        return new TagResponse(t.getId(), t.getName(), created, updated);
    }
}
