package com.legalwatch.backend.application.chat;

import com.legalwatch.backend.application.rag.LegalSopTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class LegalQaAgent {

    private static final String SYSTEM_PROMPT = """
            你是 LegalWatch 法律文档查询助手。你的任务是基于知识库材料回答用户问题，而不是给出正式法律意见。

            工作规则：
            1. 每次回答法律文档问题前，必须先调用 queryLegalSop 工具检索已入库的文档片段。
            2. 只能依据工具返回的片段作答；不得补充工具结果中没有出现的法律事实、条款或结论。
            3. 如果检索不到足以回答的材料，明确说明现有知识库无法支持回答，并提示补充材料。
            4. 回答必须使用 Markdown，包含“## 回答摘要”“## 依据来源”“## 使用边界”三个部分。
            5. “依据来源”必须引用工具结果中的文件名与 chunk 编号，格式为 `file.md#chunk-n`。
            6. “使用边界”必须说明回答仅用于文档查询与学习演示，不构成正式法律意见。
            """;

    private final ChatClient chatClient;

    public LegalQaAgent(ChatClient.Builder chatClientBuilder, LegalSopTools legalSopTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(legalSopTools)
                .build();
    }

    public String answer(String question) {
        return answer(question, "");
    }

    public String answer(String question, String conversationContext) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question is required");
        }

        String context = conversationContext == null ? "" : conversationContext.trim();
        String userPrompt = context.isBlank()
                ? "用户问题：\n" + question.trim()
                : "用户问题：\n" + question.trim() + "\n\n同一会话的历史上下文，仅用于理解追问：\n" + context;

        String answer = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();
        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("chat model returned an empty answer");
        }
        return answer;
    }
}
