package com.legalwatch.backend.application.rag;

import com.legalwatch.backend.infrastructure.rag.SopVectorStoreService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SopSearchServiceTest {

    @Test
    void searchesSopChunksByQueryEmbedding() {
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        SopVectorStoreService vectorStoreService = mock(SopVectorStoreService.class);
        float[] queryVector = new float[]{0.1f, 0.2f};
        float[] rewrittenVector = new float[]{0.3f, 0.4f};

        when(embeddingService.embed("保证责任查询")).thenReturn(queryVector);
        when(embeddingService.embed("保证责任 保证方式 保证期间 主债务范围")).thenReturn(rewrittenVector);
        when(vectorStoreService.search(queryVector, 9)).thenReturn(List.of(
                new SopVectorStoreService.SopSearchMatch(
                        "guarantee.md",
                        0,
                        0.82,
                        "保证责任应当查询保证方式、保证期间和主债务范围",
                        "保证合同查询说明"
                )
        ));
        when(vectorStoreService.search(rewrittenVector, 9)).thenReturn(List.of(
                new SopVectorStoreService.SopSearchMatch(
                        "guarantee.md",
                        0,
                        0.72,
                        "保证责任应当查询保证方式、保证期间和主债务范围",
                        "保证合同查询说明"
                )
        ));

        QueryRewriteService queryRewriteService = new QueryRewriteService();
        SopSearchService service = new SopSearchService(
                embeddingService,
                vectorStoreService,
                queryRewriteService,
                new SopRerankService(queryRewriteService)
        );

        List<SopSearchService.SopSearchResult> results = service.search(" 保证责任查询 ", 3);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).fileName()).isEqualTo("guarantee.md");
        assertThat(results.get(0).chunkIndex()).isZero();
        assertThat(results.get(0).score()).isGreaterThan(0.82);
        assertThat(results.get(0).content()).contains("保证责任");
        verify(embeddingService).embed("保证责任查询");
        verify(embeddingService).embed("保证责任 保证方式 保证期间 主债务范围");
        verify(vectorStoreService).search(queryVector, 9);
        verify(vectorStoreService).search(rewrittenVector, 9);
    }

    @Test
    void rejectsBlankQuery() {
        SopSearchService service = new SopSearchService(
                mock(EmbeddingService.class),
                mock(SopVectorStoreService.class),
                new QueryRewriteService(),
                new SopRerankService(new QueryRewriteService())
        );

        assertThatThrownBy(() -> service.search(" ", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("query is required");
    }
}
