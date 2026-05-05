package com.devmind.rag.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "document_chunks")
public class DocumentChunkEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "chunk_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ChunkType chunkType;

    @Column(name = "start_line")
    private int startLine;

    @Column(name = "end_line")
    private int endLine;

    @Column(name = "symbol_name", length = 256)
    private String symbolName;

    @Column(name = "embedding", insertable = false, updatable = false)
    private String embedding;

    @Column(name = "token_count")
    private int tokenCount;

    @Column(insertable = false, updatable = false)
    private String searchVector;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    public enum ChunkType {
        CLASS, METHOD, FUNCTION, BLOCK, TEXT
    }

    public DocumentChunkEntity() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public DocumentEntity getDocument() { return document; }
    public void setDocument(DocumentEntity document) { this.document = document; }
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
    public String getEmbedding() { return embedding; }
    public void setEmbedding(String embedding) { this.embedding = embedding; }
    public int getTokenCount() { return tokenCount; }
    public void setTokenCount(int tokenCount) { this.tokenCount = tokenCount; }
    public String getSearchVector() { return searchVector; }
    public void setSearchVector(String searchVector) { this.searchVector = searchVector; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
}
