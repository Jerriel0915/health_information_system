# API Key 安全配置说明

## 修改内容

已将 API Key 从 `application.yml` 中移除，改为通过**系统环境变量**读取。

## 需要设置的环境变量

| 环境变量名 | 说明 | 对应平台 |
|---|---|---|
| `DEEPSEEK_API_KEY` | DeepSeek 大模型对话 API Key | platform.deepseek.com |
| `DASHSCOPE_API_KEY` | 阿里云 DashScope ASR/TTS API Key | dashscope.aliyun.com |

## Windows 设置方式

### 临时设置（当前 PowerShell 会话有效）
```powershell
$env:DEEPSEEK_API_KEY="sk-你的key"
$env:DASHSCOPE_API_KEY="sk-你的key"
```

### 永久设置
1. 按 `Win + R`，输入 `sysdm.cpl`
2. 进入「高级」→「环境变量」
3. 在「系统变量」或「用户变量」中新增上述两个变量
4. 重启 IDEA / 命令行终端

## 版本回退说明

原 `application.yml` 中 `deepseek-api-key` 和 `dashscope-api-key` 的值已清空，yml 中只保留占位空字符串。
启动时会优先读取 yml 中的值，若为空则 fallback 到环境变量。

## 安全提醒

- **不要**将 API Key 写在代码中提交到 Git
- **不要**将 API Key 截图或复制到公开聊天中
- 如果怀疑 Key 泄漏，立即去对应平台撤销并重新生成
- 建议每个组员各自申请自己的 API Key，通过环境变量配置本地开发环境
