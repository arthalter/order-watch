package com.orderwatch.backend.infrastructure.milvus;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MilvusProperties.class)
public class MilvusConfig {
}

