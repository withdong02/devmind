package com.devmind.skills.impl.taskplanner;

import com.devmind.core.skill.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TaskPlannerSkill implements Skill {

    private final ChatClient chatClient;

    public TaskPlannerSkill(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "task-planner",
                "Task Planner",
                "Decomposes complex development tasks into actionable steps with dependencies and estimates",
                Map.of("task", "string", "context", "string"),
                List.of("plan", "task", "decompose", "breakdown", "steps")
        );
    }

    @Override
    public SkillOutput execute(SkillInput input, SkillContext context) {
        String task = input.getString("task");
        if (task == null || task.isBlank()) {
            return SkillOutput.failure("No task description provided");
        }

        String prompt = buildPlannerPrompt(task);
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return SkillOutput.success(response, Map.of("skill", "task-planner"));
    }

    @Override
    public boolean canHandle(String userIntent) {
        String lower = userIntent.toLowerCase();
        return lower.contains("plan") || lower.contains("decompose")
                || lower.contains("break down") || lower.contains("steps")
                || lower.contains("how to implement");
    }

    private String buildPlannerPrompt(String task) {
        return "Decompose the following development task into actionable steps:\n\n" +
                "Task: " + task + "\n\n" +
                "For each step, provide:\n" +
                "1. Clear description\n" +
                "2. Dependencies on other steps\n" +
                "3. Estimated effort (hours)\n" +
                "4. Key considerations or risks\n\n" +
                "Format as a numbered list with sub-items.";
    }
}
