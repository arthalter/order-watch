package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.application.rag.SopSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SopSearchController.class)
class SopSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SopSearchService sopSearchService;

    @Test
    void searchesSopChunks() throws Exception {
        when(sopSearchService.search("大额订单人工审核", 3)).thenReturn(List.of(
                new SopSearchService.SopSearchResult(
                        "sop-large-order.md",
                        0,
                        0.82,
                        "当订单金额显著高于店铺平均客单价时，应进入人工审核",
                        "大额订单 SOP"
                )
        ));

        mockMvc.perform(get("/api/sop/search")
                        .param("query", "大额订单人工审核")
                        .param("topK", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fileName").value("sop-large-order.md"))
                .andExpect(jsonPath("$.data[0].chunkIndex").value(0))
                .andExpect(jsonPath("$.data[0].score").value(0.82))
                .andExpect(jsonPath("$.data[0].content").value("当订单金额显著高于店铺平均客单价时，应进入人工审核"))
                .andExpect(jsonPath("$.data[0].title").value("大额订单 SOP"));
    }

    @Test
    void returnsBadRequestResponseForBlankQuery() throws Exception {
        when(sopSearchService.search(" ", null))
                .thenThrow(new IllegalArgumentException("query is required"));

        mockMvc.perform(get("/api/sop/search").param("query", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(40000))
                .andExpect(jsonPath("$.message").value("query is required"));
    }
}
