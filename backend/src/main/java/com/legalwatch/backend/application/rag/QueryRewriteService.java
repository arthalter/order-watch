package com.legalwatch.backend.application.rag;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class QueryRewriteService {

    public List<String> rewrite(String query) {
        String normalized = requireQuery(query);
        LinkedHashSet<String> queries = new LinkedHashSet<>();
        queries.add(normalized);

        List<String> legalTerms = legalTerms(normalized);
        if (!legalTerms.isEmpty()) {
            queries.add(String.join(" ", legalTerms));
        }
        return List.copyOf(queries);
    }

    public List<String> keywords(String query) {
        String normalized = requireQuery(query);
        LinkedHashSet<String> keywords = new LinkedHashSet<>(legalTerms(normalized));
        String compact = normalized
                .replaceAll("[？?，,。.!！：:\\s]", "")
                .replaceAll("(怎么查询|如何查询|怎么处理|是什么意思|是什么|请解释|具体)$", "");
        if (compact.length() >= 2) {
            keywords.add(compact);
        }
        return List.copyOf(keywords);
    }

    private List<String> legalTerms(String query) {
        Set<String> terms = new LinkedHashSet<>();
        if (containsAny(query, "保证", "担保")) {
            terms.addAll(List.of("保证责任", "保证方式", "保证期间", "主债务范围"));
        }
        if (containsAny(query, "付款", "支付", "验收")) {
            terms.addAll(List.of("付款条款", "付款期限", "验收", "违约责任"));
        }
        if (containsAny(query, "劳动", "解除", "加班", "调岗")) {
            terms.addAll(List.of("劳动争议", "证据", "劳动合同", "解除"));
        }
        if (containsAny(query, "时效", "借款", "诉讼")) {
            terms.addAll(List.of("诉讼时效", "借款", "催收", "还款"));
        }
        if (containsAny(query, "法律意见", "答复边界")) {
            terms.addAll(List.of("正式法律意见", "答复边界", "文档查询"));
        }
        return new ArrayList<>(terms);
    }

    private static boolean containsAny(String query, String... words) {
        for (String word : words) {
            if (query.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private static String requireQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query is required");
        }
        return query.trim();
    }
}
