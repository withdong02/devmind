package com.devmind.rag.chunker;

import com.devmind.rag.entity.DocumentChunkEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class TokenTextChunker implements DocumentChunker {

    private static final int CHUNK_SIZE = 512;   // approximate tokens (1 token ~ 4 chars)
    private static final int OVERLAP = 64;        // overlap tokens
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(
        "python", "javascript", "typescript", "go", "rust", "c", "kotlin",
        "ruby", "php", "swift", "csharp", "markdown", "json", "yaml", "xml", "text"
    );

    @Override
    public List<ChunkResult> chunk(String content, String language, String documentId) {
        List<ChunkResult> chunks = new ArrayList<>();
        String[] lines = content.split("\n", -1);

        int chunkSizeLines = CHUNK_SIZE * 4 / 80; // rough: ~80 chars per line
        int overlapLines = OVERLAP * 4 / 80;

        int i = 0;
        int chunkIndex = 0;
        while (i < lines.length) {
            int end = Math.min(i + chunkSizeLines, lines.length);
            String chunkContent = String.join("\n", java.util.Arrays.copyOfRange(lines, i, end));

            if (!chunkContent.isBlank()) {
                chunks.add(new ChunkResult(
                    chunkContent,
                    DocumentChunkEntity.ChunkType.TEXT,
                    i + 1,
                    end,
                    "chunk-" + chunkIndex
                ));
                chunkIndex++;
            }

            i += chunkSizeLines - overlapLines;
        }

        return chunks;
    }

    @Override
    public boolean supports(String language) {
        return SUPPORTED_LANGUAGES.contains(language);
    }
}
