# 健康大数据统计分析决策系统

面向医疗健康数据的统计分析与辅助决策平台，提供基础数据管理、可视化看板、统计分析接口以及智能分析能力。

## 项目概览

- 后端位于 `server/`，基于 RuoYi 改造，提供认证、系统管理、统计分析和算法接口。
- 前端位于 `web/`，基于 `Vue 3 + Vite + Element Plus + ECharts`，提供看板、业务管理页面和智能分析中心。
- 系统默认后端端口为 `8081`，前端开发服务器端口为 `80`。
- 根目录保留了一套历史 `Vue CLI` 配置文件，当前开发与启动请以 `web/` 和 `server/` 为准。

## 核心功能

- 首页看板：汇总机构、人员、床位、服务量、费用等核心指标。
- 业务管理：医疗机构、医疗人员、医疗床位、医疗服务、医疗费用、人口维度、区域维度。
- 统计分析：分布分析、趋势分析、排行分析、总览统计等接口。
- 智能分析：对话问答、语音识别、语音合成、语音全链路接口。
- 预留能力：图像分类、目标检测接口已预留，待接入真实模型。

## 目录结构

```text
health_information_system/
|-- server/                        # 后端工程
|   |-- ruoyi-admin/               # 启动模块、控制器入口
|   |-- ruoyi-system/              # 业务服务、Mapper、领域对象
|   |-- ruoyi-framework/           # 安全、配置、拦截器等框架层
|   |-- ruoyi-common/              # 公共工具与通用配置
|   |-- sql/                       # 数据库脚本
|   |-- doc/                       # 后端文档
|   |-- Dockerfile                 # 后端镜像构建文件
|   `-- docker-compose.yml         # MySQL + Redis + 后端编排
|-- web/                           # 前端工程
|   |-- src/views/dashboard/       # 首页看板
|   |-- src/views/system/          # 各业务模块页面
|   |-- src/views/ai/              # 智能分析中心
|   |-- src/api/                   # 前端接口封装
|   `-- vite.config.js             # Vite 配置与代理
|-- README.md
`-- 后端接口对接文档.md            # 项目接口总说明
```

## 技术栈

### 后端

- Java 17
- Spring Boot 4
- Spring Security + JWT
- MyBatis
- Druid
- MySQL 8
- Redis
- Springdoc OpenAPI

### 前端

- Vue 3
- Vite 6
- Element Plus
- Vue Router 4
- ECharts
- Axios

## 环境要求

建议使用以下环境：

- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+
- Redis 7.x

## 快速开始

### 1. 初始化数据库

1. 创建数据库，推荐名称为 `health_data_manager`：

```sql
CREATE DATABASE IF NOT EXISTS health_data_manager
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

2. 先导入 RuoYi 基础表：

```powershell
mysql -u root -p health_data_manager < server/sql/ry_20260417.sql
```

3. 再导入业务数据：

```powershell
mysql -u root -p health_data_manager < server/sql/init_db_clean.sql
```

4. 修改后端数据库连接配置，确保与实际库名一致。推荐优先使用环境变量：

```powershell
set MYSQL_URL=jdbc:mysql://127.0.0.1:3306/health_data_manager?useUnicode=true^&characterEncoding=utf8^&zeroDateTimeBehavior=convertToNull^&useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=Asia%%2FShanghai
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=你的数据库密码
```

说明：

- `server/ruoyi-admin/src/main/resources/application-druid.yml` 中默认数据库名是 `ry-vue`，如果你导入到了 `health_data_manager`，请务必同步修改 `MYSQL_URL`。
- 更完整的初始化说明见 `server/doc/数据库初始化说明.md`。

### 2. 启动后端

方式一：Maven 打包后启动

```powershell
cd server
mvn clean package -DskipTests -pl ruoyi-admin -am
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

方式二：直接运行

```powershell
cd server
mvn spring-boot:run -pl ruoyi-admin
```

常用环境变量：

```powershell
set SERVER_PORT=8081
set TOKEN_SECRET=change-this-token-secret
set REDIS_HOST=127.0.0.1
set REDIS_PORT=6379
set REDIS_DATABASE=0
set REDIS_PASSWORD=
```

AI 能力依赖的配置项位于 `server/ruoyi-admin/src/main/resources/application.yml` 的 `algorithm` 节点，正式部署前请替换为你自己的密钥，不要直接提交真实凭据。

### 3. 启动前端

```powershell
cd web
npm install
npm run dev
```

开发环境说明：

- 前端默认运行在 `http://localhost/`。
- Vite 代理会将 `/dev-api` 转发到 `http://localhost:8081`。
- 如需打包：

```powershell
cd web
npm run build:prod
```

## 默认访问信息

| 项目 | 说明 |
|------|------|
| 后端地址 | `http://localhost:8081` |
| Swagger | `http://localhost:8081/swagger-ui.html` |
| OpenAPI | `http://localhost:8081/v3/api-docs` |
| 前端地址 | `http://localhost/` |
| 登录接口 | `POST /login` |
| 默认账号 | `admin / admin123` |
| 验证码 | 已关闭，`code` 与 `uuid` 传空字符串即可 |
| 认证方式 | `Authorization: Bearer <token>` |

## 接口说明

### 统计分析接口

系统提供首页看板与 7 个业务域的统计分析接口，覆盖：

- 首页总览
- 医疗机构
- 医疗人员
- 医疗床位
- 医疗服务
- 医疗费用
- 人口维度
- 区域维度

完整说明见 [后端接口对接文档.md](./后端接口对接文档.md) 和 `server/doc/后端接口对接文档.md`。

### 算法模块接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/algorithm/chat` | 智能对话 |
| POST | `/algorithm/asr` | 语音识别 |
| POST | `/algorithm/tts` | 语音合成 |
| POST | `/algorithm/pipeline` | 语音识别 -> 问答 -> 语音合成 |
| POST | `/algorithm/image-classify` | 图像分类预留接口 |
| POST | `/algorithm/object-detect` | 目标检测预留接口 |

说明：

- 后端已提供 `chat/asr/tts/pipeline` 接口入口。
- 前端 `web/src/views/ai/` 中部分交互目前以前端演示逻辑为主，接入生产能力时建议联调后端真实接口。

## Docker 部署

后端目录提供了 `docker-compose.yml`，可同时启动 MySQL、Redis 和后端服务：

```powershell
cd server
docker compose --env-file .env.example up -d --build
```

使用前请注意：

- `.env.example` 默认数据库名为 `ry-vue`，如你使用 `health_data_manager`，请先修改。
- `docker-compose.yml` 中引用了 `init_db.sql`，实际整理数据脚本主要位于 `server/sql/` 下，部署前请先核对脚本路径与文件名是否一致。

## Windows 脚本

项目内已提供常用批处理脚本：

- 后端启动：`server/bin/run.bat`
- 后端打包：`server/bin/package.bat`
- 前端启动：`web/bin/run-web.bat`
- 前端打包：`web/bin/build.bat`

## 已知事项

- 当前仓库存在部分历史代码与迁移痕迹，根目录旧前端配置不作为主入口。
- 后端配置文件中的默认值偏向模板工程，部署前建议统一检查数据库、Token、Redis 和算法服务密钥。
- 若 Swagger 在生产环境不可访问，请检查 `application-prod.yml` 中的 `springdoc` 开关。
