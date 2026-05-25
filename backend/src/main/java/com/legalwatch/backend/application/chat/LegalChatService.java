package com.legalwatch.backend.application.chat;

import com.legalwatch.backend.interfaces.http.dto.LegalChatResponse;
import org.springframework.stereotype.Service;

@Service
public class LegalChatService {

    private final LegalQaAgent legalQaAgent;
    private final ConversationMemoryService conversationMemoryService;

    public LegalChatService(LegalQaAgent legalQaAgent, ConversationMemoryService conversationMemoryService) {
        this.legalQaAgent = legalQaAgent;
        this.conversationMemoryService = conversationMemoryService;
    }

    public LegalChatResponse chat(String requestedConversationId, String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question is required");
        }
        String conversationId = conversationMemoryService.resolveConversationId(requestedConversationId);
        String context = conversationMemoryService.contextFor(conversationId);
        String answer = legalQaAgent.answer(question.trim(), context);
        conversationMemoryService.remember(conversationId, question.trim(), answer);
        return new LegalChatResponse(true, conversationId, answer, null);
    }
}
