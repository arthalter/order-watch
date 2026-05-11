package com.orderwatch.backend.application.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderSopToolsTest {

    @Test
    void queriesOrderSopWithDefaultTopK() {
        SopSearchService sopSearchService = mock(SopSearchService.class);
        String query = "大额订单异常处理规则";
        List<SopSearchService.SopSearchResult> expected = List.of(
                new SopSearchService.SopSearchResult(
                        "sop-large-order.md",
                        0,
                        0.82,
                        "大额订单应进入人工审核",
                        "大额订单 SOP"
                )
        );

        when(sopSearchService.search(query, null)).thenReturn(expected);

        OrderSopTools tools = new OrderSopTools(sopSearchService);
        List<SopSearchService.SopSearchResult> results = tools.queryOrderSop(query);

        assertThat(results).isEqualTo(expected);
        verify(sopSearchService).search(query, null);
    }
}
