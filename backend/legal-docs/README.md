# legal-docs（SOP 文档目录）

本目录用于存放 **法律文档查询 SOP（Standard Operating Procedure）Markdown 文档**，后续会被后端的 RAG 流程读取、分片、生成 embedding，并写入 Milvus。

## 配置来源

后端默认从 `rag.docs-path` 读取文档目录（见 `backend/src/main/resources/application.yml`）：

- 默认值：`./legal-docs`（即从 `backend/` 目录启动应用时，对应 `backend/legal-docs`）
- 也可以通过环境变量覆盖：`RAG_DOCS_PATH`

## 约定

- 文件格式：`.md`
- 一份查询指引或材料清单一个文件（便于管理与更新）
- 内容建议包含：
  - 适用场景（查询哪类法律材料）
  - 查询重点（需要定位哪些条款、事实或材料）
  - 处理步骤（如何组织检索与回答）
  - 输出边界（避免把文档查询写成正式法律意见）

## 下一步任务

- 示例文档：合同付款条款、劳动材料清单、借款诉讼时效、法律答复边界。
