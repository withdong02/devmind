package com.devmind.core.rag;

public record RetrievalResult(
    DocumentChunk chunk,
    double score,
    String source
) {}
