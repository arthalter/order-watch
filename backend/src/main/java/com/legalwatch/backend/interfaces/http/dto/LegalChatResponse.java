package com.legalwatch.backend.interfaces.http.dto;

public record LegalChatResponse(
        boolean success,
        String conversationId,
        String answer,
        String errorMessage
) {
}
