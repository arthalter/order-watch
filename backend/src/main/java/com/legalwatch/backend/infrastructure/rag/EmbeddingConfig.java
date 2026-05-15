package com.legalwatch.backend.infrastructure.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.legalwatch.backend.application.rag.EmbeddingService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmbeddingProperties.class)
public class EmbeddingConfig {

    @Bean
    public EmbeddingService embeddingService(EmbeddingProperties properties, ObjectMapper objectMapper) {
        String provider = properties.getProvider();
        if (provider == null || provider.isBlank()) {
            throw new IllegalStateException("embedding.provider is required");
        }

        if ("dashscope".equalsIgnoreCase(provider)) {
            return new DashScopeEmbeddingService(properties, objectMapper);
        }
        if ("fake".equalsIgnoreCase(provider)) {
            return new FakeEmbeddingService(properties.getDimension());
        }

        throw new IllegalStateException("Unsupported embedding.provider: " + provider);
    }
}
