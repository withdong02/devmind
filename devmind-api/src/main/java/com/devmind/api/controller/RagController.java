package com.devmind.api.controller;

import com.devmind.core.rag.RetrievalResult;
import com.devmind.rag.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "RAG", description = "Retrieval-Augmented Generation: indexing, search")
@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @Operation(summary = "Index a directory of source files for RAG")
    @PostMapping("/index")
    public ResponseEntity<Map<String, Object>> indexDirectory(@RequestBody Map<String, String> request) {
        String path = request.get("path");
        if (path == null || path.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "path is required"));
        }

        try {
            RagService.IndexResult result = ragService.indexDirectory(path);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "documentsIndexed", result.documentsIndexed(),
                "chunksCreated", result.chunksCreated()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Search indexed documents by semantic similarity")
    @PostMapping("/search")
    public ResponseEntity<List<SearchResult>> search(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        int limit = request.containsKey("limit") ? ((Number) request.get("limit")).intValue() : 10;

        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<RetrievalResult> results = ragService.search(query, limit);

        List<SearchResult> response = results.stream()
            .map(r -> new SearchResult(
                r.chunk().getId(),
                r.chunk().getContent(),
                r.chunk().getSymbolName(),
                r.chunk().getChunkType().name(),
                r.chunk().getStartLine(),
                r.chunk().getEndLine(),
                r.score(),
                r.source()
            ))
            .toList();

        return ResponseEntity.ok(response);
    }

    record SearchResult(
        String chunkId,
        String content,
        String symbolName,
        String chunkType,
        int startLine,
        int endLine,
        double score,
        String retrievalSource
    ) {}
}
