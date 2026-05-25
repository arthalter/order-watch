# Frontend

Vue 3 + Element Plus 的 LegalWatch Mini 前端控制台，用于验证后端法律 SOP RAG 链路：

- 后端健康检查与 Milvus 连接状态
- SOP collection 初始化
- 本地 Markdown 文档入库
- 单个 Markdown 文件上传并入库
- SOP chunk 检索
- 法律文档问答

问答面板以会话时间线展示每轮问题与回答，并使用 `conversationId` 触发后端短期记忆：页面保留可见对话，后端在追问时参考当前会话最近 5 轮回答摘要；点击“新对话”即可开始独立话题。

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

如果本地 `8080` 已被其他服务占用，可将后端启动到其他端口并覆盖代理目标：

```bash
VITE_BACKEND_TARGET=http://localhost:8081 npm run dev
```

## 后端准备

在 `backend/` 目录启动 Milvus 和 Spring Boot：

```bash
docker compose up -d
# 仅验证文档入库与检索流程
EMBEDDING_PROVIDER=fake ./mvnw spring-boot:run

# 验证 Agent 回答与连续追问流程
DASHSCOPE_API_KEY=你的百炼APIKey ./mvnw spring-boot:run
```

## 页面验证路径

1. 打开 `http://localhost:5173`
2. 点击“刷新状态”，确认 Spring Boot、Milvus、Collection 状态
3. 点击“初始化 Collection”
4. 点击“索引本地文档”
5. 在“SOP 检索”中搜索 `正式法律意见`
6. 在“连续法律研读”中发送 `保证责任怎么查询？`，观察第一轮回答中的来源定位与使用边界
7. 点击左侧“结合上文追问”中的 `其中第二条需要准备哪些材料？` 并发送，确认对话时间线保留首问，左侧上下文状态显示已记忆轮次，追问被标记为基于上文
8. 点击“新对话”，确认时间线与记忆状态清空，再发送新主题问题以验证会话隔离

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
