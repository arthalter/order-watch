package com.orderwatch.backend.application.rag;

import com.orderwatch.backend.infrastructure.rag.SopVectorStoreService;
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

        when(embeddingService.embed("大额订单人工审核")).thenReturn(queryVector);
        when(vectorStoreService.search(queryVector, 3)).thenReturn(List.of(
                new SopVectorStoreService.SopSearchMatch(
                        "sop-large-order.md",
                        0,
                        0.82,
                        "当订单金额显著高于店铺平均客单价时，应进入人工审核",
                        "大额订单 SOP"
                )
        ));

        SopSearchService service = new SopSearchService(embeddingService, vectorStoreService);

        List<SopSearchService.SopSearchResult> results = service.search(" 大额订单人工审核 ", 3);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).fileName()).isEqualTo("sop-large-order.md");
        assertThat(results.get(0).chunkIndex()).isZero();
        assertThat(results.get(0).score()).isEqualTo(0.82);
        assertThat(results.get(0).content()).contains("人工审核");
        verify(embeddingService).embed("大额订单人工审核");
        verify(vectorStoreService).search(queryVector, 3);
    }

    @Test
    void rejectsBlankQuery() {
        SopSearchService service = new SopSearchService(
                mock(EmbeddingService.class),
                mock(SopVectorStoreService.class)
        );

        assertThatThrownBy(() -> service.search(" ", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("query is required");
    }
}
