package com.legalwatch.backend.interfaces.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(LegalApiIntegrationTest.TestChatModelConfig.class)
class LegalApiIntegrationTest {

    private static final Path DOCS_DIR = Path.of("target/legal-api-it-docs");
    private static final String COLLECTION_NAME = "legal_api_it_" + System.currentTimeMillis();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("milvus.collection-name", () -> COLLECTION_NAME);
        registry.add("milvus.initialize-on-startup", () -> "false");
        registry.add("rag.docs-path", () -> DOCS_DIR.toString());
        registry.add("embedding.provider", () -> "fake");
        registry.add("embedding.dimension", () -> "1024");
        registry.add("spring.ai.openai.api-key", () -> "test-key");
    }

    @BeforeAll
    static void cleanDocsDir() throws Exception {
        if (Files.exists(DOCS_DIR)) {
            try (var paths = Files.walk(DOCS_DIR)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception ex) {
                                throw new IllegalStateException(ex);
                            }
                        });
            }
        }
        Files.createDirectories(DOCS_DIR);
    }

    @Test
    void allHttpApisRunAgainstRealMilvus() throws Exception {
        JsonNode health = getJson("/health");
        assertOk(health);
        assertThat(health.path("data").asText()).isEqualTo("ok");

        JsonNode milvusHealth = getJson("/health/milvus");
        assertOk(milvusHealth);
        assertThat(milvusHealth.path("data").path("connected").asBoolean()).isTrue();

        JsonNode init = postJson("/api/sop/collection/init", null);
        assertOk(init);
        assertThat(init.path("data").path("collectionName").asText()).isEqualTo(COLLECTION_NAME);

        JsonNode collection = getJson("/api/sop/collection");
        assertOk(collection);
        assertThat(collection.path("data").path("exists").asBoolean()).isTrue();

        JsonNode localIndex = postJson("/api/sop/index-local-docs", null);
        assertOk(localIndex);
        assertThat(localIndex.path("data").path("collectionName").asText()).isEqualTo(COLLECTION_NAME);
        assertThat(localIndex.path("data").path("documentCount").asInt()).isZero();

        JsonNode upload = uploadMarkdown();
        assertOk(upload);
        assertThat(upload.path("data").path("fileName").asText()).isEqualTo("guarantee.md");
        assertThat(upload.path("data").path("collectionName").asText()).isEqualTo(COLLECTION_NAME);
        assertThat(upload.path("data").path("chunkCount").asInt()).isGreaterThan(0);
        assertThat(upload.path("data").path("upsertedCount").asLong()).isGreaterThan(0);

        JsonNode search = getJsonWithData("/api/sop/search?query=保证责任&topK=3");
        assertOk(search);
        assertThat(search.path("data").get(0).path("content").asText()).contains("保证责任");

        JsonNode chat = postJson("/api/legal_chat", "{\"question\":\"保证责任怎么查询？\"}");
        assertOk(chat);
        assertThat(chat.path("data").path("success").asBoolean()).isTrue();
        assertThat(chat.path("data").path("conversationId").asText()).startsWith("conv_");
        assertThat(chat.path("data").path("answer").asText()).contains(
                "## 回答摘要",
                "## 依据来源",
                "guarantee.md#chunk-",
                "保证责任",
                "## 使用边界",
                "不构成正式法律意见"
        );

        JsonNode followUp = postJson("/api/legal_chat", """
                {"conversationId":"%s","question":"其中的保证期间是什么？"}
                """.formatted(chat.path("data").path("conversationId").asText()));
        assertOk(followUp);
        assertThat(followUp.path("data").path("conversationId").asText())
                .isEqualTo(chat.path("data").path("conversationId").asText());
    }

    private JsonNode getJson(String path) throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(path, String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return objectMapper.readTree(response.getBody());
    }

    private JsonNode getJsonWithData(String path) throws Exception {
        JsonNode last = null;
        for (int i = 0; i < 5; i++) {
            last = getJson(path);
            if (last.path("data").isArray() && !last.path("data").isEmpty()) {
                return last;
            }
            Thread.sleep(300);
        }
        return last;
    }

    private JsonNode postJson(String path, String body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body == null ? "" : body, headers);
        ResponseEntity<String> response = restTemplate.exchange(path, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return objectMapper.readTree(response.getBody());
    }

    private JsonNode uploadMarkdown() throws Exception {
        String markdown = """
                # 保证合同查询说明

                保证责任应当查询保证方式、保证期间、主债务范围、债权人通知义务和争议解决条款。

                如果文件没有写明保证方式，需要结合上下文继续检索相关条款。
                """;

        ByteArrayResource resource = new ByteArrayResource(markdown.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "guarantee.md";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/documents/upload", request, String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return objectMapper.readTree(response.getBody());
    }

    private static void assertOk(JsonNode root) {
        assertThat(root.path("success").asBoolean()).isTrue();
        assertThat(root.path("code").asInt()).isZero();
    }

    @TestConfiguration
    static class TestChatModelConfig {

        @Bean
        @Primary
        ChatModel legalChatTestModel() {
            return prompt -> new ChatResponse(List.of(new Generation(new AssistantMessage("""
                    ## 回答摘要
                    保证责任需要结合入库材料核查保证方式、保证期间与主债务范围。

                    ## 依据来源
                    - guarantee.md#chunk-0

                    ## 使用边界
                    本回答仅用于文档查询与学习演示，不构成正式法律意见。
                    """))));
        }
    }
}
