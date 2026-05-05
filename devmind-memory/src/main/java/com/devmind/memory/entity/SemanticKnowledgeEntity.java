package com.devmind.memory.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "semantic_knowledge")
public class SemanticKnowledgeEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String fact;

    @Column(length = 512)
    private String source;

    @Column(nullable = false)
    private float confidence;

    @Column(name = "embedding", insertable = false, updatable = false)
    private String embedding;

    @Column(nullable = false)
    private float importance;

    @Column(name = "access_count", nullable = false)
    private int accessCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_accessed")
    private Instant lastAccessed;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFact() { return fact; }
    public void setFact(String fact) { this.fact = fact; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }
    public String getEmbedding() { return embedding; }
    public float getImportance() { return importance; }
    public void setImportance(float importance) { this.importance = importance; }
    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(Instant lastAccessed) { this.lastAccessed = lastAccessed; }
}
