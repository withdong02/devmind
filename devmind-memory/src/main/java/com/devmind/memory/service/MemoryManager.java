package com.devmind.memory.service;

import com.devmind.core.memory.MemoryEntry;
import com.devmind.core.memory.MemoryQuery;
import com.devmind.core.memory.MemoryType;
import com.devmind.memory.entity.EpisodicMemoryEntity;
import com.devmind.memory.entity.LongTermMemoryEntity;
import com.devmind.memory.entity.SemanticKnowledgeEntity;
import com.devmind.memory.impl.EpisodicMemory;
import com.devmind.memory.impl.LongTermMemory;
import com.devmind.memory.impl.ShortTermMemory;
import com.devmind.memory.impl.SemanticKnowledgeMemory;
import com.devmind.memory.repository.EpisodicMemoryRepository;
import com.devmind.memory.repository.LongTermMemoryRepository;
import com.devmind.memory.repository.SemanticKnowledgeRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MemoryManager {

    private final ShortTermMemory shortTermMemory;
    private final LongTermMemory longTermMemory;
    private final EpisodicMemory episodicMemory;
    private final SemanticKnowledgeMemory semanticMemory;
    private final LongTermMemoryRepository ltmRepository;
    private final EpisodicMemoryRepository emRepository;
    private final SemanticKnowledgeRepository skRepository;
    private final MemoryConsolidator consolidator;

    public MemoryManager(ShortTermMemory shortTermMemory,
                         LongTermMemory longTermMemory,
                         EpisodicMemory episodicMemory,
                         SemanticKnowledgeMemory semanticMemory,
                         LongTermMemoryRepository ltmRepository,
                         EpisodicMemoryRepository emRepository,
                         SemanticKnowledgeRepository skRepository,
                         MemoryConsolidator consolidator) {
        this.shortTermMemory = shortTermMemory;
        this.longTermMemory = longTermMemory;
        this.episodicMemory = episodicMemory;
        this.semanticMemory = semanticMemory;
        this.ltmRepository = ltmRepository;
        this.emRepository = emRepository;
        this.skRepository = skRepository;
        this.consolidator = consolidator;
    }

    public void storeShortTerm(String userId, String content) {
        MemoryEntry entry = MemoryEntry.of(userId, MemoryType.SHORT_TERM, content);
        shortTermMemory.store(entry);
    }

    public void storeLongTerm(String userId, String content, String memoryType) {
        MemoryEntry entry = MemoryEntry.of(userId, MemoryType.LONG_TERM, content);
        entry.setMetadata(Map.of("memoryType", memoryType));
        longTermMemory.store(entry);
    }

    public void storeEpisodic(String userId, String description, String outcome, String stepsTaken, String learnings) {
        MemoryEntry entry = MemoryEntry.of(userId, MemoryType.EPISODIC, description);
        entry.setMetadata(Map.of(
                "outcome", outcome,
                "stepsTaken", stepsTaken != null ? stepsTaken : "",
                "learnings", learnings != null ? learnings : ""
        ));
        episodicMemory.store(entry);
    }

    public void storeSemantic(String userId, String fact, String source, float confidence) {
        MemoryEntry entry = MemoryEntry.of(userId, MemoryType.SEMANTIC, fact);
        entry.setMetadata(Map.of(
                "source", source != null ? source : "manual",
                "confidence", confidence
        ));
        semanticMemory.store(entry);
    }

    public List<MemoryEntry> retrieveAll(String userId, String query, int limit) {
        List<MemoryEntry> results = new ArrayList<>();
        MemoryQuery mq = MemoryQuery.of(userId, query);

        // Retrieve from all memory types, limit each to avoid overwhelming context
        int perTypeLimit = Math.max(limit / 3, 3);

        results.addAll(longTermMemory.retrieve(new MemoryQuery(userId, query, MemoryType.LONG_TERM, perTypeLimit, Map.of())));
        results.addAll(episodicMemory.retrieve(new MemoryQuery(userId, query, MemoryType.EPISODIC, perTypeLimit, Map.of())));
        results.addAll(semanticMemory.retrieve(new MemoryQuery(userId, query, MemoryType.SEMANTIC, perTypeLimit, Map.of())));

        // Sort by importance * relevance, take top N
        results.sort((a, b) -> Float.compare(b.getImportance(), a.getImportance()));
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }
        return results;
    }

    public List<MemoryEntry> retrieveByType(String userId, String query, MemoryType type, int limit) {
        return switch (type) {
            case SHORT_TERM -> shortTermMemory.retrieve(MemoryQuery.of(userId, query, type));
            case LONG_TERM -> longTermMemory.retrieve(new MemoryQuery(userId, query, type, limit, Map.of()));
            case EPISODIC -> episodicMemory.retrieve(new MemoryQuery(userId, query, type, limit, Map.of()));
            case SEMANTIC -> semanticMemory.retrieve(new MemoryQuery(userId, query, type, limit, Map.of()));
        };
    }

    public void forget(String memoryId, MemoryType type) {
        switch (type) {
            case SHORT_TERM -> shortTermMemory.forget(memoryId);
            case LONG_TERM -> longTermMemory.forget(memoryId);
            case EPISODIC -> episodicMemory.forget(memoryId);
            case SEMANTIC -> semanticMemory.forget(memoryId);
        }
    }

    public MemoryConsolidator.ConsolidationResult consolidateSession(String userId, List<String> messages) {
        return consolidator.consolidate(userId, messages);
    }

    public Map<String, Object> getStats(String userId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("shortTermCount", shortTermMemory.retrieve(MemoryQuery.of(userId, "")).size());
        stats.put("longTermCount", ltmRepository.countByUserId(userId));
        stats.put("episodicCount", emRepository.countByUserId(userId));
        stats.put("semanticCount", skRepository.countByUserId(userId));
        return stats;
    }

    public List<Map<String, Object>> listAll(String userId, MemoryType type, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (type == null || type == MemoryType.LONG_TERM) {
            ltmRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().limit(limit).forEach(e -> {
                result.add(Map.of("id", e.getId(), "type", "LONG_TERM", "content", e.getContent(),
                        "memoryType", e.getMemoryType(), "importance", e.getImportance(),
                        "createdAt", e.getCreatedAt().toString()));
            });
        }
        if (type == null || type == MemoryType.EPISODIC) {
            emRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().limit(limit).forEach(e -> {
                result.add(Map.of("id", e.getId(), "type", "EPISODIC", "content", e.getTaskDescription(),
                        "outcome", e.getOutcome() != null ? e.getOutcome() : "UNKNOWN",
                        "importance", e.getImportance(),
                        "createdAt", e.getCreatedAt().toString()));
            });
        }
        if (type == null || type == MemoryType.SEMANTIC) {
            skRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().limit(limit).forEach(e -> {
                result.add(Map.of("id", e.getId(), "type", "SEMANTIC", "content", e.getFact(),
                        "source", e.getSource() != null ? e.getSource() : "",
                        "confidence", e.getConfidence(),
                        "importance", e.getImportance(),
                        "createdAt", e.getCreatedAt().toString()));
            });
        }
        return result;
    }
}
