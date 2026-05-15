package com.legalwatch.backend.application.rag;

import com.legalwatch.backend.infrastructure.rag.RagProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DocumentUploadService {

    private final RagProperties ragProperties;
    private final SopIndexService sopIndexService;

    public DocumentUploadService(RagProperties ragProperties, SopIndexService sopIndexService) {
        this.ragProperties = ragProperties;
        this.sopIndexService = sopIndexService;
    }

    public UploadResult uploadAndIndex(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        String filename = normalizeFilename(file.getOriginalFilename());
        if (!filename.toLowerCase().endsWith(".md")) {
            throw new IllegalArgumentException("only .md files are supported");
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (content.isBlank()) {
                throw new IllegalArgumentException("file content is required");
            }

            Path docsDir = resolveDocsDir();
            Files.createDirectories(docsDir);
            Path savedPath = docsDir.resolve(filename).normalize();
            if (!savedPath.startsWith(docsDir)) {
                throw new IllegalArgumentException("invalid filename");
            }
            Files.writeString(savedPath, content, StandardCharsets.UTF_8);

            SopIndexService.IndexResult indexResult = sopIndexService.indexDocument(filename, content);
            return new UploadResult(
                    filename,
                    savedPath.toString(),
                    indexResult.collectionName(),
                    indexResult.chunkCount(),
                    indexResult.upsertedCount()
            );
        } catch (IOException ex) {
            throw new IllegalStateException("failed to save uploaded document", ex);
        }
    }

    private Path resolveDocsDir() {
        String docsPath = ragProperties.getDocsPath();
        if (docsPath == null || docsPath.isBlank()) {
            throw new IllegalStateException("rag.docs-path is required");
        }
        return Path.of(docsPath).normalize();
    }

    private static String normalizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("filename is required");
        }
        return Path.of(originalFilename).getFileName().toString();
    }

    public record UploadResult(
            String fileName,
            String path,
            String collectionName,
            int chunkCount,
            long upsertedCount
    ) {
    }
}
