package com.devmind.api.controller;

import com.devmind.agents.service.AgentMessageBus;
import com.devmind.agents.orchestration.AgentRegistry;
import com.devmind.agents.orchestration.OrchestratorAgent;
import com.devmind.core.agent.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "Agents", description = "Multi-agent system: orchestration, execution, routing")
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final AgentRegistry agentRegistry;
    private final AgentMessageBus messageBus;

    public AgentController(AgentRegistry agentRegistry, AgentMessageBus messageBus) {
        this.agentRegistry = agentRegistry;
        this.messageBus = messageBus;
    }

    @Operation(summary = "List all registered agents")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listAgents() {
        List<Map<String, Object>> agents = new ArrayList<>();
        for (Agent agent : agentRegistry.getAllAgents()) {
            agents.add(Map.of(
                    "id", agent.getId(),
                    "role", agent.getRole().toString(),
                    "description", agent.getDescription()
            ));
        }
        return ResponseEntity.ok(agents);
    }

    @Operation(summary = "Execute a specific agent")
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> execute(@RequestBody Map<String, Object> body) {
        String agentRole = (String) body.getOrDefault("agentRole", "CODE");
        String content = (String) body.getOrDefault("content", "");

        Optional<Agent> agent = agentRegistry.getByRoleName(agentRole);
        if (agent.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Agent not found: " + agentRole));
        }

        AgentMessage message = AgentMessage.request("user", agent.get().getId(), content);
        AgentSession session = AgentSession.create(agent.get().getId());
        AgentResponse response = agent.get().handle(message, session);
        return ResponseEntity.ok(toResponseMap(response));
    }

    @Operation(summary = "Orchestrate a complex task across multiple agents")
    @PostMapping("/orchestrate")
    public ResponseEntity<Map<String, Object>> orchestrate(@RequestBody Map<String, Object> body) {
        String content = (String) body.getOrDefault("content", "");
        Optional<Agent> orchestrator = agentRegistry.getById(OrchestratorAgent.ID);
        if (orchestrator.isEmpty()) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Orchestrator not available"));
        }

        AgentMessage message = AgentMessage.request("user", OrchestratorAgent.ID, content);
        AgentSession session = AgentSession.create(OrchestratorAgent.ID);
        AgentResponse response = orchestrator.get().handle(message, session);
        return ResponseEntity.ok(toResponseMap(response));
    }

    @Operation(summary = "Get agent message bus log")
    @GetMapping("/messages")
    public ResponseEntity<List<Map<String, Object>>> getMessageLog() {
        List<Map<String, Object>> messages = new ArrayList<>();
        for (AgentMessage msg : messageBus.getMessageLog()) {
            messages.add(Map.of(
                    "id", msg.id(),
                    "from", msg.fromAgentId(),
                    "to", msg.toAgentId(),
                    "type", msg.type().toString(),
                    "preview", msg.content().length() > 100 ? msg.content().substring(0, 100) + "..." : msg.content()
            ));
        }
        return ResponseEntity.ok(messages);
    }

    private Map<String, Object> toResponseMap(AgentResponse response) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("agentId", response.agentId());
        result.put("content", response.content());
        result.put("handoff", response.handoff());
        if (response.metadata() != null) {
            result.putAll(response.metadata());
        }
        return result;
    }
}
