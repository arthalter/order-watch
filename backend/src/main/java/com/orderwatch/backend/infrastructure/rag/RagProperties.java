package com.orderwatch.backend.infrastructure.rag;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
@Getter
@Setter
public class RagProperties {

    /**
     * Directory path that contains SOP markdown docs.
     * Example: ./order-docs (when started from backend/)
     */
    private String docsPath;
}

