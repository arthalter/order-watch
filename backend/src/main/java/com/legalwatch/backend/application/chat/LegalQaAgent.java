package com.legalwatch.backend.application.chat;

import com.legalwatch.backend.application.rag.LegalSopTools;
import com.legalwatch.backend.application.rag.SopSearchService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LegalQaAgent {

    private final LegalSopTools legalSopTools;

    public LegalQaAgent(LegalSopTools legalSopTools) {
        this.legalSopTools = legalSopTools;
    }

    public String answer(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question is required");
        }

        List<SopSearchService.SopSearchResult> results = legalSopTools.queryLegalSop(question);
        if (results.isEmpty()) {
            return "当前已入库文档中没有检索到可回答该问题的内容。";
        }

        StringBuilder answer = new StringBuilder();
        answer.append("基于已入库文档，检索到以下相关内容：\n\n");
        for (int i = 0; i < results.size(); i++) {
            SopSearchService.SopSearchResult result = results.get(i);
            answer.append(i + 1)
                    .append(". ")
                    .append(result.title())
                    .append("（")
                    .append(result.fileName())
                    .append("#")
                    .append(result.chunkIndex())
                    .append("）\n")
                    .append(result.content())
                    .append("\n\n");
        }
        answer.append("以上回答仅基于当前入库文档片段，不构成正式法律意见。");
        return answer.toString();
    }
}
