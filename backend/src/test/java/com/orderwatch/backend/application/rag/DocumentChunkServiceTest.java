package com.orderwatch.backend.application.rag;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentChunkServiceTest {

    private final DocumentChunkService service = new DocumentChunkService(1200);

    @Test
    void returnsEmptyWhenContentBlank() {
        assertThat(service.chunk("doc-1", "   ")).isEmpty();
    }

    @Test
    void requiresDocumentId() {
        assertThatThrownBy(() -> service.chunk(" ", "hi"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void splitsByBlankLinesAndMergesIntoChunks() {
        String content = """
                # Title

                Para A line1
                Para A line2

                Para B

                Para C
                """;

        var chunks = service.chunk("doc-1", content, 40);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.get(0).documentId()).isEqualTo("doc-1");
        assertThat(chunks.get(0).chunkIndex()).isEqualTo(0);
        assertThat(chunks.get(0).text()).contains("# Title");
        assertThat(chunks.get(0).text().length()).isLessThanOrEqualTo(40);
        assertThat(chunks.get(1).chunkIndex()).isEqualTo(1);
    }

    @Test
    void hardSplitsWhenSingleParagraphTooLong() {
        String content = "a".repeat(25);
        var chunks = service.chunk("doc-1", content, 10);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).text()).hasSize(10);
        assertThat(chunks.get(1).text()).hasSize(10);
        assertThat(chunks.get(2).text()).hasSize(5);
    }

    @Test
    void chunksRealSopMarkdownFile() throws Exception {
        // 读取真实 SOP 文件，验证分片逻辑在“真实输入”下仍然稳定可用。
        // 注意：这里不依赖 Spring 配置，只验证 chunk(service, content, max) 的核心逻辑。
        Path sopPath = Path.of("order-docs/sop-large-order.md");
        String content = Files.readString(sopPath);

        int max = 120;
        var chunks = service.chunk("sop-large-order.md", content, max);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.get(0).documentId()).isEqualTo("sop-large-order.md");
        assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.text()).isNotBlank());
        assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.text().length()).isLessThanOrEqualTo(max));

        // 粗略校验：关键片段应该仍然能在某个 chunk 中被找到（避免分片后丢内容）
        assertThat(chunks).anySatisfy(chunk -> assertThat(chunk.text()).contains("大额订单异常"));
    }
}
