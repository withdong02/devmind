package com.devmind.core.agent;

import java.util.Map;

public record AgentResponse(
    String agentId,
    String content,
    boolean handoff,
    String handoffToAgentId,
    Map<String, Object> metadata
) {
    public static AgentResponse reply(String agentId, String content) {
        return new AgentResponse(agentId, content, false, null, Map.of());
    }

    public static AgentResponse handoff(String fromAgentId, String toAgentId, String context) {
        return new AgentResponse(fromAgentId, context, true, toAgentId, Map.of());
    }
}
