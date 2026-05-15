# LegalWatch Mini (Backend-Focused Monorepo)

`LegalWatch Mini` 是一个学习型 Agent + Tool + Milvus RAG 骨架项目，用于演示法律文档上传、Markdown 切片、Embedding 入库、Milvus 检索和法律文档查询对话。

当前后端主链路：

```text
上传 / 本地文档
→ Markdown 读取与切片
→ Embedding
→ Milvus 向量入库
→ 用户问题
→ Agent 调用唯一的 LegalSopTools
→ 基于命中文档片段生成查询回答
```

## 目录

- `backend/`：Spring Boot 后端，包含 Milvus RAG、文档上传、检索 Tool、Agent 和 API
- `frontend/`：旧版前端原型目录，本次后端改造未作为验证入口
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

## 核心接口

```bash
# 上传 Markdown 文档并立即切片入库
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@/path/to/legal-doc.md"

# 入库 backend/legal-docs 下的本地 Markdown 文档
curl -X POST http://localhost:8080/api/sop/index-local-docs

# 检索已入库文档片段
curl "http://localhost:8080/api/sop/search?query=保证责任&topK=3"

# 法律文档查询对话
curl -X POST http://localhost:8080/api/legal_chat \
  -H "Content-Type: application/json" \
  -d '{"question":"保证责任怎么查询？"}'
```

## 验证

真实 HTTP + Milvus 集成测试：

```bash
cd backend
docker compose up -d
./mvnw test
```

测试会用随机端口启动 Spring Boot，连接本地 Milvus，调用上传、入库、检索、对话和健康检查接口。
