package com.legalwatch.backend.application.chat;

import com.legalwatch.backend.interfaces.http.dto.LegalChatResponse;
import org.springframework.stereotype.Service;

@Service
public class LegalChatService {

    private final LegalQaAgent legalQaAgent;

    public LegalChatService(LegalQaAgent legalQaAgent) {
        this.legalQaAgent = legalQaAgent;
    }

    public LegalChatResponse chat(String question) {
        String answer = legalQaAgent.answer(question);
        return new LegalChatResponse(true, answer, null);
    }
}
