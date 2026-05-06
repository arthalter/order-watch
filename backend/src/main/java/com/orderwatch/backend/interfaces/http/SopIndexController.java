package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.api.ApiResponse;
import com.orderwatch.backend.application.rag.SopIndexService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SopIndexController {

    private final SopIndexService sopIndexService;

    public SopIndexController(SopIndexService sopIndexService) {
        this.sopIndexService = sopIndexService;
    }

    @PostMapping(value = "/api/sop/index-local-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<SopIndexService.IndexResult> indexLocalDocs() {
        return ApiResponse.ok(sopIndexService.indexLocalDocs());
    }
}
