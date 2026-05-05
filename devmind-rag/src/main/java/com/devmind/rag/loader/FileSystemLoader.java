package com.devmind.rag.loader;

import com.devmind.rag.entity.DocumentEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class FileSystemLoader implements DocumentLoader {

    private static final Set<String> CODE_EXTENSIONS = Set.of(
        ".java", ".py", ".js", ".ts", ".tsx", ".jsx", ".go", ".rs", ".cpp", ".c", ".h",
        ".kt", ".scala", ".rb", ".php", ".swift", ".cs"
    );

    private static final Set<String> DOC_EXTENSIONS = Set.of(
        ".md", ".txt", ".rst", ".adoc", ".json", ".yaml", ".yml", ".xml", ".properties"
    );

    private static final Set<String> IGNORED_DIRS = Set.of(
        "node_modules", ".git", "target", "build", "dist", ".idea", ".vscode", "__pycache__"
    );

    @Override
    public List<LoadResult> load(Path path) {
        List<LoadResult> results = new ArrayList<>();
        try {
            if (Files.isRegularFile(path)) {
                loadFile(path).ifPresent(results::add);
            } else if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        if (IGNORED_DIRS.contains(dir.getFileName().toString())) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        loadFile(file).ifPresent(results::add);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load documents from: " + path, e);
        }
        return results;
    }

    @Override
    public boolean supports(Path path) {
        return Files.exists(path);
    }

    private java.util.Optional<LoadResult> loadFile(Path file) {
        String fileName = file.getFileName().toString();
        String ext = getExtension(fileName);

        if (!CODE_EXTENSIONS.contains(ext) && !DOC_EXTENSIONS.contains(ext)) {
            return java.util.Optional.empty();
        }

        try {
            String content = Files.readString(file);
            if (content.isBlank()) return java.util.Optional.empty();

            String language = detectLanguage(ext);
            String title = fileName;
            String sourcePath = file.toAbsolutePath().toString();

            return java.util.Optional.of(new LoadResult(title, content, language, sourcePath, DocumentEntity.SourceType.FILE));
        } catch (IOException e) {
            return java.util.Optional.empty();
        }
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : "";
    }

    private String detectLanguage(String ext) {
        return switch (ext) {
            case ".java" -> "java";
            case ".py" -> "python";
            case ".js", ".jsx" -> "javascript";
            case ".ts", ".tsx" -> "typescript";
            case ".go" -> "go";
            case ".rs" -> "rust";
            case ".cpp", ".c", ".h" -> "c";
            case ".kt" -> "kotlin";
            case ".rb" -> "ruby";
            case ".php" -> "php";
            case ".swift" -> "swift";
            case ".cs" -> "csharp";
            case ".md" -> "markdown";
            case ".json" -> "json";
            case ".yaml", ".yml" -> "yaml";
            case ".xml" -> "xml";
            default -> "text";
        };
    }
}
