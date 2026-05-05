package com.devmind.rag.loader;

import com.devmind.rag.entity.DocumentEntity;

import java.nio.file.Path;
import java.util.List;

public interface DocumentLoader {

    List<LoadResult> load(Path path);

    boolean supports(Path path);

    record LoadResult(
        String title,
        String content,
        String language,
        String sourcePath,
        DocumentEntity.SourceType sourceType
    ) {}
}
