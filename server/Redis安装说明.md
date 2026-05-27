# Redis 安装与使用说明

## 下载

前往 https://github.com/redis-windows/redis-windows/releases 下载最新的 Redis-x64 版本。
或使用国内镜像：https://pan.baidu.com 搜索 `Redis-x64`

## 安装

1. 运行安装包，安装到 `C:\Program Files\Redis\`（默认路径即可）
2. 安装过程中勾选 `Add to PATH`
3. 安装完成后，Redis 会自动注册为 Windows 服务

## 启动

安装后 Redis 默认已作为 Windows 服务运行。如果需要手动控制：

```powershell
# 启动 Redis 服务
net start Redis

# 停止 Redis 服务
net stop Redis
```

验证是否启动成功：
```powershell
redis-cli ping
```
返回 `PONG` 即表示正常。

## 配置

本项目的后端默认连接 `localhost:6379`，无密码。Redis 默认配置即可，无需额外修改。

## 注意事项

1. 后端启动前必须先确保 Redis 正在运行，否则启动会报错
2. 如果 Redis 端口被占用，可以修改 `application.yml` 中的 `spring.data.redis.port`
3. Redis 不需要导入任何数据，后端会自动使用