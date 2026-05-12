package com.orderwatch.backend.interfaces.http.dto;

public record OpsChatResponse(
        boolean success,
        String answer,
        String errorMessage
) {
}
