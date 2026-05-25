package com.legalwatch.backend.application.rag;

import com.legalwatch.backend.infrastructure.rag.SopVectorStoreService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SopSearchService {

    private static final int DEFAULT_TOP_K = 5;
    private static final int MAX_TOP_K = 20;

    private final EmbeddingService embeddingService;
    private final SopVectorStoreService vectorStoreService;
    private final QueryRewriteService queryRewriteService;
    private final SopRerankService rerankService;

    public SopSearchService(
            EmbeddingService embeddingService,
            SopVectorStoreService vectorStoreService,
            QueryRewriteService queryRewriteService,
            SopRerankService rerankService
    ) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.queryRewriteService = queryRewriteService;
        this.rerankService = rerankService;
    }

    public List<SopSearchResult> search(String query, Integer topK) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query is required");
        }

        int limit = normalizeTopK(topK);
        int recallLimit = Math.min(MAX_TOP_K, Math.max(limit * 3, limit));
        Map<String, SopSearchResult> candidates = new LinkedHashMap<>();

        for (String rewrittenQuery : queryRewriteService.rewrite(query.trim())) {
            float[] queryVector = embeddingService.embed(rewrittenQuery);
            vectorStoreService.search(queryVector, recallLimit).stream()
                    .map(SopSearchService::toResult)
                    .forEach(result -> candidates.merge(
                            result.fileName() + "#" + result.chunkIndex(),
                            result,
                            SopSearchService::higherScore
                    ));
        }
        return rerankService.rerank(query.trim(), List.copyOf(candidates.values()), limit);
    }

    private static SopSearchResult toResult(SopVectorStoreService.SopSearchMatch match) {
        return new SopSearchResult(
                match.fileName(),
                match.chunkIndex(),
                match.score(),
                match.content(),
                match.title()
        );
    }

    private static SopSearchResult higherScore(SopSearchResult left, SopSearchResult right) {
        return left.score() >= right.score() ? left : right;
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
