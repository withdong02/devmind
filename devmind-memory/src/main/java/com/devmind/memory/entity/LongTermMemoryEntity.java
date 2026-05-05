package com.devmind.memory.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "long_term_memories")
public class LongTermMemoryEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "memory_type", nullable = false, length = 32)
    private String memoryType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private float importance;

    @Column(name = "access_count", nullable = false)
    private int accessCount;

    @Column(name = "embedding", insertable = false, updatable = false)
    private String embedding;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_accessed")
    private Instant lastAccessed;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getMemoryType() { return memoryType; }
    public void setMemoryType(String memoryType) { this.memoryType = memoryType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public float getImportance() { return importance; }
    public void setImportance(float importance) { this.importance = importance; }
    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }
    public String getEmbedding() { return embedding; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(Instant lastAccessed) { this.lastAccessed = lastAccessed; }
}
