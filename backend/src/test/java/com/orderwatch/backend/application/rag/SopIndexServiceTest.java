package com.orderwatch.backend.application.rag;

import com.orderwatch.backend.infrastructure.milvus.MilvusProperties;
import com.orderwatch.backend.infrastructure.rag.MarkdownDocumentReader;
import com.orderwatch.backend.infrastructure.rag.SopCollectionService;
import com.orderwatch.backend.infrastructure.rag.SopVectorStoreService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SopIndexServiceTest {

    @Test
    void indexesLocalMarkdownDocs() {
        MarkdownDocumentReader reader = mock(MarkdownDocumentReader.class);
        DocumentChunkService chunkService = mock(DocumentChunkService.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        SopCollectionService collectionService = mock(SopCollectionService.class);
        SopVectorStoreService vectorStoreService = mock(SopVectorStoreService.class);

        MilvusProperties properties = new MilvusProperties();
        properties.setCollectionName("order_sop_chunks");

        when(collectionService.initializeCollection())
                .thenReturn(new SopCollectionService.InitResult("order_sop_chunks", true, false, 2));
        when(reader.readAll()).thenReturn(List.of(new MarkdownDocumentReader.MarkdownDocument(
                "sop-large-order.md",
                "/docs/sop-large-order.md",
                "# 大额订单 SOP\n\n人工审核"
        )));
        when(chunkService.chunk("sop-large-order.md", "# 大额订单 SOP\n\n人工审核"))
                .thenReturn(List.of(new DocumentChunk("sop-large-order.md", 0, "人工审核")));
        when(embeddingService.embed("人工审核")).thenReturn(new float[]{0.1f, 0.2f});
        when(vectorStoreService.upsert(anyList())).thenReturn(1L);

        SopIndexService service = new SopIndexService(
                reader,
                chunkService,
                embeddingService,
                collectionService,
                vectorStoreService,
                properties
        );

        SopIndexService.IndexResult result = service.indexLocalDocs();

        assertThat(result.collectionName()).isEqualTo("order_sop_chunks");
        assertThat(result.documentCount()).isEqualTo(1);
        assertThat(result.chunkCount()).isEqualTo(1);
        assertThat(result.upsertedCount()).isEqualTo(1);
        assertThat(result.files()).containsExactly("sop-large-order.md");
        verify(vectorStoreService).upsert(anyList());
    }
}
