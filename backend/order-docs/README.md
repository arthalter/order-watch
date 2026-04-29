# order-docs（SOP 文档目录）

本目录用于存放 **运营 SOP（Standard Operating Procedure）Markdown 文档**，后续会被后端的 RAG 流程读取、分片、生成 embedding，并写入 Milvus。

## 配置来源

后端默认从 `rag.docs-path` 读取文档目录（见 `backend/src/main/resources/application.yml`）：

- 默认值：`./order-docs`（即从 `backend/` 目录启动应用时，对应 `backend/order-docs`）
- 也可以通过环境变量覆盖：`RAG_DOCS_PATH`

## 约定

- 文件格式：`.md`
- 一份 SOP 一个文件（便于管理与更新）
- 内容建议包含：
  - 适用场景（是什么异常）
  - 判定标准（为什么算异常）
  - 处理步骤（怎么处理）
  - 注意事项（风险/边界/例外）
  - 相关话术（可选）

## 下一步任务

- 任务 10：在此目录下新增四份异常订单 SOP Markdown。

