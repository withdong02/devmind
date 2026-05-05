package com.devmind.memory.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "episodic_memories")
public class EpisodicMemoryEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "task_description", columnDefinition = "TEXT", nullable = false)
    private String taskDescription;

    @Column(name = "steps_taken", columnDefinition = "TEXT")
    private String stepsTaken;

    @Column(nullable = false, length = 32)
    private String outcome;

    @Column(columnDefinition = "TEXT")
    private String learnings;

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
    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
    public String getStepsTaken() { return stepsTaken; }
    public void setStepsTaken(String stepsTaken) { this.stepsTaken = stepsTaken; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getLearnings() { return learnings; }
    public void setLearnings(String learnings) { this.learnings = learnings; }
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
