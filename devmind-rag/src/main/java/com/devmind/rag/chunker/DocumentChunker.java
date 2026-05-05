package com.devmind.rag.chunker;

import com.devmind.rag.entity.DocumentChunkEntity;

import java.util.List;

public interface DocumentChunker {

    List<ChunkResult> chunk(String content, String language, String documentId);

    boolean supports(String language);

    record ChunkResult(
        String content,
        DocumentChunkEntity.ChunkType chunkType,
        int startLine,
        int endLine,
        String symbolName
    ) {}
}
