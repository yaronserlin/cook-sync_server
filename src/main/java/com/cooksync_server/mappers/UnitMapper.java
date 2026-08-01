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
        String created = MapperUtils.toIsoStringOrNull(u.getCreatedAt());
        String updated = MapperUtils.toIsoStringOrNull(u.getUpdatedAt());
        return new UnitResponse(u.getId(), u.getCode(), u.getName(), created, updated);
    }
}
