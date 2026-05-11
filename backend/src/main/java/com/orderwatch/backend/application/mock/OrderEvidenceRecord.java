package com.orderwatch.backend.application.mock;

public record OrderEvidenceRecord(
        String evidenceId,
        String anomalyId,
        String topic,
        String content
) {
}
