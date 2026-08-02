package com.cooksync_server.mappers;

import com.cooksync_server.entities.Unit;
import com.dtos.response.unit.UnitResponse;

public final class UnitMapper {

    private UnitMapper() {
    }

    public static UnitResponse toResponse(Unit unit) {
        if (unit == null) {
            return null;
        }
        String created = MapperUtils.toIsoStringOrNull(unit.getCreatedAt());
        String updated = MapperUtils.toIsoStringOrNull(unit.getUpdatedAt());
        return new UnitResponse(unit.getId(), unit.getCode(), unit.getName(), created, updated);
    }
}
