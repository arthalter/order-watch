package com.legalwatch.backend.application.chat;

import com.legalwatch.backend.application.rag.LegalSopTools;
import org.springframework.ai.chat.client.ChatClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LegalQaAgentTest {

    @Test
    void registersLegalToolAndReturnsModelAnswer() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec request = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec response = mock(ChatClient.CallResponseSpec.class);
        LegalSopTools tools = mock(LegalSopTools.class);
        String modelAnswer = """
                ## 回答摘要
                核查保证方式与保证期间。

                ## 依据来源
                sop-guarantee-liability.md#chunk-1

                ## 使用边界
                本回答不构成正式法律意见。
                """;

        when(builder.defaultSystem(org.mockito.ArgumentMatchers.anyString())).thenReturn(builder);
        when(builder.defaultTools(tools)).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(request);
        when(request.user(org.mockito.ArgumentMatchers.anyString())).thenReturn(request);
        when(request.call()).thenReturn(response);
        when(response.content()).thenReturn(modelAnswer);

        String answer = new LegalQaAgent(builder, tools).answer("保证责任怎么查询？", "此前回答摘要");

        assertThat(answer).isEqualTo(modelAnswer);
        verify(builder).defaultTools(tools);
        verify(request).user(org.mockito.ArgumentMatchers.contains("此前回答摘要"));
    }

    @Test
    void rejectsEmptyModelAnswer() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec request = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec response = mock(ChatClient.CallResponseSpec.class);
        LegalSopTools tools = mock(LegalSopTools.class);
        when(builder.defaultSystem(org.mockito.ArgumentMatchers.anyString())).thenReturn(builder);
        when(builder.defaultTools(tools)).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(request);
        when(request.user(org.mockito.ArgumentMatchers.anyString())).thenReturn(request);
        when(request.call()).thenReturn(response);
        when(response.content()).thenReturn(" ");

        LegalQaAgent agent = new LegalQaAgent(builder, tools);
        assertThatThrownBy(() -> agent.answer("未知问题"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("chat model returned an empty answer");
    }
}
