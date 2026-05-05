package com.devmind.memory.impl;

import com.devmind.core.memory.Memory;
import com.devmind.core.memory.MemoryEntry;
import com.devmind.core.memory.MemoryQuery;
import com.devmind.core.memory.MemoryType;
import com.devmind.memory.entity.EpisodicMemoryEntity;
import com.devmind.memory.repository.EpisodicMemoryRepository;
import com.devmind.memory.service.MemoryEmbeddingHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EpisodicMemory implements Memory {

    private final EpisodicMemoryRepository repository;
    private final MemoryEmbeddingHelper embeddingHelper;

    public EpisodicMemory(EpisodicMemoryRepository repository, MemoryEmbeddingHelper embeddingHelper) {
        this.repository = repository;
        this.embeddingHelper = embeddingHelper;
    }

    @Override
    @Transactional
    public void store(MemoryEntry entry) {
        EpisodicMemoryEntity entity = new EpisodicMemoryEntity();
        entity.setId(entry.getId() != null ? entry.getId() : UUID.randomUUID().toString());
        entity.setUserId(entry.getUserId());
        entity.setTaskDescription(entry.getContent());
        entity.setOutcome(entry.getMetadata() != null && entry.getMetadata().containsKey("outcome")
                ? (String) entry.getMetadata().get("outcome") : "UNKNOWN");
        entity.setStepsTaken(entry.getMetadata() != null && entry.getMetadata().containsKey("stepsTaken")
                ? (String) entry.getMetadata().get("stepsTaken") : null);
        entity.setLearnings(entry.getMetadata() != null && entry.getMetadata().containsKey("learnings")
                ? (String) entry.getMetadata().get("learnings") : null);
        entity.setImportance(entry.getImportance());
        entity.setAccessCount(0);

        entity = repository.save(entity);

        String vector = embeddingHelper.embedToPgVector(entry.getContent());
        repository.updateEmbedding(entity.getId(), vector);
    }

    @Override
    public List<MemoryEntry> retrieve(MemoryQuery query) {
        if (query.text() != null && !query.text().isEmpty()) {
            String vector = embeddingHelper.embedToPgVector(query.text());
            return repository.findByEmbeddingSimilarity(query.userId(), vector, query.limit())
                    .stream()
                    .map(this::toEntry)
                    .collect(Collectors.toList());
        }
        return repository.findByUserIdOrderByCreatedAtDesc(query.userId())
                .stream()
                .limit(query.limit())
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void forget(String memoryId) {
        repository.deleteById(memoryId);
    }

    @Override
    public MemoryType getType() {
        return MemoryType.EPISODIC;
    }

    private MemoryEntry toEntry(EpisodicMemoryEntity entity) {
        MemoryEntry entry = MemoryEntry.of(entity.getUserId(), MemoryType.EPISODIC, entity.getTaskDescription());
        entry.setId(entity.getId());
        entry.setImportance(entity.getImportance());
        entry.setAccessCount(entity.getAccessCount());
        entry.setCreatedAt(entity.getCreatedAt());
        entry.setLastAccessed(entity.getLastAccessed());
        entry.setMetadata(Map.of(
                "outcome", entity.getOutcome() != null ? entity.getOutcome() : "UNKNOWN",
                "stepsTaken", entity.getStepsTaken() != null ? entity.getStepsTaken() : "",
                "learnings", entity.getLearnings() != null ? entity.getLearnings() : ""
        ));
        return entry;
    }
}
