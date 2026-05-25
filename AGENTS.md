# AGENTS.md（项目协作约定 / Codex 指南）

本文件用于指导 Codex 在本仓库内如何阅读上下文、做出修改、以及如何交付可验证的结果。  
适用范围：仓库根目录下的整个目录树（如有子目录 `AGENTS.md`，以更深层文件为准）。

---

## 项目背景与目标（以当前代码与 README 为准）

`LegalWatch Mini` 是一个个人学习项目，用于从 0 搭建 **Agent + Tool + Milvus RAG** 的最小可运行骨架。

- 必须保留 Milvus（用于理解真实 RAG 链路）。
- 当前版本聚焦法律文档上传、本地 Markdown 读取、文档分片、Embedding、Milvus 入库/检索、Tool 查询与法律文档问答。
- 当前主链路已经完成，项目状态标记为：**已完成**。
- 不做完整法律业务系统，不做复杂权限，不做多模型路由，不做过度工程化。

核心链路（必须能在实现中体现）：

```text
上传 / 本地文档
→ Markdown 读取与切片
→ Embedding
→ Milvus 向量入库
→ 用户问题
→ Agent 调用 LegalSopTools
→ 基于命中文档片段生成法律文档查询回答
```

其中：

```text
文档事实：来自上传 Markdown 或 backend/legal-docs 本地 Markdown
检索规则：来自 Milvus RAG
最终表达：由 LegalQaAgent 组织生成
```

---

## 当前完成状态

当前所有任务均已完成，已具备最小可验证闭环：

- Spring Boot 后端骨架：已完成。
- 统一 API 返回结构 `ApiResponse`：已完成。
- 健康检查 `/health`、`/actuator/health`、`/health/milvus`：已完成。
- Milvus Standalone docker-compose、配置属性、客户端配置、collection 初始化：已完成。
- `backend/legal-docs` 本地 Markdown 文档目录：已完成。
- Markdown 文档读取、上传、分片：已完成。
- Fake Embedding 与百炼 DashScope Embedding 接入：已完成。
- Milvus 向量 upsert/search：已完成。
- 本地文档入库接口 `/api/sop/index-local-docs`：已完成。
- 文档上传并入库接口 `/api/documents/upload`：已完成。
- SOP 检索接口 `/api/sop/search`：已完成。
- 唯一 Tool：`LegalSopTools`：已完成。
- 法律文档查询 Agent：`LegalQaAgent`：已完成。
- 法律文档问答接口 `/api/legal_chat`：已完成。
- README 演示脚本与测试说明：已完成。
- Maven 测试与真实 HTTP + Milvus 集成测试路径：已完成。

---

## DDD 领域划分（按当前 LegalWatch 代码）

仓库代码在设计时按以下领域理解与落盘（不强制等同于 Maven module，但命名/包结构要体现边界）：

1. **rag**：法律 Markdown 文档读取、上传、分片、Embedding、Milvus 入库/检索、DTO/接口
2. **chat**：法律文档查询问答（`/api/legal_chat`）请求解析、对话编排、输出格式
3. **milvus / infrastructure**：Milvus 连接、collection 初始化、向量写入、向量检索
4. **api / interfaces**：统一响应结构、Controller、HTTP DTO

### 边界规则

- 领域之间通过 **清晰的接口/DTO** 交互，避免随意跨包直接访问实现细节。
- **rag 领域**对 Milvus 的访问集中在 `infrastructure/rag`、`infrastructure/milvus` 或 service 层，避免在 controller 里写 Milvus 细节。
- **chat** 只做编排与输出，事实来源必须来自 Tool（`LegalSopTools`）或 RAG 检索结果。
- **interfaces/http** 只负责路由、入参校验、DTO 转换和统一响应，不承载文档读取、Embedding 或 Milvus 细节。
- 当前项目已经从电商异常订单背景变更为法律文档 RAG 查询背景，后续新增代码必须以 `LegalWatch Mini` 为准。

---

## 目录与分层（建议，遵循现有代码为先）

后端（`backend/`）尽量使用清晰的分层（示例）：

- `com.legalwatch.backend.interfaces.http`：Controller / Web DTO
- `com.legalwatch.backend.application`：用例编排、文档入库、检索、问答编排
- `com.legalwatch.backend.infrastructure`：外部依赖实现（Milvus client、文件读取、Embedding 实现等）
- `com.legalwatch.backend.api`：全局 API 契约（`ApiResponse`、`ErrorCode`）
- `com.legalwatch.backend.domain`：如后续出现稳定业务规则再引入；当前不强制使用

如当前仓库已采用不同命名/结构，以现有结构为准；新增代码应延续既有风格。

---

## 任务约束（以 `TickTick-Work-Tasks.md` 为完成记录）

当前 24 条任务已经全部完成。后续修改应避免“做得太多”，优先维护已完成主链路：

- 01-08：后端骨架 + 健康检查 + Milvus 连接与 health：已完成
- 09-16：法律文档目录、Markdown 读取/分片、Embedding、Milvus collection、入库与检索接口：已完成
- 17-20：文档上传、检索 Tool、SOP 查询封装：已完成
- 21-24：法律问答 Agent、`legal_chat`、README 演示脚本、测试验证路径：已完成

### 交付要求（与任务绑定）

- 每完成一个任务范围内的功能：补齐 **最小可验证路径**（接口可调用、返回结构稳定、README 或 curl 可演示）。
- 不做任务外的扩展（例如：多模型路由、复杂权限、BI 大屏、完整案件管理系统等）。

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

- `com.legalwatch.backend.interfaces.http`：Controller（路由、入参/出参、HTTP 状态码）
- `com.legalwatch.backend.interfaces.http.dto`：对外 Request/Response DTO（对前端/调用方稳定）
- `com.legalwatch.backend.api`：全局 API 契约（如 `ApiResponse`、`ErrorCode`、后续全局异常处理）
- `com.legalwatch.backend.application.<domain>`：用例编排（串流程、聚合数据、调用 Tool/RAG）
- `com.legalwatch.backend.infrastructure.<domain>`：外部实现（Milvus SDK、文件读写、外部 HTTP、Embedding 实现等）
- `com.legalwatch.backend.domain.<domain>`：当且仅当出现稳定业务规则/策略/模型时再下沉（早期可空置）

### rag 领域（法律文档→分片→Embedding→Milvus→召回）

- HTTP 接口（`/api/sop/index-local-docs`、`/api/sop/search`、`/api/documents/upload`、`/api/sop/collection/init`）：`interfaces/http`
- 文档读取（扫描目录、读 Markdown、上传文件保存）：`infrastructure/rag` 与 `application/rag`
- 分片（chunking）与检索流程编排：`application/rag`
- Embedding：
  - Fake/本地算法：`infrastructure/rag`
  - 外部模型/服务调用：`infrastructure/rag`（application 只编排）
- Milvus 连接/配置/Collection 初始化/Upsert/Search：`infrastructure/rag`
- 召回（retrieval）与重排（rerank）：
  - 纯规则/轻量加权：`application/rag`
  - 依赖外部 rerank 服务：`infrastructure/rag`

### chat 领域（legal_chat 编排）

- HTTP 接口（`/api/legal_chat`）：`interfaces/http` + `interfaces/http/dto`
- 工具调用编排、检索结果组织、回答生成：`application/chat`
- 当前 Agent 为 `LegalQaAgent`，通过 `LegalSopTools` 查询已入库文档片段。
- 如果后续接入大模型/外部 Chat API：`infrastructure/chat`（application 只做流程）

### 当前不再使用的旧背景

- 旧的 `OrderWatch Mini`、异常订单、支付记录、客服工单、运营问答、订单监控报告背景已经废弃。
- 后续不得再以订单异常监控作为默认业务背景新增代码。
- 如历史文档仍保留旧名称，只作为迁移前记录；当前实现与协作约定以 `LegalWatch Mini` 为准。
