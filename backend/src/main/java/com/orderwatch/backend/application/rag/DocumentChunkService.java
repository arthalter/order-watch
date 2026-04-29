package com.orderwatch.backend.application.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * DocumentChunkService：把 Markdown 文档内容切成多个“可用于检索”的 chunk。
 * <p>
 * 为什么要分片？
 * 1) embedding 的输入长度有限（真实 embedding 服务通常会限制 token 数）
 * 2) 检索命中时希望返回“最相关的一段”，而不是整篇文档
 * 3) chunk 带上来源信息，后续可以解释“为什么命中”
 * <p>
 * 本项目是学习型微型项目，优先选择简单、可读的实现方式：
 * - 先按空行切成段落（paragraph）
 * - 再把多个段落拼成 chunk，保证 chunk 不超过 maxChunkChars
 * - 如果某个段落本身太长，则直接按字符数硬切
 * <p>
 * 注意：
 * - 这里用“字符数”做限制（不是 token），因为早期我们只需要一个稳定的、可预测的分片方式
 * - 后续如果接入真实 embedding，可以再把规则改成 token 级别
 */
@Service
public class DocumentChunkService {

    /**
     * 单个 chunk 的最大字符数（可在 application.yml 配置）：
     * <p>
     * rag:
     *   chunk-max-chars: 20
     */
    private final int maxChunkChars;

    public DocumentChunkService(@Value("${rag.chunk-max-chars:1200}") int maxChunkChars) {
        this.maxChunkChars = maxChunkChars;
    }

    public List<DocumentChunk> chunk(String documentId, String content) {
        return chunk(documentId, content, maxChunkChars);
    }

    public List<DocumentChunk> chunk(String documentId, String content, int maxChunkChars) {
        if (documentId == null || documentId.isBlank()) {
            throw new IllegalArgumentException("documentId is required");
        }
        if (content == null || content.isBlank()) {
            return List.of();
        }
        if (maxChunkChars <= 0) {
            throw new IllegalArgumentException("maxChunkChars must be positive");
        }

        // 1) 按空行切段落：兼容 Windows/Unix 换行
        String normalized = content.replace("\r\n", "\n").replace("\r", "\n");
        String[] paragraphs = normalized.split("\\n\\s*\\n+");

        // 2) 把段落拼成 chunk
        List<DocumentChunk> chunks = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        int chunkIndex = 0;

        for (String paragraph : paragraphs) {
            String p = paragraph == null ? "" : paragraph.trim();
            if (p.isEmpty()) {
                continue;
            }

            // 如果段落本身就超过最大长度：先把 buffer flush，再硬切该段落
            if (p.length() > maxChunkChars) {
                chunkIndex = flushBufferAsChunk(documentId, chunks, buffer, chunkIndex);

                int start = 0;
                while (start < p.length()) {
                    int end = Math.min(start + maxChunkChars, p.length());
                    String piece = p.substring(start, end).trim();
                    if (!piece.isEmpty()) {
                        chunks.add(new DocumentChunk(documentId, chunkIndex++, piece));
                    }
                    start = end;
                }
                continue;
            }

            // 尝试把该段落追加到 buffer 中；超过就先 flush，再写入
            int extraLen = (buffer.isEmpty() ? 0 : 2) + p.length(); // 2 = 段落间用空行分隔
            if (buffer.length() + extraLen > maxChunkChars) {
                chunkIndex = flushBufferAsChunk(documentId, chunks, buffer, chunkIndex);
            }

            if (!buffer.isEmpty()) {
                buffer.append("\n\n");
            }
            buffer.append(p);
        }

        flushBufferAsChunk(documentId, chunks, buffer, chunkIndex);
        return chunks;
    }

    private static int flushBufferAsChunk(
            String documentId,
            List<DocumentChunk> chunks,
            StringBuilder buffer,
            int chunkIndex
    ) {
        if (!buffer.isEmpty()) {
            String text = buffer.toString().trim();
            if (!text.isEmpty()) {
                chunks.add(new DocumentChunk(documentId, chunkIndex++ , text));
            }
            buffer.setLength(0);
        }
        return chunkIndex;
    }
}
