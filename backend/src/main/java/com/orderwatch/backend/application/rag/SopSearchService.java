package com.orderwatch.backend.application.rag;

import com.orderwatch.backend.infrastructure.rag.SopVectorStoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SopSearchService {

    private static final int DEFAULT_TOP_K = 5;
    private static final int MAX_TOP_K = 20;

    private final EmbeddingService embeddingService;
    private final SopVectorStoreService vectorStoreService;

    public SopSearchService(EmbeddingService embeddingService, SopVectorStoreService vectorStoreService) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    public List<SopSearchResult> search(String query, Integer topK) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query is required");
        }

        int limit = normalizeTopK(topK);
        float[] queryVector = embeddingService.embed(query.trim());
        return vectorStoreService.search(queryVector, limit).stream()
                .map(match -> new SopSearchResult(
                        match.fileName(),
                        match.chunkIndex(),
                        match.score(),
                        match.content(),
                        match.title()
                ))
                .toList();
    }

    private static int normalizeTopK(Integer topK) {
        if (topK == null) {
            return DEFAULT_TOP_K;
        }
        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be positive");
        }
        return Math.min(topK, MAX_TOP_K);
    }

    public record SopSearchResult(
            String fileName,
            int chunkIndex,
            double score,
            String content,
            String title
    ) {
    }
}
