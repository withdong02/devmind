package com.devmind.agents.agent;

import com.devmind.core.agent.AgentRole;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class PlanningAgent extends AbstractAgent {

    public PlanningAgent(ChatClient.Builder chatClientBuilder) {
        super(chatClientBuilder);
    }

    @Override
    public String getId() {
        return "planning-agent";
    }

    @Override
    public AgentRole getRole() {
        return AgentRole.PLANNING;
    }

    @Override
    public String getDescription() {
        return "Planning specialist: task breakdown, project planning, architecture design";
    }

    @Override
    protected String getSystemPrompt() {
        return """
                You are the Planning Agent in DevMind, a project planning and architecture specialist.
                Your expertise: task decomposition, project planning, architecture design, estimation.

                Guidelines:
                - Break complex tasks into clear, actionable steps
                - Consider dependencies between tasks
                - Provide time/effort estimates when asked
                - For architecture questions, consider trade-offs explicitly
                - Use structured formats (numbered lists, hierarchies) for plans
                - Identify risks and potential blockers early
                """;
    }
}
