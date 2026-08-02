package com.cooksync_server.mappers;

import com.cooksync_server.entities.User;
import com.dtos.response.user.UserResponse;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        String created = MapperUtils.toIsoStringOrNull(user.getCreatedAt());
        String updated = MapperUtils.toIsoStringOrNull(user.getUpdatedAt());
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.isAdmin(), user.getAvatarUrl(), created, updated, user.isEnabled());
    }
}
