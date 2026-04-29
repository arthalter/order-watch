package com.orderwatch.backend.infrastructure.milvus;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MilvusPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(MilvusConfig.class);

    @Test
    void bindsMilvusProperties() {
        contextRunner
                .withPropertyValues(
                        "milvus.host=localhost",
                        "milvus.port=19530",
                        "milvus.collection-name=order_sop_chunks",
                        "milvus.vector-dimension=1024"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(MilvusProperties.class);
                    MilvusProperties properties = context.getBean(MilvusProperties.class);
                    assertThat(properties.getHost()).isEqualTo("localhost");
                    assertThat(properties.getPort()).isEqualTo(19530);
                    assertThat(properties.getCollectionName()).isEqualTo("order_sop_chunks");
                    assertThat(properties.getVectorDimension()).isEqualTo(1024);
                });
    }
}

