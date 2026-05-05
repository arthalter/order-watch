package com.orderwatch.backend.infrastructure.rag;

import com.orderwatch.backend.infrastructure.milvus.MilvusProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SopCollectionInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SopCollectionInitializer.class);

    private final MilvusProperties milvusProperties;
    private final SopCollectionService sopCollectionService;

    public SopCollectionInitializer(MilvusProperties milvusProperties, SopCollectionService sopCollectionService) {
        this.milvusProperties = milvusProperties;
        this.sopCollectionService = sopCollectionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!milvusProperties.isInitializeOnStartup()) {
            log.info("Milvus SOP collection initialization is disabled");
            return;
        }

        try {
            SopCollectionService.InitResult result = sopCollectionService.initializeCollection();
            log.info("Milvus SOP collection initialization finished: {}", result);
        } catch (Exception e) {
            log.warn("Milvus SOP collection initialization skipped: {}", e.getMessage());
        }
    }
}
