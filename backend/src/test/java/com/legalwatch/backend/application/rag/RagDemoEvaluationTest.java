package com.legalwatch.backend.application.rag;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagDemoEvaluationTest {

    private final QueryRewriteService rewriteService = new QueryRewriteService();
    private final SopRerankService rerankService = new SopRerankService(rewriteService);

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "保证责任怎么查询？, sop-guarantee-liability.md",
            "付款期限和验收节点怎么核查？, sop-contract-payment-clause.md",
            "劳动争议证据应该如何整理？, sop-labor-dispute-evidence.md",
            "借款诉讼时效需要查什么？, sop-loan-limitation-period.md",
            "正式法律意见的答复边界是什么？, sop-legal-answer-style.md"
    })
    void reranksDemoQueriesToExpectedDocuments(String question, String expectedFileName) throws Exception {
        String expectedContent = Files.readString(Path.of("legal-docs", expectedFileName));
        List<SopSearchService.SopSearchResult> candidates = List.of(
                new SopSearchService.SopSearchResult(
                        "irrelevant.md",
                        0,
                        0.20,
                        "一般项目说明与操作流程，不包含本次查询主题。",
                        "一般说明"
                ),
                new SopSearchService.SopSearchResult(
                        expectedFileName,
                        0,
                        0.05,
                        expectedContent,
                        expectedContent.lines().findFirst().orElse(expectedFileName)
                )
        );

        List<SopSearchService.SopSearchResult> reranked = rerankService.rerank(question, candidates, 2);

        assertThat(rewriteService.rewrite(question)).isNotEmpty();
        assertThat(reranked.get(0).fileName()).isEqualTo(expectedFileName);
    }
}
