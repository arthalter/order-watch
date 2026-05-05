package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.api.ApiResponse;
import com.orderwatch.backend.infrastructure.rag.SopCollectionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SopCollectionController {

    private final SopCollectionService sopCollectionService;

    public SopCollectionController(SopCollectionService sopCollectionService) {
        this.sopCollectionService = sopCollectionService;
    }

    @PostMapping(value = "/api/sop/collection/init", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<SopCollectionService.InitResult> initializeCollection() {
        return ApiResponse.ok(sopCollectionService.initializeCollection());
    }

    @GetMapping(value = "/api/sop/collection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<CollectionStatus> collectionStatus() {
        return ApiResponse.ok(new CollectionStatus(sopCollectionService.hasCollection()));
    }

    record CollectionStatus(boolean exists) {
    }
}
