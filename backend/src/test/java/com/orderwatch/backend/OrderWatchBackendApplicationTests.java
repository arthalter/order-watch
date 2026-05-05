package com.orderwatch.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "milvus.initialize-on-startup=false")
class OrderWatchBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
