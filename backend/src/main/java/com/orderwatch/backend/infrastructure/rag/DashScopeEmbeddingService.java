package com.orderwatch.backend.infrastructure.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderwatch.backend.application.rag.EmbeddingService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * DashScopeEmbeddingService：
 * <p>
 * 使用阿里云 DashScope 的 OpenAI 兼容接口生成 embedding（真向量）。
 * <p>
 * 说明：
 * - 本实现尽量保持“微型项目”风格：不引入新依赖，直接用 JDK HttpClient + Jackson
 * - 返回 float[]，便于后续写入 Milvus SDK（通常需要 float 向量）
 */
public class DashScopeEmbeddingService implements EmbeddingService {

    private final EmbeddingProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public DashScopeEmbeddingService(EmbeddingProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getDashscope().getTimeoutMs()))
                .build();
    }

    DashScopeEmbeddingService(EmbeddingProperties properties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }

        EmbeddingProperties.Dashscope dashscope = properties.getDashscope();
        if (dashscope.getApiKey() == null || dashscope.getApiKey().isBlank()) {
            throw new IllegalStateException("embedding.dashscope.api-key is required");
        }
        if (dashscope.getBaseUrl() == null || dashscope.getBaseUrl().isBlank()) {
            throw new IllegalStateException("embedding.dashscope.base-url is required");
        }
        if (dashscope.getModel() == null || dashscope.getModel().isBlank()) {
            throw new IllegalStateException("embedding.dashscope.model is required");
        }
        if (dashscope.getTimeoutMs() <= 0) {
            throw new IllegalStateException("embedding.dashscope.timeout-ms must be positive");
        }
        if (properties.getDimension() <= 0) {
            throw new IllegalStateException("embedding.dimension must be positive");
        }

        String url = normalizeBaseUrl(dashscope.getBaseUrl()) + "/embeddings";
        try {
            String requestJson = objectMapper.createObjectNode()
                    .put("model", dashscope.getModel())
                    .put("input", text)
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(dashscope.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + dashscope.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("DashScope embeddings failed: HTTP " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data0 = root.path("data").path(0);
            JsonNode embeddingNode = data0.path("embedding");
            if (!embeddingNode.isArray()) {
                throw new IllegalStateException("DashScope embeddings response missing data[0].embedding");
            }

            int expectedDim = properties.getDimension();
            int actualDim = embeddingNode.size();
            if (expectedDim > 0 && actualDim != expectedDim) {
                throw new IllegalStateException("Embedding dimension mismatch: expected " + expectedDim + " but got " + actualDim);
            }

            float[] vector = new float[actualDim];
            for (int i = 0; i < actualDim; i++) {
                vector[i] = (float) embeddingNode.get(i).asDouble();
            }
            return vector;
        } catch (Exception e) {
            throw new IllegalStateException("DashScope embeddings request failed", e);
        }
    }

    private static String normalizeBaseUrl(String baseUrl) {
        String trimmed = baseUrl.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
