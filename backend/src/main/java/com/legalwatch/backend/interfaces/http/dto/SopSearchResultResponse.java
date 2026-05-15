package com.legalwatch.backend.interfaces.http.dto;

public record SopSearchResultResponse(
        String fileName,
        int chunkIndex,
        double score,
        String content,
        String title
) {
}
