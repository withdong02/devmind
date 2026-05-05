package com.devmind.skills.impl.codesearch;

import com.devmind.core.skill.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CodeSearchSkill implements Skill {

    private final ChatClient chatClient;

    public CodeSearchSkill(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "code-search",
                "Code Search",
                "Searches code repositories using semantic understanding to find relevant code snippets, functions, and patterns",
                Map.of("query", "string", "language", "string", "path", "string"),
                List.of("search", "find", "code", "lookup", "grep", "semantic")
        );
    }

    @Override
    public SkillOutput execute(SkillInput input, SkillContext context) {
        String query = input.getString("query");
        if (query == null || query.isBlank()) {
            return SkillOutput.failure("No search query provided");
        }

        String language = input.getString("language");
        String path = input.getString("path");

        String prompt = buildSearchPrompt(query, language, path);
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return SkillOutput.success(response, Map.of(
                "skill", "code-search",
                "query", query
        ));
    }

    @Override
    public boolean canHandle(String userIntent) {
        String lower = userIntent.toLowerCase();
        return lower.contains("search") || lower.contains("find code")
                || lower.contains("where is") || lower.contains("locate")
                || lower.contains("grep") || lower.contains("look for");
    }

    private String buildSearchPrompt(String query, String language, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("Help me find code related to: ").append(query).append("\n\n");
        if (language != null) {
            sb.append("Programming language: ").append(language).append("\n");
        }
        if (path != null) {
            sb.append("Search in: ").append(path).append("\n");
        }
        sb.append("\nPlease describe what patterns, functions, or code structures would match this query, ");
        sb.append("and suggest search strategies (grep patterns, file types to look for, etc.).");
        return sb.toString();
    }
}
