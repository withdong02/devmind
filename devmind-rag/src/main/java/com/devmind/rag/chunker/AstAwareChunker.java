package com.devmind.rag.chunker;

import com.devmind.rag.entity.DocumentChunkEntity;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AstAwareChunker implements DocumentChunker {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("java");
    private static final int MAX_CHUNK_LINES = 100;
    private static final int MIN_CHUNK_LINES = 5;

    @Override
    public List<ChunkResult> chunk(String content, String language, String documentId) {
        if (!supports(language)) {
            return List.of();
        }

        try {
            CompilationUnit cu = StaticJavaParser.parse(content);
            List<ChunkResult> chunks = new ArrayList<>();
            String[] lines = content.split("\n", -1);

            // Extract package and imports as a preamble chunk
            String preamble = extractPreamble(lines);
            if (!preamble.isBlank()) {
                chunks.add(new ChunkResult(
                    preamble,
                    DocumentChunkEntity.ChunkType.BLOCK,
                    1,
                    countLines(preamble),
                    "preamble"
                ));
            }

            // Visit class and method declarations
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                    super.visit(n, arg);
                    int startLine = n.getBegin().map(p -> p.line).orElse(1);
                    int endLine = n.getEnd().map(p -> p.line).orElse(startLine);

                    // If the class is small enough, chunk it as a whole
                    if (endLine - startLine <= MAX_CHUNK_LINES) {
                        String chunkContent = extractLines(lines, startLine, endLine);
                        if (countLines(chunkContent) >= MIN_CHUNK_LINES) {
                            chunks.add(new ChunkResult(
                                chunkContent,
                                DocumentChunkEntity.ChunkType.CLASS,
                                startLine,
                                endLine,
                                n.getNameAsString()
                            ));
                        }
                    } else {
                        // Large class - chunk individual methods
                        // Class declaration without methods
                        String classHeader = extractLines(lines, startLine, findMethodStart(n) - 1);
                        if (!classHeader.isBlank() && countLines(classHeader) >= MIN_CHUNK_LINES) {
                            chunks.add(new ChunkResult(
                                classHeader,
                                DocumentChunkEntity.ChunkType.CLASS,
                                startLine,
                                findMethodStart(n) - 1,
                                n.getNameAsString() + " (declaration)"
                            ));
                        }
                    }
                }

                @Override
                public void visit(MethodDeclaration n, Void arg) {
                    super.visit(n, arg);
                    int startLine = n.getBegin().map(p -> p.line).orElse(1);
                    int endLine = n.getEnd().map(p -> p.line).orElse(startLine);

                    String chunkContent = extractLines(lines, startLine, endLine);
                    if (countLines(chunkContent) >= MIN_CHUNK_LINES) {
                        chunks.add(new ChunkResult(
                            chunkContent,
                            DocumentChunkEntity.ChunkType.METHOD,
                            startLine,
                            endLine,
                            n.getNameAsString()
                        ));
                    }
                }
            }, null);

            // If no AST chunks were extracted, fall back to full file
            if (chunks.isEmpty()) {
                chunks.add(new ChunkResult(
                    content,
                    DocumentChunkEntity.ChunkType.TEXT,
                    1,
                    lines.length,
                    "full-file"
                ));
            }

            return chunks;
        } catch (Exception e) {
            // JavaParser failed - return whole file as single chunk
            return List.of(new ChunkResult(
                content,
                DocumentChunkEntity.ChunkType.TEXT,
                1,
                content.split("\n", -1).length,
                "parse-failed"
            ));
        }
    }

    @Override
    public boolean supports(String language) {
        return "java".equals(language);
    }

    private String extractPreamble(String[] lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.strip();
            if (trimmed.startsWith("package ") || trimmed.startsWith("import ") || trimmed.isBlank()) {
                sb.append(line).append("\n");
            } else if (trimmed.startsWith("public ") || trimmed.startsWith("class ") ||
                       trimmed.startsWith("interface ") || trimmed.startsWith("@" )) {
                break;
            }
        }
        return sb.toString().strip();
    }

    private String extractLines(String[] lines, int startLine, int endLine) {
        int start = Math.max(0, startLine - 1);
        int end = Math.min(lines.length, endLine);
        return String.join("\n", Arrays.copyOfRange(lines, start, end));
    }

    private int countLines(String text) {
        return text.split("\n", -1).length;
    }

    private int findMethodStart(ClassOrInterfaceDeclaration n) {
        return n.getMembers().stream()
            .filter(m -> m instanceof MethodDeclaration)
            .map(m -> m.getBegin().map(p -> p.line).orElse(Integer.MAX_VALUE))
            .min(Integer::compareTo)
            .orElse(n.getEnd().map(p -> p.line).orElse(n.getBegin().map(p -> p.line).orElse(1) + 1));
    }
}
