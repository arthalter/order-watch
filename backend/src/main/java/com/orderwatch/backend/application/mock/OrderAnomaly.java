package com.orderwatch.backend.application.mock;

public record OrderAnomaly(
        String anomalyId,
        String orderId,
        String userId,
        String productId,
        String skuId,
        String anomalyType,
        String metric,
        String currentValue,
        String baselineValue,
        String timeWindow,
        String severity,
        String summary
) {
}
