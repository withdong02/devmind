package com.devmind.rag.service;

import com.devmind.core.rag.RetrievalResult;
import com.devmind.rag.chunker.AstAwareChunker;
import com.devmind.rag.chunker.DocumentChunker;
import com.devmind.rag.chunker.TokenTextChunker;
import com.devmind.rag.embedder.EmbeddingService;
import com.devmind.rag.entity.DocumentChunkEntity;
import com.devmind.rag.entity.DocumentEntity;
import com.devmind.rag.loader.FileSystemLoader;
import com.devmind.rag.loader.GitRepositoryLoader;
import com.devmind.rag.retriever.HybridRetriever;
import com.devmind.rag.repository.DocumentChunkRepository;
import com.devmind.rag.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final FileSystemLoader fileSystemLoader;
    private final GitRepositoryLoader gitRepositoryLoader;
    private final AstAwareChunker astChunker;
    private final TokenTextChunker textChunker;
    private final EmbeddingService embeddingService;
    private final HybridRetriever hybridRetriever;

    public RagService(DocumentRepository documentRepository,
                      DocumentChunkRepository chunkRepository,
                      FileSystemLoader fileSystemLoader,
                      GitRepositoryLoader gitRepositoryLoader,
                      AstAwareChunker astChunker,
                      TokenTextChunker textChunker,
                      EmbeddingService embeddingService,
                      HybridRetriever hybridRetriever) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.fileSystemLoader = fileSystemLoader;
        this.gitRepositoryLoader = gitRepositoryLoader;
        this.astChunker = astChunker;
        this.textChunker = textChunker;
        this.embeddingService = embeddingService;
        this.hybridRetriever = hybridRetriever;
    }

    @Transactional
    public IndexResult indexDirectory(String directoryPath) {
        Path path = Path.of(directoryPath);
        var loader = gitRepositoryLoader.supports(path) ? gitRepositoryLoader : fileSystemLoader;
        var loadResults = loader.load(path);

        int docsIndexed = 0;
        int chunksCreated = 0;

        for (var loadResult : loadResults) {
            String fileHash = sha256(loadResult.content());

            // Skip if already indexed with same content
            if (documentRepository.existsByFileHash(fileHash)) {
                log.debug("Skipping unchanged file: {}", loadResult.sourcePath());
                continue;
            }

            // Remove old version if exists
            documentRepository.findBySourcePath(loadResult.sourcePath()).ifPresent(old -> {
                chunkRepository.deleteByDocumentId(old.getId());
                documentRepository.delete(old);
            });

            // Create document
            DocumentEntity doc = new DocumentEntity();
            doc.setId(UUID.randomUUID().toString());
            doc.setSourceType(loadResult.sourceType());
            doc.setSourcePath(loadResult.sourcePath());
            doc.setTitle(loadResult.title());
            doc.setContent(loadResult.content());
            doc.setLanguage(loadResult.language());
            doc.setFileHash(fileHash);
            documentRepository.save(doc);

            // Chunk
            List<DocumentChunker.ChunkResult> chunks = chunkDocument(loadResult.content(), loadResult.language(), doc.getId());

            // Embed and save chunks
            List<String> chunkTexts = chunks.stream().map(DocumentChunker.ChunkResult::content).toList();
            List<float[]> embeddings = embeddingService.embedBatch(chunkTexts);

            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunker.ChunkResult cr = chunks.get(i);
                DocumentChunkEntity entity = new DocumentChunkEntity();
                entity.setId(UUID.randomUUID().toString());
                entity.setDocument(doc);
                entity.setChunkIndex(i);
                entity.setContent(cr.content());
                entity.setChunkType(DocumentChunkEntity.ChunkType.valueOf(cr.chunkType().name()));
                entity.setStartLine(cr.startLine());
                entity.setEndLine(cr.endLine());
                entity.setSymbolName(cr.symbolName());
                entity.setTokenCount(estimateTokens(cr.content()));
                entity.setSearchVector(null); // populated by native query below
                chunkRepository.save(entity);
                // Set embedding via native query (vector type requires explicit cast)
                chunkRepository.updateEmbedding(entity.getId(), floatArrayToVector(embeddings.get(i)));
                chunksCreated++;
            }

            docsIndexed++;
            log.info("Indexed document: {} ({} chunks)", loadResult.title(), chunks.size());
        }

        // Update full-text search vectors
        chunkRepository.updateSearchVectors();

        return new IndexResult(docsIndexed, chunksCreated);
    }

    public List<RetrievalResult> search(String query, int limit) {
        return hybridRetriever.retrieve(query, limit);
    }

    public List<RetrievalResult> searchSemantic(String query, int limit) {
        return hybridRetriever.retrieve(query, limit);
    }

    private List<DocumentChunker.ChunkResult> chunkDocument(String content, String language, String documentId) {
        if (astChunker.supports(language)) {
            List<DocumentChunker.ChunkResult> chunks = astChunker.chunk(content, language, documentId);
            if (!chunks.isEmpty()) return chunks;
        }
        return textChunker.chunk(content, language, documentId);
    }

    private int estimateTokens(String text) {
        return text.length() / 4; // rough estimate
    }

    private String floatArrayToVector(float[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int j = 0; j < arr.length; j++) {
            if (j > 0) sb.append(",");
            sb.append(arr[j]);
        }
        sb.append("]");
        return sb.toString();
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record IndexResult(int documentsIndexed, int chunksCreated) {}
}
