package com.orderwatch.backend.application.chat;

import com.orderwatch.backend.application.mock.OrderEvidenceTools;
import com.orderwatch.backend.application.mock.OrderMetricsTools;
import com.orderwatch.backend.application.rag.OrderSopTools;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimpleOrderAgentTest {

    @Test
    void rejectsBlankQuestion() {
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));

        SimpleOrderAgent agent = new SimpleOrderAgent(
                chatClientBuilder,
                mock(OrderMetricsTools.class),
                mock(OrderEvidenceTools.class),
                mock(OrderSopTools.class)
        );

        assertThatThrownBy(() -> agent.answer(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("question is required");
        assertThatThrownBy(() -> agent.answer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("question is required");
    }
}
