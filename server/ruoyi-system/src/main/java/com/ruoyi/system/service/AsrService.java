package com.ruoyi.system.service;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.ruoyi.common.config.AlgorithmConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ASR 语音识别服务 — 基于阿里云 paraformer-realtime-v2
 *
 * 使用 Recognition.call(param, file) 直接传入音频文件，
 * 支持 wav/mp3/m4a 等常见格式，采样率建议 16000Hz。
 */
@Service
public class AsrService
{
    private static final Logger log = LoggerFactory.getLogger(AsrService.class);

    private final AlgorithmConfig config;

    public AsrService(AlgorithmConfig config)
    {
        this.config = config;
    }

    /**
     * 上传音频文件进行语音识别
     *
     * @param file 上传的音频文件（wav/mp3/m4a 等格式）
     * @return 识别后的文本
     */
    public String recognize(MultipartFile file) throws IOException, NoApiKeyException
    {
        // 保存上传文件到临时目录
        Path tempDir = Files.createTempDirectory("asr_");
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty())
        {
            originalName = "audio.wav";
        }
        File tempFile = tempDir.resolve(originalName).toFile();
        file.transferTo(tempFile);

        try
        {

            // 构建实时识别参数
            RecognitionParam param = RecognitionParam.builder()
                    .model("paraformer-realtime-v2")
                    .apiKey(config.getDashscopeApiKey())
                    .format("wav")
                    .sampleRate(16000)
                    .parameter("language_hints", new String[]{"zh", "en"})
                    .build();

            // 直接传入文件进行识别
            Recognition recognizer = new Recognition();
            String result = recognizer.call(param, tempFile);

            log.info("ASR 识别完成，RequestId: {}, 结果长度: {}",
                    "unknown",
                    result != null ? result.length() : 0);

            if (result != null && !result.trim().isEmpty())
            {
                return result.trim();
            }
            else
            {
                return "未能识别到语音内容";
            }
        }
        finally
        {
            // 清理临时文件
            if (tempFile.exists())
            {
                tempFile.delete();
            }
            if (tempDir.toFile().exists())
            {
                tempDir.toFile().delete();
            }
        }
    }
}

