package com.devmind.agents.agent;

import com.devmind.core.agent.AgentRole;
import com.devmind.core.skill.SkillRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class CodeAgent extends AbstractAgent {

    private final SkillRegistry skillRegistry;

    public CodeAgent(ChatClient.Builder chatClientBuilder, SkillRegistry skillRegistry) {
        super(chatClientBuilder);
        this.skillRegistry = skillRegistry;
    }

    @Override
    public String getId() {
        return "code-agent";
    }

    @Override
    public AgentRole getRole() {
        return AgentRole.CODE;
    }

    @Override
    public String getDescription() {
        return "Code specialist: analysis, debugging, review, refactoring";
    }

    @Override
    protected String getSystemPrompt() {
        return """
                You are the Code Agent in DevMind, a specialized code assistant.
                Your expertise: code analysis, debugging, code review, refactoring, and bug detection.

                Guidelines:
                - Analyze code thoroughly for bugs, performance issues, and style problems
                - Provide specific, actionable suggestions with code examples
                - When debugging, trace the root cause systematically
                - For refactoring, preserve behavior while improving structure
                - Be concise but precise in technical explanations
                """;
    }
}
