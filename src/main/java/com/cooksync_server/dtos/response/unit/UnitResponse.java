package com.cooksync_server.dtos.response.unit;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.cooksync_server.entities.Unit;

/**
 * Data Transfer Object representing a measurement unit in API responses.
 * Uses Java records for an immutable data carrier.
 */
public record UnitResponse(
    String id,
    String code,
    String name,
    String createdAt,
    String updatedAt
) {
    /**
     * Maps a persistent Unit entity into a formatted UnitResponse DTO.
     */
    public static UnitResponse fromEntity(Unit unit) {
        return new UnitResponse(
            unit.getId(),
            unit.getCode(),
            unit.getName(),
            unit.getCreatedAt() != null ? unit.getCreatedAt().toString() : null,
            unit.getUpdatedAt() != null ? unit.getUpdatedAt().toString() : null
        );
    }

    /**
     * Maps a collection of Unit entities to a List of UnitResponse DTOs.
     */
    public static List<UnitResponse> fromEntities(Collection<Unit> units) {
        if (units == null) return List.of();
        
        return units.stream()
                .map(UnitResponse::fromEntity)
                .collect(Collectors.toList());
    }
}