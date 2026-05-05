package com.devmind.core.agent;

import java.util.Map;
import java.util.UUID;

public record AgentMessage(
    String id,
    String fromAgentId,
    String toAgentId,
    MessageType type,
    String content,
    Map<String, Object> metadata
) {
    public AgentMessage {
        if (id == null) id = UUID.randomUUID().toString();
    }

    public static AgentMessage request(String from, String to, String content) {
        return new AgentMessage(null, from, to, MessageType.REQUEST, content, Map.of());
    }

    public static AgentMessage response(String from, String to, String content) {
        return new AgentMessage(null, from, to, MessageType.RESPONSE, content, Map.of());
    }

    public enum MessageType {
        REQUEST,
        RESPONSE,
        HANDOFF,
        ESCALATION
    }
}
