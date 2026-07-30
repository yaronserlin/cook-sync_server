package com.cooksync_server.mappers;

import com.cooksync_server.entities.User;
import com.dtos.response.user.UserResponse;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User u) {
        if (u == null) {
            return null;
        }
        String created = u.getCreatedAt() == null ? null : u.getCreatedAt().toString();
        String updated = u.getUpdatedAt() == null ? null : u.getUpdatedAt().toString();
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.isAdmin(), u.getAvatarUrl(), created, updated);
    }
}
