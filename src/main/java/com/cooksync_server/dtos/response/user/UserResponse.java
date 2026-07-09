package com.cooksync_server.dtos.response.user;

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
    private String firstName;
    private String lastName;
    private String email;
    private String createdAt;
    private String updatedAt;
    private boolean isAdmin;

    public static UserResponse fromEntity(User createdBy) {
        return UserResponse.builder()
                .id(createdBy.getId())
                .firstName(createdBy.getFirstName())
                .lastName(createdBy.getLastName())
                .email(createdBy.getEmail())
                .createdAt(createdBy.getCreatedAt() != null ? createdBy.getCreatedAt().toString() : null)
                .updatedAt(createdBy.getUpdatedAt() != null ? createdBy.getUpdatedAt().toString() : null)
                .isAdmin(createdBy.isAdmin())
                .build();
    }

}
