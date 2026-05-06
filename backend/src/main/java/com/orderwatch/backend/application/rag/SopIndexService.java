package com.orderwatch.backend.application.rag;

import com.orderwatch.backend.infrastructure.milvus.MilvusProperties;
import com.orderwatch.backend.infrastructure.rag.MarkdownDocumentReader;
import com.orderwatch.backend.infrastructure.rag.SopCollectionService;
import com.orderwatch.backend.infrastructure.rag.SopVectorStoreService;
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
            String title = extractTitle(document.content(), document.filename());
            List<DocumentChunk> chunks = chunkService.chunk(document.filename(), document.content());
            for (DocumentChunk chunk : chunks) {
                float[] vector = embeddingService.embed(chunk.text());
                vectors.add(new SopVectorStoreService.SopChunkVector(
                        chunkId(document.filename(), chunk.chunkIndex()),
                        chunk.text(),
                        vector,
                        document.filename(),
                        chunk.chunkIndex(),
                        title
                ));
            }
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
