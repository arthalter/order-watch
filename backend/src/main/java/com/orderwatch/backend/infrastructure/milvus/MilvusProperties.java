package com.orderwatch.backend.infrastructure.milvus;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "milvus")
@Getter
@Setter
public class MilvusProperties {

    private String host;
    private int port;
    private String collectionName;
    private int vectorDimension;
    private boolean initializeOnStartup;
}
