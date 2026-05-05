package com.devmind.core.skill;

import java.util.Map;

/**
 * Context provided to skills during execution.
 * Bridges skills to memory, RAG, LLM, and other system capabilities.
 */
public record SkillContext(
    String sessionId,
    String userId,
    Map<String, Object> attributes
) {
    public static SkillContext of(String sessionId, String userId) {
        return new SkillContext(sessionId, userId, Map.of());
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return value != null ? (T) value : null;
    }
}
