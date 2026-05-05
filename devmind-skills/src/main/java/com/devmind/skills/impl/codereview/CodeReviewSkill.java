package com.devmind.skills.impl.codereview;

import com.devmind.core.skill.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CodeReviewSkill implements Skill {

    private final ChatClient chatClient;

    public CodeReviewSkill(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "code-review",
                "Code Review",
                "Analyzes code for potential bugs, style issues, performance problems, and suggests improvements",
                Map.of("code", "string", "language", "string"),
                List.of("code", "review", "analysis", "quality")
        );
    }

    @Override
    public SkillOutput execute(SkillInput input, SkillContext context) {
        String code = input.getString("code");
        String language = input.getString("language");

        if (code == null || code.isBlank()) {
            return SkillOutput.failure("No code provided for review");
        }

        String prompt = buildReviewPrompt(code, language);
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return SkillOutput.success(response, Map.of(
                "skill", "code-review",
                "language", language != null ? language : "unknown"
        ));
    }

    @Override
    public boolean canHandle(String userIntent) {
        String lower = userIntent.toLowerCase();
        return lower.contains("review") || lower.contains("check code")
                || lower.contains("code quality") || lower.contains("analyze code");
    }

    private String buildReviewPrompt(String code, String language) {
        StringBuilder sb = new StringBuilder();
        sb.append("Please review the following code");
        if (language != null) {
            sb.append(" (").append(language).append(")");
        }
        sb.append(" and provide:\n");
        sb.append("1. Potential bugs or issues\n");
        sb.append("2. Code style improvements\n");
        sb.append("3. Performance considerations\n");
        sb.append("4. Security concerns (if any)\n\n");
        sb.append("Code:\n```\n").append(code).append("\n```");
        return sb.toString();
    }
}
