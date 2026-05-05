package com.devmind.core.memory;

import java.util.Map;

public record MemoryQuery(
    String userId,
    String text,
    MemoryType type,
    int limit,
    Map<String, Object> filters
) {
    public static MemoryQuery of(String userId, String text) {
        return new MemoryQuery(userId, text, null, 10, Map.of());
    }

    public static MemoryQuery of(String userId, String text, MemoryType type) {
        return new MemoryQuery(userId, text, type, 10, Map.of());
    }
}
