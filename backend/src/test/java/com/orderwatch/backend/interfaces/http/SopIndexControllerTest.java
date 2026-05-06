package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.application.rag.SopIndexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SopIndexController.class)
class SopIndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SopIndexService sopIndexService;

    @Test
    void indexesLocalDocs() throws Exception {
        when(sopIndexService.indexLocalDocs())
                .thenReturn(new SopIndexService.IndexResult(
                        "order_sop_chunks",
                        4,
                        12,
                        12,
                        List.of("sop-large-order.md")
                ));

        mockMvc.perform(post("/api/sop/index-local-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.collectionName").value("order_sop_chunks"))
                .andExpect(jsonPath("$.data.documentCount").value(4))
                .andExpect(jsonPath("$.data.chunkCount").value(12))
                .andExpect(jsonPath("$.data.upsertedCount").value(12));
    }
}
