package com.orderwatch.backend.infrastructure.rag;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class MarkdownDocumentReader {

    private final RagProperties ragProperties;

    public MarkdownDocumentReader(RagProperties ragProperties) {
        this.ragProperties = ragProperties;
    }

    public List<MarkdownDocument> readAll() {
        Path docsDir = resolveDocsDir();
        if (docsDir == null || !Files.isDirectory(docsDir)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(docsDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".md"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(this::readOne)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private MarkdownDocument readOne(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return new MarkdownDocument(path.getFileName().toString(), path.toString(), content);
        } catch (IOException e) {
            return null;
        }
    }

    private Path resolveDocsDir() {
        String docsPath = ragProperties.getDocsPath();
        if (docsPath == null || docsPath.isBlank()) {
            return null;
        }
        return Path.of(docsPath).normalize();
    }

    public record MarkdownDocument(String filename, String path, String content) {
    }
}

