package com.orderwatch.backend.infrastructure.rag;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * embedding 配置：
 * <p>
 * - provider：选择使用哪种 embedding 方式（本任务实现 dashscope）
 * - dimension：期望的向量维度（例如 1024）
 */
@ConfigurationProperties(prefix = "embedding")
@Getter
@Setter
public class EmbeddingProperties {

    /**
     * fake | dashscope
     */
    private String provider;

    private int dimension;

    private Dashscope dashscope = new Dashscope();

    @Getter
    @Setter
    public static class Dashscope {
        /**
         * DashScope API Key（敏感信息，建议放到 application-dev.yml，并且不要提交到 Git）。
         */
        private String apiKey;

        private String model;

        private String baseUrl;

        private int timeoutMs;
    }
}
