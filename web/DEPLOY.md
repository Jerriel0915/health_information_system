# 前端部署指南

本文档基于 RuoYi-Vue3-TypeScript 前端编写，适用于本地开发环境搭建。

## 环境要求

### 开发工具

| 工具      | 版本要求  | 说明     |
|---------|-------|--------|
| Node.js | 18+   | 前端运行环境 |
| yarn    | 1.22+ | 前端包管理器 |

### 环境安装

#### Node.js 安装

```bash
# 使用 nvm 安装（推荐）
curl -o bash https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh
source ~/.bashrc
nvm install 18
nvm use 18

# 或直接从 Node.js 官网下载安装包
# https://nodejs.org/
```

#### yarn 安装

```bash
npm install -g yarn

# 或使用 corepack（Node.js 16+ 内置）
corepack enable
```

---

## 安装依赖

```bash
# 进入前端目录
cd web

# 使用镜像源加速安装
yarn --registry=https://registry.npmmirror.com

# 或使用 npm
npm install --registry=https://registry.npmmirror.com
```

---

## 配置修改

编辑 `web/.env.development`：

```env
# 开发环境接口地址
VITE_APP_BASE_API = '/api'
VITE_APP_TARGET_URL = 'http://localhost:8080'
```

---

## 启动开发服务器

```bash
cd web

# 启动开发服务器
yarn dev

# 或使用 npm
npm run dev
```

前端启动成功后，访问地址：http://localhost:80

> **注意**：若 80 端口被占用，可修改 `vite.config.js` 中的 server.port 配置。

---

## 技术栈

- **框架**：Vue 3.5 + Composition API
- **语言**：TypeScript
- **构建工具**：Vite 6.4
- **UI 组件库**：Element Plus 2.13
- **状态管理**：Pinia 3.0
- **路由**：Vue Router 4.6
- **HTTP 客户端**：Axios 1.13

---

## 常见问题

### 1. 依赖安装失败

```bash
# 清理缓存重新安装
yarn cache clean
rm -rf node_modules
yarn install --registry=https://registry.npmmirror.com

# 或使用 npm
npm cache clean --force
rm -rf node_modules package-lock.json
npm install --registry=https://registry.npmmirror.com
```

### 2. 无法请求后端 API

- 确认后端服务已启动（http://localhost:8080）
- 检查跨域配置（后端已配置 CORS）
- 验证 VITE_APP_TARGET_URL 配置正确

### 3. 端口被占用

修改 `vite.config.js` 中的 server.port 配置。

---

## 参考链接

- 若依官方文档：http://doc.ruoyi.vip
- RuoYi-Vue3-TypeScript：https://gitcode.com/yangzongzhuan/RuoYi-Vue3/tree/typescript
- Element Plus 文档：https://element-plus.org/