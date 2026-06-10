package com.ruoyi.system.service;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.ruoyi.common.config.AlgorithmConfig;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * TTS 语音合成服务 — 基于阿里云 Sambert 系列
 *
 * Phase 3 加固：
 * 1. 长度截断 — 超过 MAX_LENGTH 按段落边界截断（避免超长文本卡死或限流）
 * 2. 超时控制 — 异步执行 + 超时阈值
 * 3. 失败重试 — 1 次重试
 * 4. 异常分类 — 抛出受检异常供 Controller 映射 HTTP 状态码
 *
 * 参考：https://help.aliyun.com/zh/model-studio/developer-guides/voice-synthesis
 */
@Service
public class TtsService
{
    private static final Logger log = LoggerFactory.getLogger(TtsService.class);

    /** 单次合成最大字符数（DashScope 限制约 300 字符/请求；分段累计无意义，这里设保守上限） */
    private static final int MAX_LENGTH = 2000;

    /** 单次合成超时（毫秒） */
    private static final long SYNTH_TIMEOUT_MS = 30000;

    /** 失败重试次数 */
    private static final int MAX_RETRIES = 1;

    /**
     * 专用线程池：TTS 同步 SDK 阻塞调用
     *
     * <p>M2 修复：从 static 改为实例字段，配合 {@code @PreDestroy} 优雅关闭。
     */
    private final ExecutorService ttsPool = Executors.newFixedThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors()),
            r -> {
                Thread t = new Thread(r, "tts-synth-" + System.nanoTime());
                t.setDaemon(true);
                return t;
            });

    private final AlgorithmConfig config;

    public TtsService(AlgorithmConfig config)
    {
        this.config = config;
    }

    /**
     * Spring 容器关闭时停止接收新任务，等待进行中的合成完成（最多 5 秒）后强制关闭。
     */
    @PreDestroy
    public void destroy()
    {
        ttsPool.shutdown();
        try
        {
            if (!ttsPool.awaitTermination(5, TimeUnit.SECONDS))
            {
                ttsPool.shutdownNow();
            }
        }
        catch (InterruptedException e)
        {
            ttsPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("TTS 线程池已关闭");
    }

    /**
     * 将文本合成为语音音频数据
     *
     * @param text 要合成的文本
     * @return 音频二进制数据（wav 格式）
     * @throws TtsException 合成失败（按错误类型细分）
     */
    public byte[] synthesize(String text) throws TtsException
    {
        // 1) 输入校验
        if (text == null || text.trim().isEmpty())
        {
            throw new TtsException(TtsError.EMPTY_TEXT, "文本不能为空");
        }
        if (config.getDashscopeApiKey() == null || config.getDashscopeApiKey().isEmpty())
        {
            throw new TtsException(TtsError.NOT_CONFIGURED, "未配置 DASHSCOPE_API_KEY");
        }

        // 2) 长度截断（按段落/句子边界）
        String safeText = truncateByParagraph(text.trim(), MAX_LENGTH);

        // 3) 异步执行 + 重试
        Exception lastEx = null;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++)
        {
            try
            {
                return doSynthesize(safeText);
            }
            catch (NoApiKeyException e)
            {
                throw new TtsException(TtsError.NOT_CONFIGURED, "API Key 无效", e);
            }
            catch (com.alibaba.dashscope.exception.InputRequiredException e)
            {
                throw new TtsException(TtsError.EMPTY_TEXT, "输入参数错误", e);
            }
            catch (Exception e)
            {
                lastEx = e;
                log.warn("TTS 合成失败 attempt={}/{} err={}", attempt + 1, MAX_RETRIES + 1, e.getMessage());
                if (attempt < MAX_RETRIES)
                {
                    try { Thread.sleep(300L * (attempt + 1)); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                }
            }
        }
        log.error("TTS 合成彻底失败", lastEx);
        throw new TtsException(TtsError.UPSTREAM_ERROR,
                "语音合成失败: " + (lastEx != null ? lastEx.getMessage() : "未知错误"), lastEx);
    }

    private byte[] doSynthesize(String text) throws Exception
    {
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .model(config.getTtsModel())
                .apiKey(config.getDashscopeApiKey())
                .text(text)
                .sampleRate(config.getTtsSampleRate())
                .format(SpeechSynthesisAudioFormat.WAV)
                .build();

        Callable<ByteBuffer> task = () -> new SpeechSynthesizer().call(param);
        Future<ByteBuffer> future = ttsPool.submit(task);
        try
        {
            ByteBuffer audio = future.get(SYNTH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            byte[] data = new byte[audio.remaining()];
            audio.get(data);
            log.info("TTS 合成完成 文本长度={} 音频={} bytes", text.length(), data.length);
            return data;
        }
        catch (TimeoutException te)
        {
            future.cancel(true);
            throw new TtsException(TtsError.TIMEOUT, "语音合成超时（" + SYNTH_TIMEOUT_MS + "ms）", te);
        }
        catch (ExecutionException ee)
        {
            Throwable cause = ee.getCause() != null ? ee.getCause() : ee;
            if (cause instanceof Exception) throw (Exception) cause;
            throw new RuntimeException(cause);
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            throw new TtsException(TtsError.INTERRUPTED, "合成被中断", ie);
        }
    }

    /** 按段落边界截断，保留最大可读性 */
    static String truncateByParagraph(String text, int maxLen)
    {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        int cut = text.lastIndexOf('\n', maxLen);
        if (cut < maxLen / 2) cut = text.lastIndexOf('。', maxLen);
        if (cut < maxLen / 2) cut = text.lastIndexOf('.', maxLen);
        if (cut < maxLen / 2) cut = maxLen;
        return text.substring(0, cut + 1);
    }

    // ====== 异常类型定义 ======

    public enum TtsError
    {
        EMPTY_TEXT,        // 空文本
        NOT_CONFIGURED,    // API Key 未配置/无效
        TIMEOUT,           // 合成超时
        UPSTREAM_ERROR,    // DashScope 5xx
        INTERRUPTED        // 中断
    }

    public static class TtsException extends Exception
    {
        private final TtsError error;
        public TtsException(TtsError error, String msg) { super(msg); this.error = error; }
        public TtsException(TtsError error, String msg, Throwable cause) { super(msg, cause); this.error = error; }
        public TtsError getError() { return error; }
    }
}
