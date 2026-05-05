package com.devmind.skills.impl.buganalysis;

import com.devmind.core.skill.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BugAnalysisSkill implements Skill {

    private final ChatClient chatClient;

    public BugAnalysisSkill(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "bug-analysis",
                "Bug Analysis",
                "Analyzes stack traces, error logs, and bug descriptions to identify root causes and suggest fixes",
                Map.of("error", "string", "context", "string"),
                List.of("bug", "error", "stacktrace", "debug", "exception")
        );
    }

    @Override
    public SkillOutput execute(SkillInput input, SkillContext context) {
        String error = input.getString("error");
        String codeContext = input.getString("context");

        if (error == null || error.isBlank()) {
            return SkillOutput.failure("No error information provided");
        }

        String prompt = buildAnalysisPrompt(error, codeContext);
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return SkillOutput.success(response, Map.of("skill", "bug-analysis"));
    }

    @Override
    public boolean canHandle(String userIntent) {
        String lower = userIntent.toLowerCase();
        return lower.contains("bug") || lower.contains("error") || lower.contains("exception")
                || lower.contains("stacktrace") || lower.contains("stack trace")
                || lower.contains("crash") || lower.contains("not working");
    }

    private String buildAnalysisPrompt(String error, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze the following error/bug and provide:\n");
        sb.append("1. Root cause analysis\n");
        sb.append("2. Step-by-step debugging approach\n");
        sb.append("3. Suggested fix with code example\n\n");
        sb.append("Error:\n```\n").append(error).append("\n```\n");
        if (context != null && !context.isBlank()) {
            sb.append("\nAdditional context:\n```\n").append(context).append("\n```");
        }
        return sb.toString();
    }
}
