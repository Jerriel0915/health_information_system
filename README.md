# 健康大数据统计分析决策系统

> 健康大数据应用创新研发中心项目
> 技术栈：SpringBoot 3 + MyBatis-Plus + Vue 3 + Element Plus + ECharts

---

## 项目结构

```
health_system/
├── server/          # 后端（SpringBoot 3 + RuoYi）
│   ├── ruoyi-admin/     # 启动入口，Controller 层
│   ├── ruoyi-system/    # 业务逻辑层（Service / Mapper / Entity）
│   ├── ruoyi-framework/ # 框架配置（安全、CORS、拦截器）
│   ├── ruoyi-common/    # 公共工具类（含 AlgorithmConfig）
│   ├── ruoyi-quartz/    # 定时任务（RuoYi 自带，未使用）
│   └── ruoyi-generator/ # 代码生成器（RuoYi 自带，未使用）
│
├── web/             # 前端（Vue 3 + Element Plus + Vite）
│
└── 后端接口对接文档.md  # 38 个 API 的完整接口说明
```

---

## 后端启动

```bash
cd server
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

或使用 Maven 直接运行：

```bash
cd server
mvn spring-boot:run -pl ruoyi-admin
```

> 建议在启动前设置 DashScope API Key 环境变量：
> ```bash
> set DASHSCOPE_API_KEY=你的key
> ```

---

## 接口说明

### 基础信息

| 项目 | 说明 |
|------|------|
| 服务地址 | http://localhost:8081 |
| 登录接口 | POST /login |
| 默认账号 | admin / admin123 |
| 验证码 | 已关闭（code、uuid 传空字符串） |
| 认证方式 | Bearer Token |

### 统计分析接口（38 个）

| 模块 | 接口数量 | 说明 |
|------|----------|------|
| 首页看板 | 1 | 全系统总览数据 |
| 医疗机构 | 7 | CRUD + 类型/等级/区域分布 + 趋势 |
| 医疗人员 | 8 | CRUD + 职称/类别/学历/性别分布 + 趋势 |
| 医疗床位 | 7 | CRUD + 科室/区域分布 + 使用率 + 趋势 |
| 医疗服务 | 8 | CRUD + 类型/科室分布 + 疾病排行 + 趋势 |
| 医疗费用 | 8 | CRUD + 构成/医保/次均费用分析 + 趋势 |
| 人口维度 | 7 | CRUD + 年龄/性别/区域分布 + 趋势 |
| 区域维度 | 5 | CRUD + 树形 + 总览 |

完整接口文档见 [后端接口对接文档.md](./后端接口对接文档.md)

### 算法模块接口（5 个）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /algorithm/chat | 智能对话（DeepSeek） |
| POST | /algorithm/asr | 语音识别（阿里云 Paraformer-realtime-v2） |
| POST | /algorithm/tts | 语音合成（阿里云 Sambert） |
| POST | /algorithm/pipeline | 语音→对话→语音全链路 |
| POST | /algorithm/image-classify | 图像分类（预留） |
| POST | /algorithm/object-detect | 目标检测（预留） |

---

## 数据库

- 数据库名：`health_data_manager`
- 端口：3306
- 编码：utf8mb4
- Redis：不强制依赖

---

## 分支说明

| 分支 | 说明 |
|------|------|
| feature/backend | 后端开发主分支（当前） |
| feat/statistics-module | 前端统计分析页面代码（已归档） |
| main | 主线分支 |
