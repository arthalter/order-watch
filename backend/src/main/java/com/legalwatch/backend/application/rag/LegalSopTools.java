package com.legalwatch.backend.application.rag;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LegalSopTools {

    private final SopSearchService sopSearchService;

    public LegalSopTools(SopSearchService sopSearchService) {
        this.sopSearchService = sopSearchService;
    }

    @Tool(description = "检索已入库法律文档和 SOP 片段。适用于用户根据上传或本地入库文档进行查询问答。")
    public List<SopSearchService.SopSearchResult> queryLegalSop(
            @ToolParam(description = "检索问题，例如：保证责任如何约定。") String query
    ) {
        return sopSearchService.search(query, null);
    }
}
