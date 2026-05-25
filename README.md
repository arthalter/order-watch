# LegalWatch Mini (Backend-Focused Monorepo)

`LegalWatch Mini` 是一个学习型 Agent + Tool + Milvus RAG 骨架项目，用于演示法律 Markdown 入库、Milvus 检索和带引用的法律文档问答。检索链路支持轻量问题重写、多路召回和规则重排，问答链路由 Spring AI `ChatClient` 驱动模型调用 `LegalSopTools`，并支持短期会话记忆。

当前后端主链路：

```text
上传 / 本地文档
→ Markdown 读取与切片
→ Embedding
→ Milvus 向量入库
→ 用户问题
→ 问题重写 / 多路召回 / 轻量重排
→ ChatClient 驱动模型调用唯一的 LegalSopTools
→ 带文档定位引用的回答
```

## 目录

- `backend/`：Spring Boot 后端，包含 Milvus RAG、文档上传、检索 Tool、Agent 和 API
- `frontend/`：Vue 3 控制台，支持检索、法律问答与当前会话连续追问
- `backend/legal-docs/`：本地 Markdown 文档目录

## 本地运行

```bash
cd backend
docker compose up -d
EMBEDDING_PROVIDER=fake ./mvnw spring-boot:run
```

使用真实百炼 embedding 时：

```bash
export DASHSCOPE_API_KEY=你的百炼APIKey
./mvnw spring-boot:run
```

前端演示：

```bash
cd frontend
npm install
npm run dev
```

## 五分钟演示

先在 `backend/` 下执行本地示例文档入库：

```bash
curl -X POST http://localhost:8080/api/sop/index-local-docs

curl "http://localhost:8080/api/sop/search?query=正式法律意见的答复边界&topK=3"

curl -X POST http://localhost:8080/api/legal_chat \
  -H "Content-Type: application/json" \
  -d '{"question":"正式法律意见的答复边界是什么？"}'

# 将首次响应的 conversationId 填入追问
curl -X POST http://localhost:8080/api/legal_chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"conv_替换为首次返回值","question":"其中第二条是什么意思？"}'

```

模型会先通过 `LegalSopTools` 检索知识库，再生成包含 `回答摘要`、`依据来源`、`file.md#chunk-n` 定位与 `使用边界` 的回答。

示例知识库主题包括保证责任、合同付款、劳动材料、借款诉讼时效和法律答复边界。

## 验证

后端测试包含真实 HTTP + Milvus 链路以及五条轻量重排评测；接口测试使用可控测试 `ChatModel` 避免调用外部模型服务。`fake` embedding 适合复现流程，实际问答演示需配置 DashScope API Key。

```bash
cd backend
docker compose up -d
./mvnw test

cd ../frontend
npm run build
```
