package com.devmind.core.memory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class MemoryEntry {

    private String id;
    private String userId;
    private MemoryType type;
    private String content;
    private float importance;
    private int accessCount;
    private Instant createdAt;
    private Instant lastAccessed;
    private Map<String, Object> metadata;

    public MemoryEntry() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.importance = 0.5f;
        this.accessCount = 0;
    }

    public static MemoryEntry of(String userId, MemoryType type, String content) {
        MemoryEntry entry = new MemoryEntry();
        entry.setUserId(userId);
        entry.setType(type);
        entry.setContent(content);
        return entry;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public MemoryType getType() { return type; }
    public void setType(MemoryType type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public float getImportance() { return importance; }
    public void setImportance(float importance) { this.importance = importance; }
    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(Instant lastAccessed) { this.lastAccessed = lastAccessed; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
