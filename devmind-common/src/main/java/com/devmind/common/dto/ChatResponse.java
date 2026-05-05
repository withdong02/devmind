package com.devmind.common.dto;

import java.util.Map;

public record ChatResponse(
    String messageId,
    String content,
    String agentId,
    String skillId,
    Map<String, Object> metadata
) {
    public static ChatResponse of(String messageId, String content) {
        return new ChatResponse(messageId, content, null, null, Map.of());
    }
}
