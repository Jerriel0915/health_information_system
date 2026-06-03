# 健康大数据统计分析决策系统

> 健康大数据应用创新研发中心项目
> 技术栈：Spring Boot 4.0.3 + MyBatis + Druid + Redis + MySQL + Vue 3 + Element Plus + ECharts

---

## 项目概述

健康大数据应用创新研发中心统计分析决策系统，是一个面向医疗健康领域的多维度数据分析与智能决策平台。系统集成了人口信息、医疗卫生机构、医务人员、床位资源、医疗服务、医疗费用六大核心模块的统计分析与可视化展示，同时引入大模型对话、语音识别、语音合成等AI技术，为研发人员和管理人员提供数据查询、统计解读和决策建议服务。

---

## 项目结构

```
health_system/
├── server/                   # 后端（Spring Boot + RuoYi）
│   ├── ruoyi-admin/          # 启动入口、Controller
│   ├── ruoyi-system/         # 业务逻辑（Service / Mapper / Entity）
│   ├── ruoyi-framework/      # 框架配置（安全、CORS、拦截器）
│   ├── ruoyi-common/         # 公共工具类（含算法配置）
│   ├── ruoyi-quartz/         # 定时任务（RuoYi自带，未使用）
│   ├── ruoyi-generator/      # 代码生成器（RuoYi自带，未使用）
│   └── sql/                  # 数据库初始化脚本
│
├── web/                      # 前端（Vue 3 + Element Plus + Vite）
│
└── 后端接口对接文档.md         # API 接口说明文档
```

---

## 环境要求

| 环境 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.8+ | 后端构建工具 |
| MySQL | 8.0+ | 数据库 |
| Redis | 任意 | 缓存（非强制） |
| Node.js | 18+ | 前端运行环境 |

---

## 快速启动

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE health_data_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构和数据
mysql -u root -p health_data_manager < server/sql/health_data_manager.sql
```

### 2. 配置 API Key（可选）

AI 功能需要配置 API Key，通过系统环境变量设置：

```bash
# Windows PowerShell
$env:DEEPSEEK_API_KEY="你的DeepSeek Key"
$env:DASHSCOPE_API_KEY="你的阿里云DashScope Key"
```

不配置不影响后端启动和业务功能，仅 AI 对话/语音不可用。

### 3. 启动后端

```bash
cd server
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

后端运行在 http://localhost:8081

### 4. 启动前端

```bash
cd web
npm install
npm run dev
```

前端运行在 http://localhost:5173

---

## 功能模块

### 统计分析（7 大模块，38 个接口）
- 首页看板：系统概览、趋势图、分布图
- 人口信息：人口结构、年龄/性别/区域分布
- 区域维度：区域树形结构及总览
- 医疗机构：机构类型/等级/区域分布
- 医务人员：职称/类别/学历/性别分布
- 医疗床位：科室/区域分布、使用率分析
- 医疗服务：类型/科室分布、疾病排行
- 医疗费用：费用构成、医保分析、次均费用

### AI 智能分析
- 智能对话：基于 DeepSeek 大模型，AI 角色"小慧"
- 语音识别：阿里云 DashScope Paraformer 模型
- 语音合成：阿里云 DashScope Sambert 模型
- 语音查询全链路：ASR → DeepSeek → TTS

---

## 登录信息

- 默认账号：admin / admin123
- 验证码：已关闭（便于联调测试）
- 认证方式：Bearer Token

---

## 分支说明

| 分支 | 说明 |
|------|------|
| main | 稳定分支 |
| feature-backend | 后端开发分支 |
| feature | 前端开发分支 |
| release/v1.0-full-integration | 全量集成版本 |
