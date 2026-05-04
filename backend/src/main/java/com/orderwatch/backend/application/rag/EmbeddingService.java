package com.orderwatch.backend.application.rag;

/**
 * EmbeddingService：把文本变成向量（embedding）。
 * <p>
 * 本项目里 Embedding 的作用：
 * - 文档 chunk -> 向量 -> 写入 Milvus
 * - 用户 query -> 向量 -> 在 Milvus 做向量检索
 */
public interface EmbeddingService {

    /**
     * 将文本转换为向量。
     *
     * @param text 输入文本（不能为空）
     * @return embedding 向量（维度由配置决定，例如 1024）
     */
    float[] embed(String text);
}

