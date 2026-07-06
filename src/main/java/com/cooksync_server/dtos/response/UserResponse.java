package com.cooksync_server.dtos.response;

import com.cooksync_server.entities.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String name;
    private String email;
    private String createdAt;
    private String updatedAt;
    private boolean isAdmin;

    public static UserResponse fromEntity(User createdBy) {
        return UserResponse.builder()
                .id(createdBy.getId())
                .name(createdBy.getName())
                .email(createdBy.getEmail())
                .createdAt(createdBy.getCreatedAt().toString())
                .updatedAt(createdBy.getUpdatedAt().toString())
                .isAdmin(createdBy.isAdmin())
                .build();
    }

}
