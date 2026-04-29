package com.orderwatch.backend.infrastructure.rag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownDocumentReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void readsMarkdownFilesFromDocsPath() throws Exception {
        Path a = tempDir.resolve("a.md");
        Path b = tempDir.resolve("b.MD");
        Path c = tempDir.resolve("c.txt");

        Files.writeString(a, "# A\nhello");
        Files.writeString(b, "# B\nworld");
        Files.writeString(c, "ignore");

        RagProperties properties = new RagProperties();
        properties.setDocsPath(tempDir.toString());
        MarkdownDocumentReader reader = new MarkdownDocumentReader(properties);

        List<MarkdownDocumentReader.MarkdownDocument> docs = reader.readAll();

        assertThat(docs).hasSize(2);
        assertThat(docs.get(0).filename()).isEqualTo("a.md");
        assertThat(docs.get(0).content()).contains("hello");
        assertThat(docs.get(1).filename()).isEqualTo("b.MD");
        assertThat(docs.get(1).content()).contains("world");
    }
}

