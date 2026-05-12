package com.orderwatch.backend.application.chat;

import com.orderwatch.backend.application.mock.OrderEvidenceTools;
import com.orderwatch.backend.application.mock.OrderMetricsTools;
import com.orderwatch.backend.application.rag.OrderSopTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SimpleOrderAgent {

    private static final String SYSTEM_PROMPT = """
            你是 OrderWatch Mini 的电商异常订单运营助手。
            回答前优先调用工具获取事实，不要编造工具没有返回的信息。
            查询异常订单列表时使用 OrderMetricsTools。
            查询异常证据或依据时使用 OrderEvidenceTools。
            查询异常处理规则或 SOP 时使用 OrderSopTools。
            如果当前工具无法提供答案，请明确说明无法从当前工具获取。
            """;

    private final ChatClient chatClient;
    private final OrderMetricsTools orderMetricsTools;
    private final OrderEvidenceTools orderEvidenceTools;
    private final OrderSopTools orderSopTools;

    public SimpleOrderAgent(
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

    public String answer(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question is required");
        }

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(question)
                .tools(orderMetricsTools, orderEvidenceTools, orderSopTools)
                .call()
                .content();
    }
}
