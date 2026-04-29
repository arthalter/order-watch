package com.orderwatch.backend.application.rag;

/**
 * 文档分片（chunk）：
 * <p>
 * 这是一种“给机器用”的中间结构：
 * - {@code text} 会用于后续 embedding（向量化）
 * - {@code documentId}/{@code chunkIndex} 用于追踪来源，便于调试与解释命中结果
 */
public record DocumentChunk(
        String documentId,
        int chunkIndex,
        String text
) {
}

