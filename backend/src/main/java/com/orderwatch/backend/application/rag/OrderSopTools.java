package com.orderwatch.backend.application.rag;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderSopTools {

    private final SopSearchService sopSearchService;

    public OrderSopTools(SopSearchService sopSearchService) {
        this.sopSearchService = sopSearchService;
    }

    @Tool(description = "检索异常订单处理 SOP 规则。适用于用户询问大额订单、频繁取消、同地址多账号等异常应该怎么处理。")
    public List<SopSearchService.SopSearchResult> queryOrderSop(
            @ToolParam(description = "SOP 检索问题，例如：大额订单异常应该怎么处理。") String query
    ) {
        return sopSearchService.search(query, null);
    }
}
