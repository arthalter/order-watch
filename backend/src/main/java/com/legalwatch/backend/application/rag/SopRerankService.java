package com.legalwatch.backend.application.rag;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SopRerankService {

    private final QueryRewriteService queryRewriteService;

    public SopRerankService(QueryRewriteService queryRewriteService) {
        this.queryRewriteService = queryRewriteService;
    }

    public List<SopSearchService.SopSearchResult> rerank(
            String query,
            List<SopSearchService.SopSearchResult> candidates,
            int limit
    ) {
        List<String> keywords = queryRewriteService.keywords(query);
        return candidates.stream()
                .map(result -> withRerankScore(result, keywords))
                .sorted(Comparator.comparingDouble(SopSearchService.SopSearchResult::score).reversed())
                .limit(limit)
                .toList();
    }

    private SopSearchService.SopSearchResult withRerankScore(
            SopSearchService.SopSearchResult result,
            List<String> keywords
    ) {
        String title = result.title() == null ? "" : result.title();
        String content = result.content() == null ? "" : result.content();
        double lexicalScore = 0.0;
        for (String keyword : keywords) {
            if (title.contains(keyword)) {
                lexicalScore += 0.12;
            }
            if (content.contains(keyword)) {
                lexicalScore += 0.18;
            }
        }
        double combinedScore = result.score() + Math.min(lexicalScore, 0.9);
        return new SopSearchService.SopSearchResult(
                result.fileName(),
                result.chunkIndex(),
                combinedScore,
                result.content(),
                result.title()
        );
    }
}
