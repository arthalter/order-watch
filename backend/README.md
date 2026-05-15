# LegalWatch Mini Backend

Spring Boot 3.x + Java 17 + Maven 后端，负责文档上传、Markdown 读取与切片、Embedding、Milvus 入库/检索，以及法律文档查询对话接口。

## 本地运行

需要安装 Java 17 和 Docker。

- 启动 Milvus：`docker compose up -d`
- 使用本地 fake embedding 启动：`EMBEDDING_PROVIDER=fake ./mvnw spring-boot:run`
- 使用百炼 embedding 启动：`DASHSCOPE_API_KEY=你的百炼APIKey ./mvnw spring-boot:run`
- 测试：`./mvnw test`

启动后：

- `GET http://localhost:8080/health`
- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/health/milvus`
- Milvus collection：`legal_sop_chunks`
- 手动初始化：`curl -X POST http://localhost:8080/api/sop/collection/init`
- 查看 collection：`curl http://localhost:8080/api/sop/collection`
- 上传并入库 Markdown：`curl -X POST http://localhost:8080/api/documents/upload -F "file=@/path/to/legal-doc.md"`
- 入库本地目录：`curl -X POST http://localhost:8080/api/sop/index-local-docs`
- 检索文档：`curl "http://localhost:8080/api/sop/search?query=保证责任&topK=3"`
- 文档问答：`curl -X POST http://localhost:8080/api/legal_chat -H "Content-Type: application/json" -d '{"question":"保证责任怎么查询？"}'`

## 接口返回结构

统一外层响应为 `ApiResponse`：

```json
{
  "success": true,
  "code": 0,
  "message": "ok",
  "data": {}
}
```

文档问答：

```json
{
  "success": true,
  "code": 0,
  "message": "ok",
  "data": {
    "success": true,
    "answer": "基于已入库文档，检索到以下相关内容...",
    "errorMessage": null
  }
}
```

## 演示脚本

```bash
docker compose up -d

EMBEDDING_PROVIDER=fake ./mvnw spring-boot:run

curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@legal-docs/sop-legal-answer-style.md"

curl "http://localhost:8080/api/sop/search?query=正式法律意见&topK=3"

curl -X POST http://localhost:8080/api/legal_chat \
  -H "Content-Type: application/json" \
  -d '{"question":"正式法律意见的答复边界是什么？"}'
```

## 真实接口测试

`./mvnw test` 会执行 `LegalApiIntegrationTest`：

- 随机端口启动真实 Spring Boot
- 连接本机 Docker Milvus
- 调用 `/health`
- 调用 `/health/milvus`
- 调用 `/api/sop/collection/init`
- 调用 `/api/sop/collection`
- 调用 `/api/documents/upload`
- 调用 `/api/sop/index-local-docs`
- 调用 `/api/sop/search`
- 调用 `/api/legal_chat`

## 当前完成度

- Markdown 文档上传：已完成。
- 本地目录文档入库：已完成。
- Markdown 分片、Embedding、Milvus upsert/search：已完成。
- 唯一 Tool：`LegalSopTools`，用于检索已入库文档。
- `/api/legal_chat` 文档查询对话：已完成，只依赖已入库文档检索结果。

## 代码结构

- `com.legalwatch.backend.interfaces`：HTTP Controller / DTO
- `com.legalwatch.backend.application`：文档入库、检索、问答编排
- `com.legalwatch.backend.infrastructure`：Milvus、文件读取、Embedding 实现
