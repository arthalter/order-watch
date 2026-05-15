package com.legalwatch.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "milvus.initialize-on-startup=false",
        "spring.ai.openai.api-key=test-key"
})
class LegalWatchBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
