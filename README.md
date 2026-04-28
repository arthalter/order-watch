# OrderWatch Mini (Monorepo: Frontend + Backend)

本仓库采用同仓库前后端分离的目录结构：

- `backend/`：Spring Boot 后端（Milvus RAG + Mock 数据 + API）
- `frontend/`：前端（独立构建与部署，通过 HTTP 调用后端 API）

## 开发约定

- 后端只负责提供 REST API，不依赖前端构建产物。
- 前端默认通过开发代理或 CORS 访问后端。
- 接口与数据结构以 `OrderWatch-Mini-PRD.md` 为准。

## 运行（占位）

后端（示例）：

```bash
cd backend
# mvn spring-boot:run
```

前端（示例）：

```bash
cd frontend
# npm i
# npm run dev
```

