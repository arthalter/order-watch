# OrderWatch Mini Backend

Spring Boot 3.x + Java 17 + Maven 后端，负责 Milvus RAG、Mock 数据、Spring AI Tool Calling、运营问答和 Markdown 报告接口。

接口与模块边界参考根目录 `OrderWatch-Mini-PRD.md`。

## 本地运行

需要安装 Java 17（推荐 Temurin 17）、Docker，并配置百炼 API Key。

- 启动 Milvus（Standalone，含 etcd + minio）：
  - `docker compose up -d`
  - `docker compose ps`
  - `docker compose down`
- 启动：
  - `export DASHSCOPE_API_KEY=你的百炼APIKey`
  - `./mvnw spring-boot:run`
- 测试：`./mvnw test`
- Milvus 连通性测试（需要先启动 docker compose）：
  - `./mvnw -Dmilvus.it=true test`
- ReportAgent 真实百炼链路测试：
  - `DASHSCOPE_API_KEY=你的百炼APIKey ./mvnw -Dreport.agent.it=true -Dtest=ReportAgentIntegrationTest test`

启动后：
- `GET http://localhost:8080/health`
- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/health/milvus`
- 启动时会自动检查并初始化 Milvus SOP collection：`order_sop_chunks`
- 手动触发初始化：`curl -X POST http://localhost:8080/api/sop/collection/init`
- 查看 collection 状态：`curl http://localhost:8080/api/sop/collection`
- 入库本地 SOP 文档：`curl -X POST http://localhost:8080/api/sop/index-local-docs`
- 检索 SOP 文档：`curl "http://localhost:8080/api/sop/search?query=大额订单人工审核&topK=3"`
- 运营问答：`curl -X POST http://localhost:8080/api/ops_chat -H "Content-Type: application/json" -d '{"question":"遇到大额支付订单怎么办"}'`
- 生成异常监控报告：`curl -X POST http://localhost:8080/api/order_anomaly_report`
- 前端兼容报告路径：`curl -X POST http://localhost:8080/api/order_anomaly_monitor`

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

运营问答：

```json
{
  "success": true,
  "code": 0,
  "message": "ok",
  "data": {
    "success": true,
    "answer": "根据命中的 SOP...",
    "errorMessage": null
  }
}
```

异常监控报告：

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

## 演示脚本

以下命令用于跑通 Milvus RAG、运营问答和 Markdown 报告链路。需要先配置 `DASHSCOPE_API_KEY`。

```bash
docker compose up -d

export DASHSCOPE_API_KEY=你的百炼APIKey
./mvnw spring-boot:run

curl -X POST http://localhost:8080/api/sop/index-local-docs

curl -X POST http://localhost:8080/api/ops_chat \
  -H "Content-Type: application/json" \
  -d '{"question":"遇到大额支付订单怎么办"}'

curl -X POST http://localhost:8080/api/order_anomaly_report
```

## 当前完成度

- Mock 异常订单：已完成。
- Mock 证据：已完成。
- Metrics / Evidence / SOP Tools：已完成。
- Markdown SOP 读取、分片、Embedding、Milvus 入库和检索：已完成。
- `/api/ops_chat` 运营问答：已完成，真实调用百炼。
- `/api/order_anomaly_report` Markdown 报告：已完成，真实调用百炼。
- 不接真实订单库、支付系统、客服系统，符合 Mini 项目边界。

## 代码结构（保持简单，逐步演进）

当前后端保留最小可运行骨架，按接口层、应用层和基础设施层组织代码。

建议的分层（有需要时再加，不提前“搭空架子”）：

- `com.orderwatch.backend.interfaces`：对外接口层（HTTP Controller / DTO）
- `com.orderwatch.backend.application`：应用层（用例编排、事务边界）
- `com.orderwatch.backend.domain`：领域层（聚合/实体/值对象/领域服务/仓储接口）
- `com.orderwatch.backend.infrastructure`：基础设施层（Milvus、文件读取、外部适配等）
