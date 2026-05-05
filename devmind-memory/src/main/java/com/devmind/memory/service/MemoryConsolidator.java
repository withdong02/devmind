package com.devmind.memory.service;

import com.devmind.common.util.LlmJsonParser;
import com.devmind.core.memory.MemoryEntry;
import com.devmind.core.memory.MemoryType;
import com.devmind.memory.impl.EpisodicMemory;
import com.devmind.memory.impl.LongTermMemory;
import com.devmind.memory.impl.SemanticKnowledgeMemory;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Extracts and consolidates memories from conversation history using LLM.
 * Called when a session ends or periodically during long sessions.
 */
@Component
public class MemoryConsolidator {

    private final ChatClient chatClient;
    private final LongTermMemory longTermMemory;
    private final EpisodicMemory episodicMemory;
    private final SemanticKnowledgeMemory semanticMemory;

    public MemoryConsolidator(ChatClient.Builder chatClientBuilder,
                              LongTermMemory longTermMemory,
                              EpisodicMemory episodicMemory,
                              SemanticKnowledgeMemory semanticMemory) {
        this.chatClient = chatClientBuilder.build();
        this.longTermMemory = longTermMemory;
        this.episodicMemory = episodicMemory;
        this.semanticMemory = semanticMemory;
    }

    /**
     * Analyze conversation messages and extract memories.
     */
    public ConsolidationResult consolidate(String userId, List<String> messages) {
        if (messages.isEmpty()) {
            return new ConsolidationResult(0, 0, 0);
        }

        String conversation = String.join("\n", messages);

        String extractionPrompt = """
                Analyze this conversation and extract information in these categories.
                Return a JSON object with exactly these fields (use empty arrays if nothing found):

                {
                  "preferences": ["list of user preferences or interaction style notes"],
                  "facts": ["list of factual statements or knowledge revealed"],
                  "tasks": [{"description": "task description", "outcome": "SUCCESS|FAILURE|UNKNOWN", "learnings": "what was learned"}]
                }

                Rules:
                - Only extract NEW information, not obvious or generic statements
                - Preferences: coding style, tool choices, communication preferences
                - Facts: technical knowledge, project details, domain expertise
                - Tasks: specific actions taken and their outcomes
                - Be concise, one sentence per item
                - Return ONLY the JSON, no other text

                Conversation:
                """ + conversation;

        try {
            String response = chatClient.prompt()
                    .user(extractionPrompt)
                    .call()
                    .content();

            return parseAndStore(userId, response);
        } catch (Exception e) {
            return new ConsolidationResult(0, 0, 0);
        }
    }

    private ConsolidationResult parseAndStore(String userId, String json) {
        int preferences = 0, facts = 0, tasks = 0;
        String cleanJson = LlmJsonParser.cleanMarkdownFences(json);

        try {
            for (String pref : LlmJsonParser.extractStringArray(cleanJson, "preferences")) {
                MemoryEntry entry = MemoryEntry.of(userId, MemoryType.LONG_TERM, pref);
                entry.setImportance(0.6f);
                entry.setMetadata(Map.of("memoryType", "PREFERENCE"));
                longTermMemory.store(entry);
                preferences++;
            }

            for (String fact : LlmJsonParser.extractStringArray(cleanJson, "facts")) {
                MemoryEntry entry = MemoryEntry.of(userId, MemoryType.SEMANTIC, fact);
                entry.setImportance(0.5f);
                entry.setMetadata(Map.of("source", "conversation", "confidence", 0.7));
                semanticMemory.store(entry);
                facts++;
            }

            for (JsonNode taskNode : LlmJsonParser.extractObjectArray(cleanJson, "tasks")) {
                String desc = taskNode.has("description") ? taskNode.get("description").asText() : null;
                if (desc != null && !desc.isEmpty()) {
                    String outcome = taskNode.has("outcome") ? taskNode.get("outcome").asText() : "UNKNOWN";
                    String learnings = taskNode.has("learnings") ? taskNode.get("learnings").asText() : "";
                    MemoryEntry entry = MemoryEntry.of(userId, MemoryType.EPISODIC, desc);
                    entry.setImportance(0.5f);
                    entry.setMetadata(Map.of("outcome", outcome, "learnings", learnings));
                    episodicMemory.store(entry);
                    tasks++;
                }
            }
        } catch (Exception ignored) {
        }

        return new ConsolidationResult(preferences, facts, tasks);
    }

    public record ConsolidationResult(int preferencesExtracted, int factsExtracted, int tasksExtracted) {}
}
