package com.devmind.skills.impl.docgen;

import com.devmind.core.skill.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DocumentationGeneratorSkill implements Skill {

    private final ChatClient chatClient;

    public DocumentationGeneratorSkill(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "doc-generator",
                "Documentation Generator",
                "Generates documentation including Javadoc, README, API docs, and inline comments",
                Map.of("code", "string", "docType", "string"),
                List.of("documentation", "javadoc", "readme", "comments", "docs")
        );
    }

    @Override
    public SkillOutput execute(SkillInput input, SkillContext context) {
        String code = input.getString("code");
        String docType = input.getString("docType");

        if (code == null || code.isBlank()) {
            return SkillOutput.failure("No code provided for documentation");
        }

        String prompt = buildDocPrompt(code, docType);
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return SkillOutput.success(response, Map.of("skill", "doc-generator", "docType", docType));
    }

    @Override
    public boolean canHandle(String userIntent) {
        String lower = userIntent.toLowerCase();
        return lower.contains("document") || lower.contains("javadoc")
                || lower.contains("readme") || lower.contains("generate docs")
                || lower.contains("add comments");
    }

    private String buildDocPrompt(String code, String docType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate ");
        if (docType != null) {
            sb.append(docType).append(" ");
        }
        sb.append("documentation for the following code:\n\n");
        sb.append("```\n").append(code).append("\n```\n\n");
        sb.append("Include:\n");
        sb.append("- Class/method descriptions\n");
        sb.append("- Parameter descriptions\n");
        sb.append("- Return value description\n");
        sb.append("- Usage examples where appropriate\n");
        return sb.toString();
    }
}
