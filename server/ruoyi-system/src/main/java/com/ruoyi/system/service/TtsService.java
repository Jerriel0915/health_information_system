package com.ruoyi.system.service;

import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.ruoyi.common.config.AlgorithmConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.nio.ByteBuffer;

/**
 * TTS 语音合成服务 — 基于阿里云 Sambert 系列
 *
 * 使用 DashScope SpeechSynthesizer 同步合成模式，
 * 参考：https://help.aliyun.com/zh/model-studio/developer-guides/voice-synthesis
 */
@Service
public class TtsService
{
    private static final Logger log = LoggerFactory.getLogger(TtsService.class);

    private final AlgorithmConfig config;

    public TtsService(AlgorithmConfig config)
    {
        this.config = config;
    }

    /**
     * 将文本合成为语音音频数据
     *
     * @param text 要合成的文本
     * @return 音频二进制数据（wav 格式）
     */
    public byte[] synthesize(String text) throws NoApiKeyException
    {
        // 设置 API Key（也可以通过环境变量 DASHSCOPE_API_KEY）
        if (config.getDashscopeApiKey() != null && !config.getDashscopeApiKey().isEmpty())
        {
            System.setProperty("dashscope.api.key", config.getDashscopeApiKey());
        }

        // 构建合成参数
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .model(config.getTtsModel())
                .text(text)
                .sampleRate(config.getTtsSampleRate())
                .format(SpeechSynthesisAudioFormat.WAV)
                .build();

        // 同步合成，返回 ByteBuffer 音频数据
        SpeechSynthesizer synthesizer = new SpeechSynthesizer();
        ByteBuffer audio = synthesizer.call(param);

        byte[] audioData = new byte[audio.remaining()];
        audio.get(audioData);

        log.info("TTS 合成完成，音频大小: {} bytes", audioData.length);
        return audioData;
    }
}
