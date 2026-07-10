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
        String created = t.getCreatedAt() == null ? null : t.getCreatedAt().toString();
        String updated = t.getUpdatedAt() == null ? null : t.getUpdatedAt().toString();
        return new TagResponse(t.getId(), t.getName(), created, updated);
    }
}
