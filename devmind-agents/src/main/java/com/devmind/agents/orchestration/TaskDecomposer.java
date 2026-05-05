package com.devmind.agents.orchestration;

import com.devmind.common.util.LlmJsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses LLM to decompose complex user requests into subtasks,
 * each assigned to a specialist agent.
 */
@Component
public class TaskDecomposer {

    private final ChatClient chatClient;

    public TaskDecomposer(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public List<SubTask> decompose(String userRequest) {
        String prompt = """
                Analyze this user request and break it into subtasks.
                Each subtask should be assigned to one of these agents:
                - CODE: code analysis, debugging, code review, refactoring
                - RESEARCH: documentation lookup, knowledge search, best practices
                - PLANNING: task breakdown, project planning, architecture design
                - DOCUMENTATION: generating docs, README, comments, API docs

                Return a JSON array of objects with "description" and "agent" fields.
                If the request is simple enough for one agent, return a single subtask.
                Return ONLY the JSON array, no other text.

                Example:
                [{"description": "Analyze the code for bugs", "agent": "CODE"}, {"description": "Generate documentation for the fixed code", "agent": "DOCUMENTATION"}]

                User request: """ + userRequest;

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            return parseSubTasks(response);
        } catch (Exception e) {
            List<SubTask> fallback = new ArrayList<>();
            fallback.add(new SubTask(userRequest, "CODE"));
            return fallback;
        }
    }

    private List<SubTask> parseSubTasks(String json) {
        List<SubTask> tasks = new ArrayList<>();
        List<JsonNode> items = LlmJsonParser.parseArray(json);

        for (JsonNode item : items) {
            String desc = item.has("description") ? item.get("description").asText() : null;
            String agent = item.has("agent") ? item.get("agent").asText() : null;
            if (desc != null && agent != null) {
                tasks.add(new SubTask(desc, agent.toUpperCase()));
            }
        }

        if (tasks.isEmpty()) {
            String clean = LlmJsonParser.cleanMarkdownFences(json);
            tasks.add(new SubTask(clean, "CODE"));
        }
        return tasks;
    }

    public record SubTask(String description, String agentRole) {}
}
