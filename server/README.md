# 健康大数据统计分析决策系统 — 后端

> 基于 RuoYi-Vue-SpringBoot4 框架的后端服务。
> 技术栈：Spring Boot 4.0.3 + MyBatis + Druid + Redis + MySQL

---

## 项目结构

```
server/
├── ruoyi-admin/          # Web层：Controller + 启动入口
├── ruoyi-system/         # 业务逻辑层：Service / Mapper / Entity
├── ruoyi-framework/      # 框架配置：安全认证、CORS、拦截器
├── ruoyi-common/         # 公共工具类（含 AlgorithmConfig）
├── ruoyi-quartz/         # 定时任务（未使用）
├── ruoyi-generator/      # 代码生成器（未使用）
└── sql/                  # 数据库初始化脚本
    ├── health_data_manager.sql  # 完整数据库（含业务数据）
    └── ry_20260417.sql          # RuoYi 系统表
```

---

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis（可选，不影响核心功能）

---

## 快速启动

### 1. 初始化数据库

```sql
CREATE DATABASE health_data_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
mysql -u root -p health_data_manager < sql/health_data_manager.sql
```

### 2. 配置环境变量（API Key）

算法模块需要配置 AI 服务的 API Key：

```bash
# Windows PowerShell
$env:DEEPSEEK_API_KEY="你的DeepSeek Key"
$env:DASHSCOPE_API_KEY="你的阿里云DashScope Key"
```

不配置 API Key 不影响后端启动，但 AI 对话、语音功能不可用。

### 3. 启动后端

```bash
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar

# 或直接 Maven 运行
mvn spring-boot:run -pl ruoyi-admin
```

启动后访问 http://localhost:8081 验证服务状态。

---

## 接口概览

### 统计分析接口（38 个）

| 模块 | 接口数 | 功能 |
|------|--------|------|
| 首页看板 | 1 | 系统概况、各模块趋势、分布数据 |
| 人口信息 | 7 | CRUD + 年龄/性别/区域分布 + 趋势 |
| 区域维度 | 5 | CRUD + 树形结构 + 区域总览 |
| 医疗机构 | 7 | CRUD + 类型/等级/区域分布 + 趋势 |
| 医务人员 | 8 | CRUD + 职称/类别/学历/性别分布 + 趋势 |
| 医疗床位 | 7 | CRUD + 科室/区域分布 + 使用率 + 趋势 |
| 医疗服务 | 8 | CRUD + 类型/科室分布 + 疾病排行 + 趋势 |
| 医疗费用 | 8 | CRUD + 构成分析 + 医保分析 + 次均费用 + 趋势 |

### 算法模块接口（6 个）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /algorithm/chat | 智能对话（DeepSeek） |
| POST | /algorithm/asr | 语音识别（DashScope） |
| POST | /algorithm/tts | 语音合成（DashScope） |
| POST | /algorithm/pipeline | 语音→对话→语音全链路 |
| POST | /algorithm/image-classify | 图像分类（预留） |
| POST | /algorithm/object-detect | 目标检测（预留） |

完整接口文档见 [后端接口对接文档.md](../后端接口对接文档.md)

---

## 基础配置

| 配置项 | 值 |
|--------|-----|
| 服务端口 | 8081 |
| 数据库 | health_data_manager（localhost:3306） |
| 默认账号 | admin / admin123 |
| 验证码 | 已关闭 |
| 认证方式 | Bearer Token |
| Redis | localhost:6379（非强制） |

---

## 分支说明

| 分支 | 说明 |
|------|------|
| main | 稳定分支 |
| feature-backend | 后端开发分支 |
| release/v1.0-full-integration | 全量集成版本 |
