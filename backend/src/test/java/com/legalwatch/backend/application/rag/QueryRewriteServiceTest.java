package com.legalwatch.backend.application.rag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryRewriteServiceTest {

    private final QueryRewriteService service = new QueryRewriteService();

    @Test
    void expandsLegalGuaranteeQuestionForRecallAndRerank() {
        assertThat(service.rewrite("保证责任怎么查询？"))
                .containsExactly(
                        "保证责任怎么查询？",
                        "保证责任 保证方式 保证期间 主债务范围"
                );
        assertThat(service.keywords("保证责任怎么查询？"))
                .contains("保证责任", "保证方式", "保证期间", "主债务范围");
    }
}
