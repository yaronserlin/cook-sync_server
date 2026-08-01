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
        String created = MapperUtils.toIsoStringOrNull(u.getCreatedAt());
        String updated = MapperUtils.toIsoStringOrNull(u.getUpdatedAt());
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.isAdmin(), u.getAvatarUrl(), created, updated, u.isEnabled());
    }
}
