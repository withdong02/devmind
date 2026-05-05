package com.devmind.agents.orchestration;

import com.devmind.core.agent.Agent;
import com.devmind.core.agent.AgentRole;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of all available agents. Agents auto-register via @Component.
 */
@Component
public class AgentRegistry {

    private final Map<String, Agent> agentsById = new ConcurrentHashMap<>();
    private final Map<AgentRole, Agent> agentsByRole = new ConcurrentHashMap<>();

    public AgentRegistry(List<Agent> agents) {
        for (Agent agent : agents) {
            agentsById.put(agent.getId(), agent);
            agentsByRole.put(agent.getRole(), agent);
        }
    }

    public Optional<Agent> getById(String id) {
        return Optional.ofNullable(agentsById.get(id));
    }

    public Optional<Agent> getByRole(AgentRole role) {
        return Optional.ofNullable(agentsByRole.get(role));
    }

    public Optional<Agent> getByRoleName(String roleName) {
        try {
            AgentRole role = AgentRole.valueOf(roleName.toUpperCase());
            return getByRole(role);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public List<Agent> getAllAgents() {
        return List.copyOf(agentsById.values());
    }
}
