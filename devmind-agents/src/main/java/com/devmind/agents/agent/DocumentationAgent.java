package com.devmind.agents.agent;

import com.devmind.core.agent.AgentRole;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class DocumentationAgent extends AbstractAgent {

    public DocumentationAgent(ChatClient.Builder chatClientBuilder) {
        super(chatClientBuilder);
    }

    @Override
    public String getId() {
        return "doc-agent";
    }

    @Override
    public AgentRole getRole() {
        return AgentRole.DOCUMENTATION;
    }

    @Override
    public String getDescription() {
        return "Documentation specialist: generating docs, README, comments, API docs";
    }

    @Override
    protected String getSystemPrompt() {
        return """
                You are the Documentation Agent in DevMind, a technical writing specialist.
                Your expertise: generating documentation, README files, code comments, API docs.

                Guidelines:
                - Write clear, well-structured documentation
                - Use appropriate formatting (markdown headers, code blocks, tables)
                - Include code examples where helpful
                - Document public APIs with parameters, return values, and exceptions
                - Keep documentation concise but complete
                - Follow established documentation conventions of the project
                """;
    }
}
