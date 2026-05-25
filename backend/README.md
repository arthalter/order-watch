# LegalWatch Mini Backend

Spring Boot 3.x + Java 17 + Maven 后端，负责文档上传、Markdown 读取与切片、Embedding、Milvus 入库/检索，以及带来源定位的法律文档查询接口。当前查询链路包含轻量问题重写、多路召回和规则重排；问答由 Spring AI `ChatClient` 驱动模型调用 `LegalSopTools`，并支持内存级短期会话记忆。

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

问答接口需要设置 `DASHSCOPE_API_KEY`。`LegalQaAgent` 会向 `ChatClient` 注册 `LegalSopTools`，由模型在回答前调用工具检索已入库文档，并按提示词返回来源定位与使用边界。

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
    "conversationId": "conv_f65dbd88-...",
    "answer": "## 回答摘要\n...\n\n## 依据来源\n\n[1] ...\n- 来源定位：sop-legal-answer-style.md#chunk-1\n...\n\n## 使用边界\n...",
    "errorMessage": null
  }
}
```

首次请求不提供 `conversationId` 时，服务端会生成并返回一个值。后续请求携带相同值，即可让查询参考该会话最近 5 轮回答摘要：

```bash
curl -X POST http://localhost:8080/api/legal_chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"conv_替换为首次返回值","question":"其中第二条是什么意思？"}'
```

## 演示脚本

```bash
docker compose up -d

EMBEDDING_PROVIDER=fake ./mvnw spring-boot:run

curl -X POST http://localhost:8080/api/sop/index-local-docs

curl "http://localhost:8080/api/sop/search?query=正式法律意见的答复边界&topK=3"

curl -X POST http://localhost:8080/api/legal_chat \
  -H "Content-Type: application/json" \
  -d '{"question":"正式法律意见的答复边界是什么？"}'

```

推荐演示问题：

| 问题 | 期望主题文档 |
| --- | --- |
| `保证责任需要核查哪些内容？` | `sop-guarantee-liability.md` |
| `付款期限和验收节点怎么核查？` | `sop-contract-payment-clause.md` |
| `劳动争议证据应该如何整理？` | `sop-labor-dispute-evidence.md` |
| `借款诉讼时效需要查什么？` | `sop-loan-limitation-period.md` |
| `正式法律意见的答复边界是什么？` | `sop-legal-answer-style.md` |

使用 `EMBEDDING_PROVIDER=fake` 时，系统重点验证流程与规则重排；要展示更稳定的语义召回结果，请使用真实 embedding 并重新执行入库。

## 真实接口测试

`./mvnw test` 会执行 `LegalApiIntegrationTest`；为确保测试可复现，问答部分注入测试 `ChatModel`，检索与存储仍连接本机 Docker Milvus：

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
- 复用 `conversationId` 发起追问
- 执行 `RagDemoEvaluationTest`，验证五类演示问题经规则重排后对应主题文档排名靠前

## 当前完成度

- Markdown 文档上传：已完成。
- 本地目录文档入库：已完成。
- Markdown 分片、Embedding、Milvus upsert/search：已完成。
- 问题重写、多路召回与轻量规则重排：已完成。
- 唯一 Tool：`LegalSopTools`，由 `ChatClient` 注册给模型，用于检索已入库文档。
- 模型驱动的引用式回答、`/api/legal_chat` 文档查询对话与短期会话记忆：已完成，系统提示词约束回答必须依据 Tool 返回材料并附来源定位。

## 代码结构

- `com.legalwatch.backend.interfaces`：HTTP Controller / DTO
- `com.legalwatch.backend.application`：文档入库、检索、问答编排
- `com.legalwatch.backend.infrastructure`：Milvus、文件读取、Embedding 实现
