package com.devmind.memory.impl;

import com.devmind.core.memory.Memory;
import com.devmind.core.memory.MemoryEntry;
import com.devmind.core.memory.MemoryQuery;
import com.devmind.core.memory.MemoryType;
import com.devmind.memory.entity.LongTermMemoryEntity;
import com.devmind.memory.repository.LongTermMemoryRepository;
import com.devmind.memory.service.MemoryEmbeddingHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LongTermMemory implements Memory {

    private final LongTermMemoryRepository repository;
    private final MemoryEmbeddingHelper embeddingHelper;

    public LongTermMemory(LongTermMemoryRepository repository, MemoryEmbeddingHelper embeddingHelper) {
        this.repository = repository;
        this.embeddingHelper = embeddingHelper;
    }

    @Override
    @Transactional
    public void store(MemoryEntry entry) {
        LongTermMemoryEntity entity = new LongTermMemoryEntity();
        entity.setId(entry.getId() != null ? entry.getId() : UUID.randomUUID().toString());
        entity.setUserId(entry.getUserId());
        entity.setMemoryType(entry.getMetadata() != null && entry.getMetadata().containsKey("memoryType")
                ? (String) entry.getMetadata().get("memoryType") : "PREFERENCE");
        entity.setContent(entry.getContent());
        entity.setImportance(entry.getImportance());
        entity.setAccessCount(0);
        entity.setCreatedAt(entry.getCreatedAt() != null ? entry.getCreatedAt() : java.time.Instant.now());

        entity = repository.save(entity);

        // Update embedding via native query
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
        return MemoryType.LONG_TERM;
    }

    @Transactional
    public void incrementAccess(String memoryId) {
        repository.incrementAccessCount(memoryId);
    }

    private MemoryEntry toEntry(LongTermMemoryEntity entity) {
        MemoryEntry entry = MemoryEntry.of(entity.getUserId(), MemoryType.LONG_TERM, entity.getContent());
        entry.setId(entity.getId());
        entry.setImportance(entity.getImportance());
        entry.setAccessCount(entity.getAccessCount());
        entry.setCreatedAt(entity.getCreatedAt());
        entry.setLastAccessed(entity.getLastAccessed());
        return entry;
    }
}
