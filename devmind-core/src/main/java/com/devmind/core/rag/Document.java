package com.devmind.core.rag;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Document {

    private String id;
    private SourceType sourceType;
    private String sourcePath;
    private String title;
    private String content;
    private String language;
    private String fileHash;
    private Map<String, Object> metadata;
    private Instant createdAt;

    public Document() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public enum SourceType {
        FILE, REPO, URL, MANUAL
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
