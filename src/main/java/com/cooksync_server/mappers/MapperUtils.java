package com.cooksync_server.mappers;

import java.time.LocalDateTime;

/**
 * Small shared helpers used by every entity-to-DTO mapper, so each mapper
 * doesn't repeat the same null-safe formatting logic.
 */
final class MapperUtils {

    private MapperUtils() {
    }

    static String toIsoStringOrNull(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toString();
    }
}
