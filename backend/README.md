# Backend

这里放 Spring Boot 3.x + Java 17 + Maven 的后端项目代码（Milvus RAG + Mock 数据 + Agent + API）。

接口与模块边界参考根目录 `OrderWatch-Mini-PRD.md`。

## 本地运行

需要安装 Java 17（推荐 Temurin 17）。

- 启动：`./mvnw spring-boot:run`
- 测试：`./mvnw test`

启动后：
- `GET http://localhost:8080/health`
- `GET http://localhost:8080/actuator/health`

## 代码结构（保持简单，逐步演进）

当前后端只保留最小可运行骨架（例如 `GET /health`），后续随着任务推进再逐步补齐结构。

建议的分层（有需要时再加，不提前“搭空架子”）：

- `com.orderwatch.backend.interfaces`：对外接口层（HTTP Controller / DTO）
- `com.orderwatch.backend.application`：应用层（用例编排、事务边界）
- `com.orderwatch.backend.domain`：领域层（聚合/实体/值对象/领域服务/仓储接口）
- `com.orderwatch.backend.infrastructure`：基础设施层（Milvus、文件读取、外部适配等）
