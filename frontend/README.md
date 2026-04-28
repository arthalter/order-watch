# Frontend

Vue 3 + Element Plus 企业后台原型，用于展示异常订单、风险等级、AI 分析和人工审核流程。

## 运行

```bash
npm install
npm run dev
```

默认开发地址：

```text
http://localhost:5173
```

前端通过 Vite 代理调用后端 API：

- `POST /api/ops_chat`
- `POST /api/order_anomaly_monitor`

后端默认代理到 `http://localhost:8080`。如果后端未启动，页面会使用本地演示数据兜底，方便先看完整交互效果。
