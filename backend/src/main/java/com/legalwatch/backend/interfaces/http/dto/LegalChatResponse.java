package com.legalwatch.backend.interfaces.http.dto;

public record LegalChatResponse(
        boolean success,
        String answer,
        String errorMessage
) {
}
