# 健康大数据统计分析决策系统 — 前端

> Vue 3 + Element Plus + ECharts + Vite 前端项目。
> 配套后端：server 模块，运行在 http://localhost:8081

---

## 技术栈

- Vue 3（Composition API + script setup）
- Vue Router 4
- Vuex 4
- Element Plus
- ECharts
- Vite
- Axios

---

## 快速启动

```bash
# 安装依赖
npm install

# 启动开发服务器（热更新）
npm run dev

# 生产构建
npm run build:prod

# 预览构建产物
npm run preview
```

---

## 前端运行在 http://localhost:5173

---

## 页面模块

| 路由 | 页面 | 说明 |
|------|------|------|
| /login | 登录 | 账号密码登录，调后端真实API |
| /dashboard | 首页看板 | 统计卡片 + 4 张图表（趋势/分布） |
| /system/population | 人口信息 | 人口统计分析与图表 |
| /system/region | 区域维度 | 区域结构展示 |
| /system/institution | 医疗机构 | 机构统计与分布 |
| /system/staff | 医务人员 | 人员统计与分布 |
| /system/bed | 医疗床位 | 床位统计与分析 |
| /system/service | 医疗服务 | 服务统计与疾病排行 |
| /system/cost | 医疗费用 | 费用分析与趋势 |
| /ai | AI 智能分析 | 对话 + 语音查询 + 播报 |

---

## 环境配置

Vite 代理配置（vite.config.js）已将 /dev-api 前缀转发到后端 http://localhost:8081，前端开发时无需配置跨域。

```javascript
server: {
    port: 5173,
    proxy: {
        "/dev-api": {
            target: "http://localhost:8081",
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/dev-api/, ""),
        }
    }
}
```

---

## 登录说明

- 默认账号：admin / admin123
- 验证码：已关闭（联调测试环境）
- 认证方式：Token 写入 Cookie，路由守卫从 Cookie 读取
