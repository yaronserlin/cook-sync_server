package com.cooksync_server.mappers;

import com.cooksync_server.entities.Unit;
import com.dtos.response.unit.UnitResponse;

public final class UnitMapper {

    private UnitMapper() {
    }

    public static UnitResponse toResponse(Unit u) {
        if (u == null) {
            return null;
        }
        String created = u.getCreatedAt() == null ? null : u.getCreatedAt().toString();
        String updated = u.getUpdatedAt() == null ? null : u.getUpdatedAt().toString();
        return new UnitResponse(u.getId(), u.getCode(), u.getName(), created, updated);
    }
}
