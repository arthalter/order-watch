package com.orderwatch.backend.application.monitoring;

import com.orderwatch.backend.application.mock.OrderEvidenceTools;
import com.orderwatch.backend.application.mock.OrderMetricsTools;
import com.orderwatch.backend.application.rag.OrderSopTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ReportAgent {

    private static final String SYSTEM_PROMPT = """
            你是 OrderWatch Mini 的异常订单报告助手。
            你必须先调用工具获取异常订单、证据和 SOP 依据，再生成报告。
            查询异常订单列表时使用 OrderMetricsTools。
            查询异常证据时使用 OrderEvidenceTools。
            查询处理规则或 SOP 时使用 OrderSopTools。
            不要编造工具没有返回的信息；如果工具没有提供某项信息，写“当前工具未提供”。

            输出必须是 Markdown，并严格包含以下标题：
            # 异常订单监控报告
            ## 异常概览
            ## 重点订单
            ## 证据摘要
            ## SOP 依据
            ## 人工确认项
            ## 建议处理动作
            """;

    private static final String USER_PROMPT = """
            请基于最近 24 小时异常订单、相关证据和命中的 SOP，生成一份运营可读的 Markdown 报告。
            报告要简洁，优先覆盖高危订单，不要写任务外的复杂分析。
            """;

    private final ChatClient chatClient;
    private final OrderMetricsTools orderMetricsTools;
    private final OrderEvidenceTools orderEvidenceTools;
    private final OrderSopTools orderSopTools;

    public ReportAgent(
            ChatClient.Builder chatClientBuilder,
            OrderMetricsTools orderMetricsTools,
            OrderEvidenceTools orderEvidenceTools,
            OrderSopTools orderSopTools
    ) {
        this.chatClient = chatClientBuilder.build();
        this.orderMetricsTools = orderMetricsTools;
        this.orderEvidenceTools = orderEvidenceTools;
        this.orderSopTools = orderSopTools;
    }

    public String generateMarkdownReport() {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(USER_PROMPT)
                .tools(orderMetricsTools, orderEvidenceTools, orderSopTools)
                .call()
                .content();
    }
}
