package com.legalwatch.backend.application.rag;

import com.legalwatch.backend.infrastructure.milvus.MilvusProperties;
import com.legalwatch.backend.infrastructure.rag.MarkdownDocumentReader;
import com.legalwatch.backend.infrastructure.rag.SopCollectionService;
import com.legalwatch.backend.infrastructure.rag.SopVectorStoreService;
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
        properties.setCollectionName("legal_sop_chunks");

        when(collectionService.initializeCollection())
                .thenReturn(new SopCollectionService.InitResult("legal_sop_chunks", true, false, 2));
        when(reader.readAll()).thenReturn(List.of(new MarkdownDocumentReader.MarkdownDocument(
                "sop-contract-payment-clause.md",
                "/docs/sop-contract-payment-clause.md",
                "# 合同付款条款审查指引\n\n付款期限"
        )));
        when(chunkService.chunk("sop-contract-payment-clause.md", "# 合同付款条款审查指引\n\n付款期限"))
                .thenReturn(List.of(new DocumentChunk("sop-contract-payment-clause.md", 0, "付款期限")));
        when(embeddingService.embed("付款期限")).thenReturn(new float[]{0.1f, 0.2f});
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

        assertThat(result.collectionName()).isEqualTo("legal_sop_chunks");
        assertThat(result.documentCount()).isEqualTo(1);
        assertThat(result.chunkCount()).isEqualTo(1);
        assertThat(result.upsertedCount()).isEqualTo(1);
        assertThat(result.files()).containsExactly("sop-contract-payment-clause.md");
        verify(vectorStoreService).upsert(anyList());
    }
}
