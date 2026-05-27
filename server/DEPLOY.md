# 后端部署指南

本文档基于 RuoYi-Vue3-TypeScript 后端编写，适用于本地开发环境搭建。

## 环境要求

### 开发工具

| 工具    | 版本要求        | 说明                             |
|-------|-------------|--------------------------------|
| JDK   | 17+         | Spring Boot 4.x 必须为 JDK 17 及以上 |
| Maven | 3.6+        | 项目构建工具                         |
| MySQL | 5.7+ / 8.0+ | 关系型数据库                         |
| Redis | 5.0+        | 缓存数据库                          |

### 环境安装

#### JDK 17+ 安装

```bash
# 使用 SDKMAN 安装（推荐）
curl -s "https://get.sdkman.io" | bash
sdk install java 17.0.9-tem

# 或直接从 Oracle 官网下载安装包
# https://www.oracle.com/java/technologies/downloads/#java17
```

#### Maven 安装

```bash
# macOS/Linux
brew install maven

# Windows - 下载 apache-maven-3.9.x-bin.zip 并配置环境变量
# https://maven.apache.org/download.cgi
```

---

## 数据库配置

### 创建数据库

```sql
-- 登录 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE ruoyi CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 创建用户（可选）
CREATE USER 'ruoyi'@'localhost' IDENTIFIED BY 'ruoyi123456';
GRANT ALL PRIVILEGES ON ruoyi.* TO 'ruoyi'@'localhost';
FLUSH PRIVILEGES;
```

### 导入数据脚本

```bash
# 进入 SQL 文件目录
cd server/sql

# 导入数据库脚本
mysql -u root -p ruoyi < quartz.sql
mysql -u root -p ruoyi < ry.sql
mysql -u root -p ruoyi < ry_config.sql
mysql -u root -p ruoyi < ry_dict.sql
mysql -u root -p ruoyi < ry_menu.sql
```

> **注意**：数据库脚本位于 `server/sql/` 目录下，请确保已执行所有 SQL 文件。

---

## Redis 配置

```bash
# 使用包管理器安装（Ubuntu）
sudo apt install redis-server

# macOS
brew install redis
brew services start redis

# Windows - 使用 WSL2 或下载 Redis Windows 版
# https://github.com/tporadowski/redis/releases
```

验证 Redis 是否正常运行：

```bash
redis-cli ping
# 应返回：PONG
```

---

## 配置文件修改

编辑 `server/ruoyi-admin/src/main/resources/application.yml`：

```yaml
# 数据库配置（在 application-druid.yml 中）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ruoyi?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ruoyi
    password: ruoyi123456

# Redis配置
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      password:    # 如有密码请填写

# 文件上传路径
ruoyi:
  profile: D:/ruoyi/uploadPath   # 修改为本地路径

# Token密钥（生产环境请修改）
token:
  secret: abcdefghijklmnopqrstuvwxyz   # 请修改为随机字符串
```

---

## 构建并启动

```bash
# 进入后端目录
cd server

# 清理并打包
mvn clean package -DskipTests

# 或使用 Maven Wrapper（项目自带）
./mvnw clean package -DskipTests

# 启动后端服务
java -jar ruoyi-admin/target/ruoyi-admin.jar

# 或使用脚本启动
cd bin
./run.bat   # Windows
./run.sh    # Linux/macOS
```

后端启动成功后，访问地址：http://localhost:8080

---

## 技术栈

- **框架**：Spring Boot 4.0.3
- **Java 版本**：JDK 17+
- **安全**：Spring Security + JWT
- **数据库**：MySQL + Druid 连接池
- **ORM**：MyBatis + PageHelper 分页
- **缓存**：Redis + RedisCache
- **文档**：SpringDoc OpenAPI 3.0

---

## 常见问题

### 1. 端口被占用

```bash
# Windows - 查看端口占用
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F

# Linux/macOS
lsof -i :8080
kill -9 <进程ID>
```

### 2. 数据库连接失败

- 检查 MySQL 服务是否启动
- 确认数据库用户名密码正确
- 验证数据库已创建

```bash
# 检查 MySQL 服务状态
# Windows
net start mysql

# Linux
sudo systemctl status mysql

# macOS
brew services list | grep mysql
```

### 3. Redis 连接失败

- 检查 Redis 服务是否启动
- 确认防火墙未阻止 6379 端口
- 验证密码（如设置）正确

### 4. Maven 构建失败

```bash
# 更新依赖
mvn clean install -U

# 设置 Maven 镜像（在 ~/.m2/settings.xml 中）
# 阿里云镜像加速
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>*</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

---

## 参考链接

- 若依官方文档：http://doc.ruoyi.vip
- RuoYi-Vue3 项目地址：https://gitcode.com/yangzongzhuan/RuoYi-Vue3
- 原版 RuoYi-Vue：https://gitee.com/y_project/RuoYi-Vue