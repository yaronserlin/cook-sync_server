package com.cooksync_server.dtos.response.unit;

import com.cooksync_server.entities.Unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing a measurement unit in API responses.
 * Encapsulates unit details such as its unique identifier, symbol code, full
 * name, and audit timestamps.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitResponse {

    private String id;
    private String code;
    private String name;
    private String createdAt;
    private String updatedAt;

    /**
     * Converts a persistent Unit entity into a formatted UnitResponse DTO.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * Unit unit = unitRepository.findById("uuid-1234").get();
     * UnitResponse response = UnitResponse.fromEntity(unit);
     * }</pre>
     *
     * @param unit The {@link Unit} entity retrieved from the database to be
     * converted.
     * @return A constructed {@link UnitResponse} containing the mapped and
     * formatted data from the entity.
     */
    public static UnitResponse fromEntity(Unit unit) {
        return UnitResponse.builder()
                .id(unit.getId())
                .code(unit.getCode())
                .name(unit.getName())
                .createdAt(unit.getCreatedAt().toString())
                .updatedAt(unit.getUpdatedAt().toString())
                .build();
    }
}
