package com.devmind.context.template;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Versioned prompt template management.
 * Templates use {{variable}} placeholders.
 */
@Component
public class PromptTemplateEngine {

    private final Map<String, Template> templates = new ConcurrentHashMap<>();

    public PromptTemplateEngine() {
        registerDefaults();
    }

    private void registerDefaults() {
        register("system-default", 1, """
                You are DevMind, an AI developer assistant.
                You help with code analysis, debugging, documentation, and task planning.
                Be concise, technical, and direct.

                {{user_preferences}}
                {{relevant_knowledge}}
                """);

        register("system-with-memory", 1, """
                You are DevMind, an AI developer assistant.
                You help with code analysis, debugging, documentation, and task planning.
                Be concise, technical, and direct.

                {{user_preferences}}
                {{relevant_knowledge}}
                {{episodic_context}}
                """);

        register("consolidation", 1, """
                Analyze this conversation and extract information in these categories.
                Return a JSON object with exactly these fields (use empty arrays if nothing found):

                {
                  "preferences": ["list of user preferences or interaction style notes"],
                  "facts": ["list of factual statements or knowledge revealed"],
                  "tasks": [{"description": "task description", "outcome": "SUCCESS|FAILURE|UNKNOWN", "learnings": "what was learned"}]
                }

                Rules:
                - Only extract NEW information, not obvious or generic statements
                - Be concise, one sentence per item
                - Return ONLY the JSON, no other text

                Conversation:
                {{conversation}}
                """);
    }

    public void register(String key, int version, String template) {
        templates.put(key, new Template(key, version, template));
    }

    public String render(String key, Map<String, String> variables) {
        Template template = templates.get(key);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + key);
        }
        String result = template.content();
        for (var entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    public Template getTemplate(String key) {
        return templates.get(key);
    }

    public Map<String, Template> getAllTemplates() {
        return Map.copyOf(templates);
    }

    public record Template(String key, int version, String content) {}
}
