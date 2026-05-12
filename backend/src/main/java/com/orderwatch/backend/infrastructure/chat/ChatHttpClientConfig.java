package com.orderwatch.backend.infrastructure.chat;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class ChatHttpClientConfig {

    @Bean
    public RestClientCustomizer chatRestClientCustomizer(
            @Value("${order.chat.connect-timeout-ms:10000}") int connectTimeoutMs,
            @Value("${order.chat.read-timeout-ms:120000}") int readTimeoutMs
    ) {
        return builder -> {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
            requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
            builder.requestFactory(requestFactory);
        };
    }
}
