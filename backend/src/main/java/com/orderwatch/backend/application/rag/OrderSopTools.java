package com.orderwatch.backend.application.rag;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderSopTools {

    private final SopSearchService sopSearchService;

    public OrderSopTools(SopSearchService sopSearchService) {
        this.sopSearchService = sopSearchService;
    }

    public List<SopSearchService.SopSearchResult> queryOrderSop(String query) {
        return sopSearchService.search(query, null);
    }
}
