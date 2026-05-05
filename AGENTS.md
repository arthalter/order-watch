# AGENTS.md（项目协作约定 / Codex 指南）

本文件用于指导 Codex 在本仓库内如何阅读上下文、做出修改、以及如何交付可验证的结果。  
适用范围：仓库根目录下的整个目录树（如有子目录 `AGENTS.md`，以更深层文件为准）。

---

## 项目背景与目标（以 `OrderWatch-Mini-PRD.md` 为准）

`OrderWatch Mini` 是一个个人学习项目，用于从 0 搭建 **Agent + Tool + Milvus RAG** 的最小可运行骨架：

- 必须保留 Milvus（用于理解真实 RAG 链路）。
- 第一版聚焦四件事：Mock 业务事实、Tool 查询、Milvus SOP 检索、提供运营问答与一键报告接口。
- 不接入真实订单/支付/客服系统，不实现复杂风控模型，不做过度工程化。

核心链路（必须能在实现中体现）：

```text
用户问题
→ Agent 判断需要哪些信息
→ 调用 Tool
→ Tool 查询 Mock 异常订单 / Mock 证据 / Milvus SOP
→ Agent 基于事实和规则生成回答或报告
```

---

## DDD 领域划分（按你的聊天约定）

仓库代码在设计时按 4 个领域理解与落盘（不强制等同于 Maven module，但命名/包结构要体现边界）：

1. **rag**：SOP 文档读取、分片、Embedding、Milvus 入库/检索、DTO/接口
2. **chat**：运营问答（`/api/ops_chat`）请求解析、对话编排、输出格式
3. **monitoring**：监控与报告领域（监控接口、报告生成、报告模板、健康检查/可观测性等）
4. **mock**：Mock 订单、支付、客服工单、证据等数据源与查询逻辑（Repository/Fixture/Seed）

### 边界规则

- 领域之间通过 **清晰的接口/DTO** 交互，避免随意跨包直接访问实现细节。
- **mock 领域**不得依赖 web/controller；应由 application/monitoring 或 application/chat 调用 mock 的 repository 接口。
- **rag 领域**对 Milvus 的访问集中在 infrastructure/client 或 service 层，避免在 controller 里写 Milvus 细节。
- **chat** 只做编排与输出，业务事实来源必须来自 tool（mock）或 rag（SOP）。

---

## 目录与分层（建议，遵循现有代码为先）

后端（`backend/`）尽量使用清晰的分层（示例）：

- `.../interfaces`：Controller / Web DTO
- `.../application`：用例编排、AppService、DTO、事务边界
- `.../domain`：领域对象、领域服务、领域接口（Repository Port）
- `.../infrastructure`：外部依赖实现（Milvus client、文件读取、mock 数据实现等）

如当前仓库已采用不同命名/结构，以现有结构为准；新增代码应延续既有风格。

---

## 任务约束（以 `TickTick-Work-Tasks.md` 为落地清单）

实现应与 24 条任务保持一致，避免“做得太多”：

- 01-08：后端骨架 + 健康检查 + Milvus 连接与 health
- 09-16：SOP 文档目录、Markdown 读取/分片、Embedding、Milvus collection、入库与检索接口
- 17-20：Mock 数据与 Tools（Metrics/Evidence/SOP Tools）
- 21-24：Agent 意图判断、`ops_chat`、报告模板、报告接口与 README 演示脚本

### 交付要求（与任务绑定）

- 每完成一个任务范围内的功能：补齐 **最小可验证路径**（接口可调用、返回结构稳定、README 或 curl 可演示）。
- 不做任务外的扩展（例如：多模型路由、复杂权限、BI 大屏、自动退款/封单等）。

---

## # 项目规则

## 项目目标
这是一个个人学习项目。优先选择简单、可读的实现方式，而不是复杂的工程化设计。

## 工作流程
- 收到新的开发任务后，先进行基本分析与任务拆解；除非用户明确要求直接动手，不要立刻编码。
- 分析时应将计划拆成 **3 到 4 项具体修改**，说明每项会改哪些文件、为什么要改、是否涉及新依赖或结构调整。
- 在用户确认修改方案前，不要修改业务代码、基础设施代码或配置文件；确认后再按约定范围逐项实施。
- 所有需要用户确认、批准、选择或授权的提问，必须使用简体中文表达。
- 编码前，先说明将要修改哪些文件。
- 编码后，告诉我如何运行和验证这个功能。
- 除非必要，不要引入新的依赖。
- 未经确认，不要删除已有文件。

## 验证方式
- 后端改动应提供一个 curl 示例。
- 前端改动应提供截图，或给出手动测试步骤。
- 如果项目中已有测试，请在修改后运行相关测试。

## 代码风格
- 函数尽量保持简短、易读。
- 只在逻辑不明显的地方添加注释。
- 遵循项目已有的目录结构和代码风格。必须完整保留在最终的.md中。

---

## 额外约定（建议）

- 默认以 **可解释、可复现** 为优先：返回内容要能指向“事实来源”（mock 指标/证据、命中的 SOP chunk）。
- 任何对外 API：统一返回结构（若项目已有 `ApiResponse`，则全部复用）。
- 配置集中在 `application.yml`（端口、RAG 参数、Milvus 连接等），避免散落常量。
- 错误处理：优先返回清晰的错误信息（参数缺失、Milvus 未连通、文档目录为空等），不要静默失败。

---

## 领域落盘指南（长期约定）

以下约定用于让微型项目在持续迭代中仍保持清晰：**interfaces 只做 HTTP、application 做用例编排、infrastructure 做外部实现，domain 先保留但不强制使用**。

### 通用分层（所有领域通用）

- `com.orderwatch.backend.interfaces.http`：Controller（路由、入参/出参、HTTP 状态码）
- `com.orderwatch.backend.interfaces.http.dto`：对外 Request/Response DTO（对前端/调用方稳定）
- `com.orderwatch.backend.api`：全局 API 契约（如 `ApiResponse`、`ErrorCode`、后续全局异常处理）
- `com.orderwatch.backend.application.<domain>`：用例编排（串流程、聚合数据、调用 Tool/RAG/Mock/Report）
- `com.orderwatch.backend.infrastructure.<domain>`：外部实现（Milvus SDK、文件读写、外部 HTTP、Mock 数据实现等）
- `com.orderwatch.backend.domain.<domain>`：当且仅当出现稳定业务规则/策略/模型时再下沉（早期可空置）

### rag 领域（文档→分片→Embedding→Milvus→召回/重排）

- HTTP 接口（`/api/index-local-docs`、`/api/sop_search`）：`interfaces/http`
- 文档读取（扫描目录、读 Markdown）：`infrastructure/rag`
- 分片（chunking）与检索流程编排：`application/rag`
- Embedding：
  - Fake/本地算法：`application/rag`
  - 外部模型/服务调用：`infrastructure/rag`（application 只编排）
- Milvus 连接/配置/Collection 初始化/Upsert/Search：`infrastructure/rag`
- 召回（retrieval）与重排（rerank）：
  - 纯规则/轻量加权：`application/rag`
  - 依赖外部 rerank 服务：`infrastructure/rag`

### chat 领域（ops_chat 编排）

- HTTP 接口（`/api/ops_chat`）：`interfaces/http` + `interfaces/http/dto`
- 意图判断（指标/证据/SOP/报告）、工具调用编排、回答组织：`application/chat`
- 如果后续接入大模型/外部 Chat API：`infrastructure/chat`（application 只做流程）

### monitoring 领域（监控与报告）

- HTTP 接口（`/api/order_anomaly_monitor` 等）：`interfaces/http` + `interfaces/http/dto`
- 监控/报告用例编排（拉指标、拉证据、引用 SOP、生成 Markdown 报告）：`application/monitoring`
- 报告模板与纯拼装逻辑：优先 `application/monitoring`（需要复用/复杂化再考虑下沉）

### mock 领域（业务事实数据源）

- 用例编排侧（需要哪些事实、如何组合）：`application/mock`
- 具体数据实现（内存列表、fixture、文件数据等）：`infrastructure/mock`
- 早期不强制定义 port/interface；如后续需要替换为真实数据源，再引入 `domain` 层接口
