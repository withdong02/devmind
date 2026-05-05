package com.devmind.memory.impl;

import com.devmind.core.memory.Memory;
import com.devmind.core.memory.MemoryEntry;
import com.devmind.core.memory.MemoryQuery;
import com.devmind.core.memory.MemoryType;
import com.devmind.memory.entity.SemanticKnowledgeEntity;
import com.devmind.memory.repository.SemanticKnowledgeRepository;
import com.devmind.memory.service.MemoryEmbeddingHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SemanticKnowledgeMemory implements Memory {

    private final SemanticKnowledgeRepository repository;
    private final MemoryEmbeddingHelper embeddingHelper;

    public SemanticKnowledgeMemory(SemanticKnowledgeRepository repository, MemoryEmbeddingHelper embeddingHelper) {
        this.repository = repository;
        this.embeddingHelper = embeddingHelper;
    }

    @Override
    @Transactional
    public void store(MemoryEntry entry) {
        SemanticKnowledgeEntity entity = new SemanticKnowledgeEntity();
        entity.setId(entry.getId() != null ? entry.getId() : UUID.randomUUID().toString());
        entity.setUserId(entry.getUserId());
        entity.setFact(entry.getContent());
        entity.setSource(entry.getMetadata() != null && entry.getMetadata().containsKey("source")
                ? (String) entry.getMetadata().get("source") : null);
        entity.setConfidence(entry.getMetadata() != null && entry.getMetadata().containsKey("confidence")
                ? ((Number) entry.getMetadata().get("confidence")).floatValue() : 0.5f);
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
        return MemoryType.SEMANTIC;
    }

    private MemoryEntry toEntry(SemanticKnowledgeEntity entity) {
        MemoryEntry entry = MemoryEntry.of(entity.getUserId(), MemoryType.SEMANTIC, entity.getFact());
        entry.setId(entity.getId());
        entry.setImportance(entity.getImportance());
        entry.setAccessCount(entity.getAccessCount());
        entry.setCreatedAt(entity.getCreatedAt());
        entry.setLastAccessed(entity.getLastAccessed());
        entry.setMetadata(Map.of(
                "source", entity.getSource() != null ? entity.getSource() : "",
                "confidence", entity.getConfidence()
        ));
        return entry;
    }
}
