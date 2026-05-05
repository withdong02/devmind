package com.devmind.agents.orchestration;

import com.devmind.core.agent.*;
import com.devmind.agents.service.AgentMessageBus;
import com.devmind.context.service.ContextService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Orchestrator agent: receives user requests, decomposes into subtasks,
 * routes to specialist agents, and aggregates results.
 */
@Component
public class OrchestratorAgent implements Agent {

    public static final String ID = "orchestrator";

    private final ChatClient chatClient;
    private final AgentRegistry agentRegistry;
    private final TaskDecomposer taskDecomposer;
    private final AgentMessageBus messageBus;

    public OrchestratorAgent(ChatClient.Builder chatClientBuilder,
                             @Lazy AgentRegistry agentRegistry,
                             TaskDecomposer taskDecomposer,
                             AgentMessageBus messageBus) {
        this.chatClient = chatClientBuilder.build();
        this.agentRegistry = agentRegistry;
        this.taskDecomposer = taskDecomposer;
        this.messageBus = messageBus;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public AgentRole getRole() {
        return AgentRole.ORCHESTRATOR;
    }

    @Override
    public String getDescription() {
        return "Orchestrator: decomposes tasks, routes to specialists, aggregates results";
    }

    @Override
    public AgentResponse handle(AgentMessage message, AgentSession session) {
        session.addMessage(message);
        String userRequest = message.content();

        // Decompose into subtasks
        List<TaskDecomposer.SubTask> subTasks = taskDecomposer.decompose(userRequest);

        // If single task for a single agent, route directly
        if (subTasks.size() == 1) {
            return routeToAgent(subTasks.get(0), message, session);
        }

        // Multi-task: execute each and aggregate
        List<String> results = new ArrayList<>();
        List<Map<String, Object>> taskDetails = new ArrayList<>();

        for (TaskDecomposer.SubTask subTask : subTasks) {
            Optional<Agent> agent = agentRegistry.getByRoleName(subTask.agentRole());
            if (agent.isEmpty()) {
                results.add("[" + subTask.agentRole() + "] Agent not available");
                taskDetails.add(Map.of("task", subTask.description(), "agent", subTask.agentRole(), "status", "skipped"));
                continue;
            }

            AgentMessage subMessage = AgentMessage.request(getId(), agent.get().getId(), subTask.description());
            AgentSession subSession = AgentSession.create(agent.get().getId());
            messageBus.send(subMessage);

            try {
                AgentResponse response = agent.get().handle(subMessage, subSession);
                results.add("[" + agent.get().getRole() + "]\n" + response.content());
                taskDetails.add(Map.of(
                        "task", subTask.description(),
                        "agent", agent.get().getRole().toString(),
                        "status", "completed",
                        "preview", response.content().length() > 100
                                ? response.content().substring(0, 100) + "..."
                                : response.content()
                ));
            } catch (Exception e) {
                results.add("[" + agent.get().getRole() + "] Error: " + e.getMessage());
                taskDetails.add(Map.of("task", subTask.description(), "agent", agent.get().getRole().toString(), "status", "error"));
            }
        }

        // Aggregate results
        String aggregated = aggregateResults(userRequest, results);
        Map<String, Object> metadata = Map.of(
                "subtaskCount", subTasks.size(),
                "taskDetails", taskDetails,
                "agentsUsed", taskDetails.stream().map(d -> d.get("agent")).distinct().toList()
        );

        return new AgentResponse(getId(), aggregated, false, null, metadata);
    }

    private AgentResponse routeToAgent(TaskDecomposer.SubTask subTask, AgentMessage originalMessage, AgentSession session) {
        Optional<Agent> agent = agentRegistry.getByRoleName(subTask.agentRole());
        if (agent.isEmpty()) {
            return AgentResponse.reply(getId(), "No agent available for role: " + subTask.agentRole());
        }

        Agent agentInstance = agent.get();
        AgentMessage routedMessage = AgentMessage.request(getId(), agentInstance.getId(), subTask.description());
        AgentSession subSession = AgentSession.create(agentInstance.getId());
        messageBus.send(routedMessage);

        AgentResponse response = agentInstance.handle(routedMessage, subSession);
        // Wrap with orchestrator metadata
        return new AgentResponse(getId(), response.content(), false, null,
                Map.of("routedTo", agentInstance.getRole().toString(), "subtaskCount", 1));
    }

    private String aggregateResults(String originalRequest, List<String> results) {
        if (results.size() == 1) return results.get(0);

        StringBuilder sb = new StringBuilder();
        sb.append("## Multi-Agent Response\n\n");
        sb.append("**Request:** ").append(originalRequest).append("\n\n");
        for (int i = 0; i < results.size(); i++) {
            sb.append(results.get(i));
            if (i < results.size() - 1) sb.append("\n\n---\n\n");
        }
        return sb.toString();
    }
}
