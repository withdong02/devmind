package com.devmind.api.controller;

import com.devmind.core.memory.MemoryType;
import com.devmind.memory.service.MemoryManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Memory", description = "Memory system: short-term, long-term, episodic, semantic")
@RestController
@RequestMapping("/api/v1/memory")
public class MemoryController {

    private final MemoryManager memoryManager;

    public MemoryController(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }

    @Operation(summary = "Get memory statistics for a user")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestParam(defaultValue = "user") String userId) {
        return ResponseEntity.ok(memoryManager.getStats(userId));
    }

    @Operation(summary = "List all memories for a user")
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listMemories(
            @RequestParam(defaultValue = "user") String userId,
            @RequestParam(required = false) MemoryType type,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(memoryManager.listAll(userId, type, limit));
    }

    @Operation(summary = "Search memories by semantic similarity")
    @PostMapping("/search")
    public ResponseEntity<?> searchMemories(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "user");
        String query = (String) body.getOrDefault("query", "");
        String typeStr = (String) body.get("type");
        int limit = body.containsKey("limit") ? ((Number) body.get("limit")).intValue() : 10;

        if (typeStr != null && !typeStr.isEmpty()) {
            MemoryType type = MemoryType.valueOf(typeStr);
            return ResponseEntity.ok(memoryManager.retrieveByType(userId, query, type, limit));
        }
        return ResponseEntity.ok(memoryManager.retrieveAll(userId, query, limit));
    }

    @Operation(summary = "Store a new memory entry")
    @PostMapping("/store")
    public ResponseEntity<Map<String, String>> storeMemory(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "user");
        String type = (String) body.getOrDefault("type", "LONG_TERM");
        String content = (String) body.get("content");

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "content is required"));
        }

        switch (MemoryType.valueOf(type)) {
            case LONG_TERM -> memoryManager.storeLongTerm(userId, content,
                    (String) body.getOrDefault("memoryType", "PREFERENCE"));
            case EPISODIC -> memoryManager.storeEpisodic(userId, content,
                    (String) body.getOrDefault("outcome", "UNKNOWN"),
                    (String) body.get("stepsTaken"),
                    (String) body.get("learnings"));
            case SEMANTIC -> memoryManager.storeSemantic(userId, content,
                    (String) body.get("source"),
                    body.containsKey("confidence") ? ((Number) body.get("confidence")).floatValue() : 0.5f);
            case SHORT_TERM -> memoryManager.storeShortTerm(userId, content);
        }

        return ResponseEntity.ok(Map.of("status", "stored", "type", type));
    }

    @Operation(summary = "Delete a memory by type and ID")
    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<Map<String, String>> forgetMemory(
            @PathVariable MemoryType type,
            @PathVariable String id) {
        memoryManager.forget(id, type);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    @Operation(summary = "Consolidate session messages into long-term memories")
    @PostMapping("/consolidate")
    public ResponseEntity<?> consolidate(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "user");
        @SuppressWarnings("unchecked")
        List<String> messages = (List<String>) body.get("messages");
        if (messages == null || messages.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "messages required"));
        }
        var result = memoryManager.consolidateSession(userId, messages);
        return ResponseEntity.ok(Map.of(
                "preferencesExtracted", result.preferencesExtracted(),
                "factsExtracted", result.factsExtracted(),
                "tasksExtracted", result.tasksExtracted()
        ));
    }
}
