package com.orderwatch.backend.application.chat;

import com.orderwatch.backend.interfaces.http.dto.OpsChatResponse;
import org.springframework.stereotype.Service;

@Service
public class OpsChatService {

    private final SimpleOrderAgent simpleOrderAgent;

    public OpsChatService(SimpleOrderAgent simpleOrderAgent) {
        this.simpleOrderAgent = simpleOrderAgent;
    }

    public OpsChatResponse chat(String question) {
        String answer = simpleOrderAgent.answer(question);
        return new OpsChatResponse(true, answer, null);
    }
}
