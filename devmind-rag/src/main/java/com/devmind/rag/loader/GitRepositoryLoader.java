package com.devmind.rag.loader;

import com.devmind.rag.entity.DocumentEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitRepositoryLoader implements DocumentLoader {

    private final FileSystemLoader fileSystemLoader = new FileSystemLoader();

    @Override
    public List<LoadResult> load(Path repoPath) {
        if (!isGitRepo(repoPath)) {
            throw new IllegalArgumentException("Not a git repository: " + repoPath);
        }
        return fileSystemLoader.load(repoPath).stream()
            .map(r -> new LoadResult(
                r.title(),
                r.content(),
                r.language(),
                r.sourcePath(),
                DocumentEntity.SourceType.REPO
            ))
            .toList();
    }

    @Override
    public boolean supports(Path path) {
        return isGitRepo(path);
    }

    private boolean isGitRepo(Path path) {
        return Files.isDirectory(path.resolve(".git"));
    }
}
