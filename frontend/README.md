# Frontend

Vue 3 + Element Plus 的 LegalWatch Mini 前端控制台，用于验证后端法律 SOP RAG 链路：

- 后端健康检查与 Milvus 连接状态
- SOP collection 初始化
- 本地 Markdown 文档入库
- 单个 Markdown 文件上传并入库
- SOP chunk 检索
- 法律文档问答

## 运行

```bash
npm install
npm run dev
```

默认开发地址：

```text
http://localhost:5173
```

Vite 会把 `/api` 和 `/health` 代理到后端：

```text
http://localhost:8080
```

## 后端准备

在 `backend/` 目录启动 Milvus 和 Spring Boot：

```bash
docker compose up -d
EMBEDDING_PROVIDER=fake ./mvnw spring-boot:run
```

## 页面验证路径

1. 打开 `http://localhost:5173`
2. 点击“刷新状态”，确认 Spring Boot、Milvus、Collection 状态
3. 点击“初始化 Collection”
4. 点击“索引本地文档”
5. 在“SOP 检索”中搜索 `正式法律意见`
6. 在“法律文档问答”中发送 `正式法律意见的答复边界是什么？`

## 对应接口

- `GET /health`
- `GET /health/milvus`
- `GET /api/sop/collection`
- `POST /api/sop/collection/init`
- `POST /api/sop/index-local-docs`
- `POST /api/documents/upload`
- `GET /api/sop/search?query=正式法律意见&topK=3`
- `POST /api/legal_chat`

## curl 示例

```bash
curl http://localhost:8080/health

curl http://localhost:8080/health/milvus

curl -X POST http://localhost:8080/api/sop/collection/init

curl -X POST http://localhost:8080/api/sop/index-local-docs

curl "http://localhost:8080/api/sop/search?query=正式法律意见&topK=3"

curl -X POST http://localhost:8080/api/legal_chat \
  -H "Content-Type: application/json" \
  -d '{"question":"正式法律意见的答复边界是什么？"}'
```
