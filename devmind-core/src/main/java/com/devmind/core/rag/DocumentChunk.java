package com.devmind.core.rag;

import java.util.Map;
import java.util.UUID;

public class DocumentChunk {

    private String id;
    private String documentId;
    private int chunkIndex;
    private String content;
    private ChunkType chunkType;
    private int startLine;
    private int endLine;
    private String symbolName;
    private float[] embedding;
    private int tokenCount;
    private Map<String, Object> metadata;

    public DocumentChunk() {
        this.id = UUID.randomUUID().toString();
    }

    public enum ChunkType {
        CLASS, METHOD, FUNCTION, BLOCK, TEXT
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public ChunkType getChunkType() { return chunkType; }
    public void setChunkType(ChunkType chunkType) { this.chunkType = chunkType; }
    public int getStartLine() { return startLine; }
    public void setStartLine(int startLine) { this.startLine = startLine; }
    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }
    public String getSymbolName() { return symbolName; }
    public void setSymbolName(String symbolName) { this.symbolName = symbolName; }
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
    public int getTokenCount() { return tokenCount; }
    public void setTokenCount(int tokenCount) { this.tokenCount = tokenCount; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
