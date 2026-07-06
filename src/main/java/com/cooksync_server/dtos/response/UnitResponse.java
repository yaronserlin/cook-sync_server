package com.cooksync_server.dtos.response;

import com.cooksync_server.entities.Unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
