package com.devmind.skills.impl.gitassistant;

import com.devmind.core.skill.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GitAssistantSkill implements Skill {

    private final ChatClient chatClient;

    public GitAssistantSkill(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "git-assistant",
                "Git Assistant",
                "Performs git operations: log, diff, blame, and generates commit messages",
                Map.of("command", "string", "path", "string"),
                List.of("git", "commit", "diff", "log", "blame", "version-control")
        );
    }

    @Override
    public SkillOutput execute(SkillInput input, SkillContext context) {
        String command = input.getString("command");
        String path = input.getString("path");

        if (command == null || command.isBlank()) {
            return SkillOutput.failure("No git command specified");
        }

        try {
            String result = executeGitCommand(command, path);

            // If it's a diff or log, optionally summarize with LLM
            if (command.startsWith("diff") || command.startsWith("log")) {
                String summary = chatClient.prompt()
                        .user("Summarize this git " + command.split(" ")[0] + " output:\n```\n" + result + "\n```")
                        .call()
                        .content();
                return SkillOutput.success(summary, Map.of("skill", "git-assistant", "rawOutput", result));
            }

            return SkillOutput.success(result, Map.of("skill", "git-assistant"));
        } catch (Exception e) {
            return SkillOutput.failure("Git command failed: " + e.getMessage());
        }
    }

    @Override
    public boolean canHandle(String userIntent) {
        String lower = userIntent.toLowerCase();
        return lower.contains("git") || lower.contains("commit message")
                || lower.contains("diff") || lower.contains("blame")
                || lower.contains("changelog");
    }

    private String executeGitCommand(String command, String path) throws Exception {
        String workingDir = path != null ? path : ".";
        String[] parts = command.split("\\s+");
        String[] fullCommand = new String[parts.length + 1];
        fullCommand[0] = "git";
        System.arraycopy(parts, 0, fullCommand, 1, parts.length);
        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(new File(workingDir));
        pb.command(fullCommand);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = reader.lines().collect(Collectors.joining("\n"));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Git exited with code " + exitCode + ": " + output);
            }
            return output;
        }
    }
}
