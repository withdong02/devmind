package com.devmind.core.agent;

import java.util.List;

/**
 * Core interface for all DevMind agents.
 * Each agent has a specific role, its own system prompt, and a set of tools.
 */
public interface Agent {

    /**
     * Unique identifier for this agent.
     */
    String getId();

    /**
     * The role this agent plays in the system.
     */
    AgentRole getRole();

    /**
     * Human-readable description of what this agent does.
     */
    String getDescription();

    /**
     * Handles a message and returns a response.
     */
    AgentResponse handle(AgentMessage message, AgentSession session);
}
