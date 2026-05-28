package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 算法模块配置 — 读取 application.yml 中的 algorithm 配置项
 */
@Configuration
@ConfigurationProperties(prefix = "algorithm")
public class AlgorithmConfig
{
    /** DeepSeek API Key */
    private String deepseekApiKey;

    /** DeepSeek API 地址 */
    private String deepseekBaseUrl = "https://api.deepseek.com";

    /** DeepSeek 模型名称 */
    private String deepseekModel = "deepseek-chat";

    /** 阿里云 DashScope API Key */
    private String dashscopeApiKey;

    /** 阿里云 ASR 模型 */
    private String asrModel = "paraformer-v2";

    /** 阿里云 TTS 模型 */
    private String ttsModel = "sambert-zhiyuan-v1";

    /** TTS 采样率 */
    private Integer ttsSampleRate = 48000;

    /** TTS 音频格式 */
    private String ttsFormat = "wav";

    public String getDeepseekApiKey() { return deepseekApiKey; }
    public void setDeepseekApiKey(String deepseekApiKey) { this.deepseekApiKey = deepseekApiKey; }

    public String getDeepseekBaseUrl() { return deepseekBaseUrl; }
    public void setDeepseekBaseUrl(String deepseekBaseUrl) { this.deepseekBaseUrl = deepseekBaseUrl; }

    public String getDeepseekModel() { return deepseekModel; }
    public void setDeepseekModel(String deepseekModel) { this.deepseekModel = deepseekModel; }

    public String getDashscopeApiKey() { return dashscopeApiKey; }
    public void setDashscopeApiKey(String dashscopeApiKey) { this.dashscopeApiKey = dashscopeApiKey; }

    public String getAsrModel() { return asrModel; }
    public void setAsrModel(String asrModel) { this.asrModel = asrModel; }

    public String getTtsModel() { return ttsModel; }
    public void setTtsModel(String ttsModel) { this.ttsModel = ttsModel; }

    public Integer getTtsSampleRate() { return ttsSampleRate; }
    public void setTtsSampleRate(Integer ttsSampleRate) { this.ttsSampleRate = ttsSampleRate; }

    public String getTtsFormat() { return ttsFormat; }
    public void setTtsFormat(String ttsFormat) { this.ttsFormat = ttsFormat; }
}

