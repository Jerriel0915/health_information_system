# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 项目总览

**健康大数据统计分析决策系统** — 医疗健康领域多维度数据分析与智能决策平台。三大独立部署模块组成：

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          前端 (Web)  :5173                               │
│  Vue 3 + Element Plus + ECharts + Vite  + Pinia/Vuex                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │Dashboard │ │/system/* │ │  /ai     │ │ /login   │ │  7 大    │      │
│  │  首页看板│ │  7 模块  │ │智能分析  │ │  登录    │ │ 业务模块 │      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
│       │              │              │                                    │
│       └──────────────┴──────────────┘                                    │
│              Vite proxy: /dev-api → http://localhost:8081               │
└─────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     后端 (Server)  :8081                                 │
│  RuoYi-Vue-SpringBoot4 框架 (Spring Boot 4.0.3 + MyBatis + Druid)      │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐            │
│  │ruoyi-admin │ │ruoyi-system│ │ruoyi-      │ │ruoyi-      │            │
│  │Controller层│ │业务层      │ │framework   │ │common      │            │
│  │Algorithm   │ │Service/    │ │安全/CORS   │ │工具类/     │            │
│  │Controller  │ │Mapper/Entity│ │拦截器    │ │AlgorithmCfg│            │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘            │
│       │              │                                                  │
│       └──────────────┘                                                  │
│   38 个统计接口 + 6 个 AI 接口  (统一 R 格式 {code,msg,data})           │
└─────────────────────────────────────────────────────────────────────────┘
        │                                            │
        ▼                                            ▼
┌──────────────────────────┐            ┌──────────────────────────────────┐
│   MySQL 8.0+             │            │  Python AI 微服务 :5001          │
│   health_data_manager    │            │  FastAPI + uvicorn               │
│   7 业务表 + RuoYi 系统表│            │  ┌──────┐ ┌──────┐ ┌──────┐     │
│                          │            │  │chat  │ │asr   │ │tts   │     │
│                          │            │  │LLM   │ │tongyi│ │tongyi│     │
│                          │            │  └──────┘ └──────┘ └──────┘     │
│                          │            │  ┌──────┐ ┌──────┐               │
│                          │            │  │class-│ │detect│               │
│                          │            │  │ify   │ │骨骼  │               │
│                          │            │  └──────┘ └──────┘               │
│                          │            │  DeepSeek + DashScope (Qwen)    │
└──────────────────────────┘            └──────────────────────────────────┘
```

---

## 关键架构特点

### 1. 三服务部署模型
- **后端 (Java/Spring Boot)** — 主业务 + 业务数据接口
- **前端 (Vue 3)** — 仅 UI/可视化
- **算法服务 (Python/FastAPI)** — AI 推理，可独立伸缩

> **注意**：根目录 `package.json` 是早期 Vue 2 残留，已被 `web/package.json` (Vue 3) 取代。**前端工作请在 `web/` 目录进行**。

### 2. 后端 → AI 的调用链
后端 `AlgorithmController` 是 AI 服务的代理层。前端调 `/dev-api/algorithm/*` → 后端透传 → Python `:5001`。
- 关键配置：`server/ruoyi-common/.../AlgorithmConfig.java` 中 AI 服务的 baseURL
- 流式对话使用 **SSE** (EventSource)，前端 `@/api/ai.js` 中的 `sendChatMessageStream`

### 3. 业务模块对照 (CRUD + 统计)
| 数据库表 | 前端路由 | 后端 Controller | 前端 API 文件 |
|---------|---------|----------------|--------------|
| `medical_institution` | `/system/institution` | `InstitutionController` | `api/system/institution.js` |
| `medical_staff` | `/system/staff` | `StaffController` | `api/system/staff.js` |
| `medical_bed` | `/system/bed` | `BedController` | `api/system/bed.js` |
| `medical_service` | `/system/service` | `ServiceController` | `api/system/service.js` |
| `medical_cost` | `/system/cost` | `CostController` | `api/system/cost.js` |
| `dim_population` | `/system/population` | `PopulationController` | `api/system/population.js` |
| `dim_region` | `/system/region` | `RegionController` | `api/system/region.js` |

> **CRUD 详情接口特殊**：路径是 `/detail/{id}` 而非 RuoYi 默认的 `/{id}`（见 `后端接口对接文档.md` 第 7 节）。

### 4. 前端目录速查
```
web/src/
├── api/
│   ├── login.js / dashboard.js / ai.js
│   └── system/  ← 7 个业务模块一一对应
├── views/
│   ├── dashboard/index.vue   ← 首页看板（统计卡片 + ECharts）
│   ├── system/{institution,staff,bed,service,cost,population,region}/index.vue
│   ├── ai/index.vue          ← 流式对话 + 语音
│   └── login/index.vue
├── router/index.js           ← 哈希路由 + Admin-Token cookie 守卫
└── utils/request.js          ← Axios 封装（带 token / 错误处理）
```

### 5. 算法服务
```
algorithm_service/
├── main.py          ← FastAPI 入口（/chat /chat/stream /classify /predict /detect）
├── chat.py          ← DeepSeek 调用 + 会话管理 + MySQL 历史持久化
├── asr.py / tts.py  ← DashScope 语音
├── pipeline.py      ← ASR → LLM → TTS 全链路
├── detector.py / classifier.py / bone_classifier.py ← 图像任务
├── config.py        ← 环境变量集中读取
└── requirements.txt ← fastapi/uvicorn/openai/dashscope/ultralytics/torch/pymysql
```

---

## 常用命令

### 数据库
```bash
mysql -u root -p -e "CREATE DATABASE health_data_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p health_data_manager < server/sql/health_data_manager.sql
mysql -u root -p health_data_manager < server/create_chat_tables.sql  # AI 会话表
```

### 后端 (server/)
```bash
cd server
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
# 或
mvn spring-boot:run -pl ruoyi-admin
```
- 端口：`8081`
- 启动前需设置环境变量（AI 功能）：
  - PowerShell: `$env:DEEPSEEK_API_KEY="sk-..."; $env:DASHSCOPE_API_KEY="sk-..."`

### 前端 (web/)
```bash
cd web
npm install
npm run dev          # 开发 :5173
npm run build:prod   # 生产构建
npm run build:stage  # 灰度构建
```
- Vite proxy 已将 `/dev-api` 转发到 `http://localhost:8081`，无需 CORS 配置
- 路由：哈希模式（`createWebHashHistory`），登录后路由守卫读 cookie `Admin-Token`

### 算法服务 (algorithm_service/)
```bash
cd algorithm_service
python -m venv .venv && .venv\Scripts\activate   # Windows
pip install -r requirements.txt
python main.py                                     # 启动 :5001
```
- 健康检查：`GET http://localhost:5001/health`
- **无现成测试套件**（仅 `test_images/` 测试样本）

---

## 登录与认证

- 账号：`admin / admin123`
- 验证码：**已关闭**（登录时 `code` 和 `uuid` 传空字符串）
- Token：JWT，写入浏览器 cookie `Admin-Token`，前端 axios 自动带 `Authorization: Bearer ...`
- 路由守卫：`web/src/router/index.js` 第 128 行，检查 `Admin-Token` cookie

---

## 接口响应格式

所有后端接口统一信封：
```json
{ "code": 200, "msg": "操作成功", "data": {} }
```
分页接口：`{ code, msg, total, rows }`
统计接口：`data` 通常是 `[{name, value}, ...]`，**前端 ECharts 直接渲染**。

---

## 数据库表概览

| 表名 | 模块 | 量级 |
|------|------|------|
| `medical_institution` | 医疗机构 | 78 |
| `medical_staff` | 医疗人员 | 5,206 |
| `medical_service` | 医疗服务 | 50,000 |
| `dim_region` | 区域 | 预置 |
| `dim_population` | 人口 | 预置 |
| `medical_bed` | 床位 | 预置 |
| `medical_cost` | 费用 | 预置 |
| `chat_session` / `chat_message` | AI 会话 | 算法服务运行时建 |

---

## 开发注意事项

1. **新模块加在 `ruoyi-system`**，按 controller/service/impl/mapper/domain 5 件套组织。
2. **新增前端页**：在 `web/src/views/` 加目录 → `api/system/` 加 js → `router/index.js` 注册路由。
3. **API 调用风格**：使用 `web/src/utils/request.js` 封装的 axios，**不要直接 import axios**。
4. **修改 API 路径**：CRUD 详情接口必须保留 `/detail/{id}` 前缀，与 RuoYi 默认行为不同。
5. **算法服务可选**：后端可独立启动，不依赖 `:5001`（AI 接口会 500，但不影响业务接口）。
6. **不要修改** `ruoyi-quartz` 和 `ruoyi-generator`（项目未使用，纯 RuoYi 模板冗余）。
7. **API Key 安全**：见 `server/API_KEY_SETUP.md`，仅通过环境变量配置，不要写进 yml 或提交代码。

---

## 分支策略

| 分支 | 用途 |
|------|------|
| `main` | 稳定分支 |
| `feature-backend` | 后端开发 |
| `feature` | 前端开发 |
| `release/v1.0-full-integration` | 全量集成版本（v1.0 发布） |
