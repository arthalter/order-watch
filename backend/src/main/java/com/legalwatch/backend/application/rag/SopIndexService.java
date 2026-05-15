package com.legalwatch.backend.application.rag;

import com.legalwatch.backend.infrastructure.milvus.MilvusProperties;
import com.legalwatch.backend.infrastructure.rag.MarkdownDocumentReader;
import com.legalwatch.backend.infrastructure.rag.SopCollectionService;
import com.legalwatch.backend.infrastructure.rag.SopVectorStoreService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SopIndexService {

    private final MarkdownDocumentReader documentReader;
    private final DocumentChunkService chunkService;
    private final EmbeddingService embeddingService;
    private final SopCollectionService collectionService;
    private final SopVectorStoreService vectorStoreService;
    private final MilvusProperties milvusProperties;

    public SopIndexService(
            MarkdownDocumentReader documentReader,
            DocumentChunkService chunkService,
            EmbeddingService embeddingService,
            SopCollectionService collectionService,
            SopVectorStoreService vectorStoreService,
            MilvusProperties milvusProperties
    ) {
        this.documentReader = documentReader;
        this.chunkService = chunkService;
        this.embeddingService = embeddingService;
        this.collectionService = collectionService;
        this.vectorStoreService = vectorStoreService;
        this.milvusProperties = milvusProperties;
    }

    public IndexResult indexLocalDocs() {
        SopCollectionService.InitResult collectionResult = collectionService.initializeCollection();
        List<MarkdownDocumentReader.MarkdownDocument> documents = documentReader.readAll();
        if (documents.isEmpty()) {
            return new IndexResult(collectionResult.collectionName(), 0, 0, 0, List.of());
        }

        List<SopVectorStoreService.SopChunkVector> vectors = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();

        for (MarkdownDocumentReader.MarkdownDocument document : documents) {
            fileNames.add(document.filename());
            vectors.addAll(toVectors(document));
        }

        long upsertedCount = vectorStoreService.upsert(vectors);
        return new IndexResult(
                milvusProperties.getCollectionName(),
                documents.size(),
                vectors.size(),
                upsertedCount,
                fileNames
        );
    }

    public IndexResult indexDocument(String filename, String content) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("filename is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }

        collectionService.initializeCollection();
        MarkdownDocumentReader.MarkdownDocument document = new MarkdownDocumentReader.MarkdownDocument(
                filename,
                filename,
                content
        );
        List<SopVectorStoreService.SopChunkVector> vectors = toVectors(document);
        long upsertedCount = vectorStoreService.upsert(vectors);
        return new IndexResult(
                milvusProperties.getCollectionName(),
                1,
                vectors.size(),
                upsertedCount,
                List.of(filename)
        );
    }

    private List<SopVectorStoreService.SopChunkVector> toVectors(MarkdownDocumentReader.MarkdownDocument document) {
        String title = extractTitle(document.content(), document.filename());
        return chunkService.chunk(document.filename(), document.content()).stream()
                .map(chunk -> new SopVectorStoreService.SopChunkVector(
                        chunkId(document.filename(), chunk.chunkIndex()),
                        chunk.text(),
                        embeddingService.embed(chunk.text()),
                        document.filename(),
                        chunk.chunkIndex(),
                        title
                ))
                .toList();
    }

    private static String chunkId(String filename, int chunkIndex) {
        return filename + "#" + chunkIndex;
    }

    private static String extractTitle(String content, String fallback) {
        if (content == null || content.isBlank()) {
            return fallback;
        }

        String[] lines = content.replace("\r\n", "\n").replace("\r", "\n").split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#")) {
                return trimmed.replaceFirst("^#+\\s*", "").trim();
            }
        }
        return fallback;
    }

    public record IndexResult(
            String collectionName,
            int documentCount,
            int chunkCount,
            long upsertedCount,
            List<String> files
    ) {
    }
}
