package com.cooksync_server.dtos.response.tags;

import java.util.List;

import com.cooksync_server.entities.Tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {

    private String id;
    private String name;
    private String createdAt;

    public static List<TagResponse> fromEntities(List<Tag> tags) {
        return tags.stream()
                .map(tag -> TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .createdAt(tag.getCreatedAt().toString())
                .build())
                .toList();
    }
}
