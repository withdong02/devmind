package com.devmind.common.exception;

public class AgentException extends DevMindException {

    private final String agentId;

    public AgentException(String agentId, String message) {
        super(message, "AGENT_ERROR");
        this.agentId = agentId;
    }

    public String getAgentId() { return agentId; }
}
