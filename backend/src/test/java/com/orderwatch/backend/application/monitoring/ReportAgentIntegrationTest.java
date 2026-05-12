package com.orderwatch.backend.application.monitoring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfSystemProperty(named = "report.agent.it", matches = "true")
class ReportAgentIntegrationTest {

    @Autowired
    private ReportAgent reportAgent;

    @Test
    void generateMarkdownReportWithRealBailianRequest() {
        String report = reportAgent.generateMarkdownReport();

        assertThat(report).contains("# 异常订单监控报告");
        assertThat(report).contains("## 异常概览");
        assertThat(report).contains("## 重点订单");
        assertThat(report).contains("## 证据摘要");
        assertThat(report).contains("## SOP 依据");
        assertThat(report).contains("## 人工确认项");
        assertThat(report).contains("## 建议处理动作");
    }
}
