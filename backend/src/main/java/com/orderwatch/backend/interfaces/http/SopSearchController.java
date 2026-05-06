package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.api.ApiResponse;
import com.orderwatch.backend.api.ErrorCode;
import com.orderwatch.backend.application.rag.SopSearchService;
import com.orderwatch.backend.interfaces.http.dto.SopSearchResultResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SopSearchController {

    private final SopSearchService sopSearchService;

    public SopSearchController(SopSearchService sopSearchService) {
        this.sopSearchService = sopSearchService;
    }

    @GetMapping(value = "/api/sop/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<List<SopSearchResultResponse>> search(
            @RequestParam String query,
            @RequestParam(required = false) Integer topK
    ) {
        try {
            List<SopSearchResultResponse> results = sopSearchService.search(query, topK).stream()
                    .map(result -> new SopSearchResultResponse(
                            result.fileName(),
                            result.chunkIndex(),
                            result.score(),
                            result.content(),
                            result.title()
                    ))
                    .toList();
            return ApiResponse.ok(results);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, ex.getMessage());
        }
    }
}
