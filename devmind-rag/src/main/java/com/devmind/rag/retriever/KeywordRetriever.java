package com.devmind.rag.retriever;

import com.devmind.core.rag.RetrievalResult;
import com.devmind.rag.entity.DocumentChunkEntity;
import com.devmind.rag.repository.DocumentChunkRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeywordRetriever {

    private final DocumentChunkRepository chunkRepository;

    public KeywordRetriever(DocumentChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    public List<RetrievalResult> retrieve(String query, int limit) {
        List<DocumentChunkEntity> results = chunkRepository.findByFullTextSearch(query, limit);

        return results.stream()
            .map(this::toRetrievalResult)
            .toList();
    }

    private RetrievalResult toRetrievalResult(DocumentChunkEntity entity) {
        com.devmind.core.rag.DocumentChunk chunk = new com.devmind.core.rag.DocumentChunk();
        chunk.setId(entity.getId());
        chunk.setDocumentId(entity.getDocument().getId());
        chunk.setChunkIndex(entity.getChunkIndex());
        chunk.setContent(entity.getContent());
        chunk.setChunkType(com.devmind.core.rag.DocumentChunk.ChunkType.valueOf(entity.getChunkType().name()));
        chunk.setStartLine(entity.getStartLine());
        chunk.setEndLine(entity.getEndLine());
        chunk.setSymbolName(entity.getSymbolName());

        return new RetrievalResult(chunk, 0.0, "keyword");
    }
}
