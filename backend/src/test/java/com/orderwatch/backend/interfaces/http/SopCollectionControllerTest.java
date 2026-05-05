package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.infrastructure.rag.SopCollectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SopCollectionController.class)
class SopCollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SopCollectionService sopCollectionService;

    @Test
    void initializesCollection() throws Exception {
        when(sopCollectionService.initializeCollection())
                .thenReturn(new SopCollectionService.InitResult("order_sop_chunks", true, true, 1024));

        mockMvc.perform(post("/api/sop/collection/init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.collectionName").value("order_sop_chunks"))
                .andExpect(jsonPath("$.data.exists").value(true))
                .andExpect(jsonPath("$.data.created").value(true))
                .andExpect(jsonPath("$.data.vectorDimension").value(1024));
    }

    @Test
    void returnsCollectionStatus() throws Exception {
        when(sopCollectionService.hasCollection()).thenReturn(true);

        mockMvc.perform(get("/api/sop/collection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exists").value(true));
    }
}
