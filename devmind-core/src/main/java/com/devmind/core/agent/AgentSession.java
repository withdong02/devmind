package com.devmind.core.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Maintains conversation state for an agent within a session.
 */
public class AgentSession {

    private final String sessionId;
    private final String agentId;
    private final List<AgentMessage> history;
    private final Map<String, Object> attributes;

    public AgentSession(String sessionId, String agentId, Map<String, Object> attributes) {
        this.sessionId = sessionId;
        this.agentId = agentId;
        this.history = new ArrayList<>();
        this.attributes = attributes;
    }

    public String getSessionId() { return sessionId; }
    public String getAgentId() { return agentId; }
    public List<AgentMessage> getHistory() { return history; }
    public Map<String, Object> getAttributes() { return attributes; }

    public void addMessage(AgentMessage message) {
        history.add(message);
    }

    public static AgentSession create(String agentId) {
        return new AgentSession(UUID.randomUUID().toString(), agentId, Map.of());
    }
}
