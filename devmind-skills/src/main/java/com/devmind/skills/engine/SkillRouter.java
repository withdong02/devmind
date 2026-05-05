package com.devmind.skills.engine;

import com.devmind.core.skill.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Routes user intent to the appropriate skill.
 * First tries keyword-based matching via canHandle(), then falls back to LLM-based routing.
 */
@Component
public class SkillRouter {

    private final SkillRegistry skillRegistry;
    private final ChatClient chatClient;

    public SkillRouter(SkillRegistry skillRegistry, ChatClient.Builder chatClientBuilder) {
        this.skillRegistry = skillRegistry;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Finds the best skill for the given user message.
     * Returns empty if no skill matches (meaning general chat should handle it).
     */
    public Optional<Skill> route(String userMessage) {
        // Phase 1: keyword-based matching
        List<Skill> candidates = skillRegistry.getAllSkills().stream()
                .filter(s -> s.canHandle(userMessage))
                .toList();

        if (candidates.size() == 1) {
            return Optional.of(candidates.get(0));
        }

        if (candidates.size() > 1) {
            // Multiple matches - use LLM to disambiguate
            return disambiguateWithLlm(userMessage, candidates);
        }

        // No keyword match - use LLM to check if any skill applies
        return llmRoute(userMessage);
    }

    private Optional<Skill> disambiguateWithLlm(String userMessage, List<Skill> candidates) {
        String skillDescriptions = candidates.stream()
                .map(s -> "- " + s.definition().id() + ": " + s.definition().description())
                .reduce("", (a, b) -> a + "\n" + b);

        String prompt = """
                Given this user message: "%s"

                Which of these skills is most appropriate?
                %s

                Reply with ONLY the skill id (e.g. "code-review"). If none apply, reply "none".
                """.formatted(userMessage, skillDescriptions);

        String result = chatClient.prompt().user(prompt).call().content().strip();

        if ("none".equalsIgnoreCase(result)) {
            return Optional.empty();
        }

        return skillRegistry.getSkillById(result);
    }

    private Optional<Skill> llmRoute(String userMessage) {
        String skillDescriptions = skillRegistry.getAllSkills().stream()
                .map(s -> "- " + s.definition().id() + ": " + s.definition().description())
                .reduce("", (a, b) -> a + "\n" + b);

        String prompt = """
                Given this user message: "%s"

                Available skills:
                %s

                Does any skill match this request? Reply with ONLY the skill id, or "none" if this is a general conversation.
                """.formatted(userMessage, skillDescriptions);

        String result = chatClient.prompt().user(prompt).call().content().strip();

        if ("none".equalsIgnoreCase(result)) {
            return Optional.empty();
        }

        return skillRegistry.getSkillById(result);
    }
}
