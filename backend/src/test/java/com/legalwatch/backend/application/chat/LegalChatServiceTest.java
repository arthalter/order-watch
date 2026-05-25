package com.legalwatch.backend.application.chat;

import com.legalwatch.backend.interfaces.http.dto.LegalChatResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LegalChatServiceTest {

    @Test
    void remembersPreviousTurnWhenConversationContinues() {
        LegalQaAgent agent = mock(LegalQaAgent.class);
        ConversationMemoryService memory = new ConversationMemoryService();
        LegalChatService service = new LegalChatService(agent, memory);

        when(agent.answer("保证责任怎么查询？", "")).thenReturn("保证期间需要核查。");
        when(agent.answer(eq("其中保证期间是什么意思？"), contains("保证期间需要核查")))
                .thenReturn("保证期间是责任存续期间。");

        LegalChatResponse first = service.chat("conv_test", "保证责任怎么查询？");
        LegalChatResponse second = service.chat(first.conversationId(), "其中保证期间是什么意思？");

        assertThat(second.conversationId()).isEqualTo("conv_test");
        assertThat(second.answer()).contains("责任存续期间");
        verify(agent).answer(eq("其中保证期间是什么意思？"), contains("此前同一会话内容"));
    }
}
