# OrderWatch Mini (Monorepo: Frontend + Backend)

`OrderWatch Mini` 是一个学习型 Agent + Tool + Milvus RAG 骨架项目，用 Mock 异常订单数据模拟业务事实，用 Milvus 检索 SOP 文档，并通过百炼大模型生成运营问答和 Markdown 异常监控报告。

当前主链路已经跑通：

```text
用户问题
→ Agent 调用 Tool
→ Tool 查询 Mock 异常订单 / Mock 证据 / Milvus SOP
→ Agent 基于事实和规则生成回答或报告
```

## 目录

- `backend/`：Spring Boot 后端，包含 Milvus RAG、Mock 数据、Tool、Agent 和 API
- `frontend/`：Vite + Vue 前端，通过开发代理访问后端 API
- `OrderWatch-Mini-PRD.md`：产品文档
- `TickTick-Work-Tasks.md`：24 个任务清单

## 本地运行

```bash
cd backend
docker compose up -d
export DASHSCOPE_API_KEY=你的百炼APIKey
./mvnw spring-boot:run
```

另开一个终端启动前端：

```bash
cd frontend
npm install
npm run dev
```

访问：

- 前端：`http://localhost:5173`
- 后端健康检查：`http://localhost:8080/health`
- Milvus 健康检查：`http://localhost:8080/health/milvus`

## 核心接口

```bash
# SOP 文档入库
curl -X POST http://localhost:8080/api/sop/index-local-docs

# SOP 检索
curl "http://localhost:8080/api/sop/search?query=大额订单人工审核&topK=3"

# 运营问答
curl -X POST http://localhost:8080/api/ops_chat \
  -H "Content-Type: application/json" \
  -d '{"question":"遇到大额支付订单怎么办"}'

# Markdown 异常监控报告
curl -X POST http://localhost:8080/api/order_anomaly_report
```

报告接口返回结构：

```json
{
  "success": true,
  "code": 0,
  "message": "ok",
  "data": {
    "report": "# 异常订单监控报告\n..."
  }
}
```

## 验证

```bash
cd backend
./mvnw test

cd ../frontend
npm run build
```

真实 ReportAgent 链路测试会请求百炼，默认跳过；需要时手动执行：

```bash
cd backend
DASHSCOPE_API_KEY=你的百炼APIKey ./mvnw -Dreport.agent.it=true -Dtest=ReportAgentIntegrationTest test
```
