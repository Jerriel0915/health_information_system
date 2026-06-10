package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 算法模块配置 — 优先读取 application.yml，空值时回退到环境变量
 */
@Configuration
@ConfigurationProperties(prefix = "algorithm")
public class AlgorithmConfig
{
    /** DeepSeek API Key */
    private String deepseekApiKey = "";

    /** DeepSeek API 地址 */
    private String deepseekBaseUrl = "https://api.deepseek.com";

    /** DeepSeek 模型名称 */
    private String deepseekModel = "deepseek-chat";

    /** 阿里云 DashScope API Key */
    private String dashscopeApiKey = "";

    /** 阿里云 ASR 模型（与 application.yml 默认值保持一致，避免配置漂移） */
    private String asrModel = "paraformer-realtime-v2";

    /** 阿里云 TTS 模型 */
    private String ttsModel = "sambert-zhiyuan-v1";

    /** TTS 采样率 */
    private Integer ttsSampleRate = 48000;

    /** TTS 音频格式 */
    private String ttsFormat = "wav";

    /**
     * TTS 模式：java（默认，本地 SDK）或 python（透传到 Python 算法服务）
     * 设置为 python 时，/algorithm/tts 与 /algorithm/tts/report 走 :5001
     */
    private String ttsMode = "java";

    /**
     * Python 算法服务 baseURL（用于 tts.mode=python 透传）
     */
    private String pythonBaseUrl = "http://localhost:5001";

    public String getDeepseekApiKey() {
        if (deepseekApiKey != null && !deepseekApiKey.isEmpty()) return deepseekApiKey;
        String env = System.getenv("DEEPSEEK_API_KEY");
        return env != null ? env : "";
    }

    public void setDeepseekApiKey(String deepseekApiKey) { this.deepseekApiKey = deepseekApiKey; }

    public String getDeepseekBaseUrl() { return deepseekBaseUrl; }
    public void setDeepseekBaseUrl(String deepseekBaseUrl) { this.deepseekBaseUrl = deepseekBaseUrl; }

    public String getDeepseekModel() { return deepseekModel; }
    public void setDeepseekModel(String deepseekModel) { this.deepseekModel = deepseekModel; }

    public String getDashscopeApiKey() {
        if (dashscopeApiKey != null && !dashscopeApiKey.isEmpty()) return dashscopeApiKey;
        String env = System.getenv("DASHSCOPE_API_KEY");
        return env != null ? env : "";
    }

    public void setDashscopeApiKey(String dashscopeApiKey) { this.dashscopeApiKey = dashscopeApiKey; }

    public String getAsrModel() { return asrModel; }
    public void setAsrModel(String asrModel) { this.asrModel = asrModel; }

    public String getTtsModel() { return ttsModel; }
    public void setTtsModel(String ttsModel) { this.ttsModel = ttsModel; }

    public Integer getTtsSampleRate() { return ttsSampleRate; }
    public void setTtsSampleRate(Integer ttsSampleRate) { this.ttsSampleRate = ttsSampleRate; }

    public String getTtsFormat() { return ttsFormat; }
    public void setTtsFormat(String ttsFormat) { this.ttsFormat = ttsFormat; }

    public String getTtsMode() { return ttsMode; }
    public void setTtsMode(String ttsMode) { this.ttsMode = ttsMode; }

    public String getPythonBaseUrl() { return pythonBaseUrl; }
    public void setPythonBaseUrl(String pythonBaseUrl) { this.pythonBaseUrl = pythonBaseUrl; }
}
