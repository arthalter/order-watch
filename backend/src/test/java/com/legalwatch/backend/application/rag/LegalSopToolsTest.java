package com.legalwatch.backend.application.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LegalSopToolsTest {

    @Test
    void queriesLegalSopWithDefaultTopK() {
        SopSearchService sopSearchService = mock(SopSearchService.class);
        String query = "保证责任怎么查询";
        List<SopSearchService.SopSearchResult> expected = List.of(
                new SopSearchService.SopSearchResult(
                        "sop-contract-payment-clause.md",
                        0,
                        0.82,
                        "保证责任应当查询保证方式、保证期间和主债务范围",
                        "保证合同查询说明"
                )
        );

        when(sopSearchService.search(query, null)).thenReturn(expected);

        LegalSopTools tools = new LegalSopTools(sopSearchService);
        List<SopSearchService.SopSearchResult> results = tools.queryLegalSop(query);

        assertThat(results).isEqualTo(expected);
        verify(sopSearchService).search(query, null);
    }
}
