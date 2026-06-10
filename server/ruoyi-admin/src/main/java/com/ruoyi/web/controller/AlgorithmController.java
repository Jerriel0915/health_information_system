package com.ruoyi.web.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.ChatMessage;
import com.ruoyi.system.service.AsrService;

import com.ruoyi.system.service.ChatSessionService;
import com.ruoyi.system.service.TtsService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.system.service.IReportService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 算法模块 — 智能分析助手
 *
 * ASR/TTS 在 Java 本地处理（直接调 DashScope SDK）
 * 对话 LLM 转发到 Python 算法服务（5001）
 * 会话管理在 Java 本地处理
 */
@RestController
@RequestMapping("/algorithm")
public class AlgorithmController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(AlgorithmController.class);
    private static final String DEFAULT_PYTHON_ALGO = "http://localhost:5001";

    /**
     * M6 修复：SSE 流式对话专用线程池。改实例字段 + @PreDestroy 关闭。
     */
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    private final RestTemplate restTemplate;
    private final ChatSessionService chatSessionService;
    private final AsrService asrService;
    private final TtsService ttsService;
    private final IReportService reportService;
    private final com.ruoyi.common.config.AlgorithmConfig algorithmConfig;

    public AlgorithmController(RestTemplate restTemplate, ChatSessionService chatSessionService,
                               AsrService asrService, TtsService ttsService, IReportService reportService,
                               com.ruoyi.common.config.AlgorithmConfig algorithmConfig)
    {
        this.restTemplate = restTemplate;
        this.chatSessionService = chatSessionService;
        this.asrService = asrService;
        this.ttsService = ttsService;
        this.reportService = reportService;
        this.algorithmConfig = algorithmConfig;
    }

    private String pythonBaseUrl()
    {
        String url = algorithmConfig != null ? algorithmConfig.getPythonBaseUrl() : null;
        return (url == null || url.isEmpty()) ? DEFAULT_PYTHON_ALGO : url;
    }

    /**
     * M3 修复：判断是否走 Python 透传模式。空值安全。
     */
    private boolean isPythonMode()
    {
        String mode = algorithmConfig.getTtsMode();
        return "python".equalsIgnoreCase(mode);
    }

    /**
     * M6 修复：Spring 关闭时优雅停止 SSE 线程池。
     */
    @PreDestroy
    public void destroy()
    {
        sseExecutor.shutdown();
        try
        {
            if (!sseExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS))
            {
                sseExecutor.shutdownNow();
            }
        }
        catch (InterruptedException e)
        {
            sseExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("SSE 线程池已关闭");
    }

    // ==================== 会话管理（Java 本地） ====================

    @PostMapping("/chat/sessions")
    public AjaxResult getSessions(@RequestBody Map<String, Object> body)
    {
        Long userId = body != null ? Long.valueOf(body.get("userId").toString()) : null;
        if (userId == null) return error("用户ID不能为空");
        return success(chatSessionService.getSessionList(userId));
    }

    @PostMapping("/chat/session/create")
    public AjaxResult createSession(@RequestBody Map<String, Object> body)
    {
        Long userId = body != null ? Long.valueOf(body.get("userId").toString()) : null;
        if (userId == null) return error("用户ID不能为空");
        String sessionId = chatSessionService.createSession(userId);
        return success(sessionId);
    }

    @PostMapping("/chat/session/delete")
    public AjaxResult deleteSession(@RequestBody Map<String, Object> body)
    {
        String sessionId = body != null ? (String) body.get("sessionId") : null;
        if (sessionId == null) return error("会话ID不能为空");
        chatSessionService.deleteSession(sessionId);
        return success();
    }

    @PostMapping("/chat/history")
    public AjaxResult getHistory(@RequestBody Map<String, Object> body)
    {
        String sessionId = body != null ? (String) body.get("sessionId") : null;
        if (sessionId == null) return error("会话ID不能为空");
        return success(chatSessionService.getHistory(sessionId));
    }

    // ==================== ASR（Java 本地处理） ====================

    @PostMapping("/asr")
    public AjaxResult asr(@RequestParam("file") MultipartFile file)
    {
        if (file.isEmpty()) return error("请上传音频文件");
        try
        {
            String text = asrService.recognize(file);
            return success(text);
        }
        catch (Exception e)
        {
            log.error("ASR 识别失败", e);
            return error("语音识别失败: " + e.getMessage());
        }
    }

    // ==================== TTS（Java 本地处理 / Python 透传） ====================

    @PostMapping("/tts")
    public ResponseEntity<byte[]> tts(@RequestBody Map<String, String> body)
    {
        String text = body != null ? body.get("text") : null;
        if (text == null || text.trim().isEmpty())
        {
            return ResponseEntity.badRequest().body("文本不能为空".getBytes(StandardCharsets.UTF_8));
        }

        // Phase 4: mode=python 时透传到 Python 算法服务
        if (isPythonMode())
        {
            return forwardTtsToPython(text);
        }

        try
        {
            byte[] audioData = ttsService.synthesize(text.trim());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/wav"));
            headers.setContentLength(audioData.length);
            headers.set("Content-Disposition", "inline; filename=\"tts_output.wav\"");

            return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
        }
        catch (TtsService.TtsException e)
        {
            return mapTtsException(e, "语音合成失败");
        }
        catch (Exception e)
        {
            log.error("TTS 合成失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("语音合成失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 透传 TTS 请求到 Python 算法服务（Phase 4）
     */
    private ResponseEntity<byte[]> forwardTtsToPython(String text)
    {
        try
        {
            org.springframework.util.LinkedMultiValueMap<String, String> form = new org.springframework.util.LinkedMultiValueMap<>();
            form.add("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, String>> request =
                    new HttpEntity<>(form, headers);

            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                    pythonBaseUrl() + "/tts", request, byte[].class);

            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentType(MediaType.parseMediaType("audio/wav"));
            if (response.getBody() != null) respHeaders.setContentLength(response.getBody().length);
            respHeaders.set("X-Tts-Mode", "python");
            return new ResponseEntity<>(response.getBody(), respHeaders,
                    response.getStatusCode().is2xxSuccessful() ? HttpStatus.OK : response.getStatusCode());
        }
        catch (Exception e)
        {
            log.error("Python TTS 透传失败", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(("Python TTS 服务不可用: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 把 TtsException 映射为 HTTP 状态码（Phase 3）
     */
    private ResponseEntity<byte[]> mapTtsException(TtsService.TtsException e, String prefix)
    {
        HttpStatus status;
        String userMsg;
        switch (e.getError())
        {
            case EMPTY_TEXT:
                status = HttpStatus.BAD_REQUEST;
                userMsg = "文本不能为空";
                break;
            case NOT_CONFIGURED:
                status = HttpStatus.SERVICE_UNAVAILABLE;
                userMsg = "AI 服务未配置 API Key";
                break;
            case TIMEOUT:
                status = HttpStatus.GATEWAY_TIMEOUT;
                userMsg = "语音合成超时，请稍后重试";
                break;
            case INTERRUPTED:
                status = HttpStatus.SERVICE_UNAVAILABLE;
                userMsg = "语音合成被中断";
                break;
            case UPSTREAM_ERROR:
            default:
                status = HttpStatus.BAD_GATEWAY;
                userMsg = "上游语音服务异常";
                break;
        }
        log.warn("{} err={} msg={}", prefix, e.getError(), e.getMessage());
        return ResponseEntity.status(status)
                .body((prefix + ": " + userMsg).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 统计报告 TTS — 服务端组装真实朗读文本，再合成音频
     *
     * <p>使用方式：先调 {@code GET /algorithm/tts/report/text?type=bed} 预览文本，
     * 再调本端点获取音频。这样可避免在 HTTP 响应头中携带大文本（修复 H1 超长头）。
     *
     * @param body { "type": "bed|cost|service|..." }
     * @return 音频 wav 二进制
     */
    @PostMapping("/tts/report")
    public ResponseEntity<byte[]> ttsReport(@RequestBody Map<String, Object> body)
    {
        String type = body != null && body.get("type") != null ? body.get("type").toString() : "dashboard";

        String text = reportService.generateText(type);
        if (text == null || text.trim().isEmpty())
        {
            return ResponseEntity.badRequest().body("报告内容为空".getBytes(StandardCharsets.UTF_8));
        }

        // Phase 4: mode=python 时走 Python 服务
        if (isPythonMode())
        {
            return forwardTtsToPython(text);
        }

        try
        {
            byte[] audioData = ttsService.synthesize(text.trim());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/wav"));
            headers.setContentLength(audioData.length);
            headers.set("Content-Disposition", "inline; filename=\"report_" + type + ".wav\"");
            headers.set("X-Report-Type", type);
            return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
        }
        catch (TtsService.TtsException e)
        {
            return mapTtsException(e, "报告语音合成失败");
        }
        catch (Exception e)
        {
            log.error("报告 TTS 合成失败 type={}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("报告语音合成失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 报告文本预览 — 不合成音频，只返回朗读文本
     */
    @GetMapping("/tts/report/text")
    public AjaxResult ttsReportText(@RequestParam(name = "type", defaultValue = "dashboard") String type)
    {
        return success(reportService.generateReport(type));
    }

    // ==================== Pipeline（转发到 Python 算法服务） ====================

    @PostMapping("/pipeline")
    public ResponseEntity<byte[]> pipeline(@RequestParam("file") MultipartFile file)
    {
        try
        {
            org.springframework.core.io.ByteArrayResource fileResource = new org.springframework.core.io.ByteArrayResource(file.getBytes())
            {
                @Override
                public String getFilename() { return file.getOriginalFilename(); }
            };

            org.springframework.util.LinkedMultiValueMap<String, Object> form = new org.springframework.util.LinkedMultiValueMap<>();
            form.add("file", fileResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> request =
                    new HttpEntity<>(form, headers);

            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                    pythonBaseUrl() + "/pipeline", request, byte[].class);

            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentType(MediaType.parseMediaType("audio/wav"));
            if (response.getBody() != null)
            {
                respHeaders.setContentLength(response.getBody().length);
            }
            if (response.getHeaders().containsHeader("X-Asr-Text"))
            {
                respHeaders.set("X-Asr-Text", response.getHeaders().getFirst("X-Asr-Text"));
            }
            if (response.getHeaders().containsHeader("X-Answer-Text"))
            {
                respHeaders.set("X-Answer-Text", response.getHeaders().getFirst("X-Answer-Text"));
            }

            return new ResponseEntity<>(response.getBody(), respHeaders, HttpStatus.OK);
        }
        catch (Exception e)
        {
            log.error("Pipeline 转发失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("全链路执行失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    // ==================== LLM 对话（转发到 Python 算法服务 5001） ====================

    @Anonymous
    @GetMapping("/chat/stream")
    public SseEmitter chatStream(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("question") String question)
    {
        SseEmitter emitter = new SseEmitter(300000L) {
            @Override
            protected void extendResponse(ServerHttpResponse outputMessage) {
                super.extendResponse(outputMessage);
                outputMessage.getHeaders().set("X-Accel-Buffering", "no");
                outputMessage.getHeaders().set("Cache-Control", "no-cache, no-transform");
                outputMessage.getHeaders().set("Connection", "keep-alive");
            }
        };

        if (sessionId != null && !sessionId.isEmpty()) {
            chatSessionService.saveMessage(sessionId, "user", question.trim());
        }

        StringBuilder fullAnswer = new StringBuilder();

        sseExecutor.execute(() -> {
            try
            {
                String url = pythonBaseUrl() + "/chat/stream?question="
                        + URLEncoder.encode(question, "UTF-8")
                        + "&session_id=" + URLEncoder.encode(sessionId, "UTF-8");

                URL targetUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection(java.net.Proxy.NO_PROXY);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "text/event-stream");
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200)
                {
                    try (BufferedReader errReader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)))
                    {
                        StringBuilder errBody = new StringBuilder();
                        String line;
                        while ((line = errReader.readLine()) != null) errBody.append(line);
                        log.error("算法服务返回错误: {} body: {}", responseCode, errBody);
                    }
                    emitter.send(SseEmitter.event().name("error").data("AI 服务返回错误: " + responseCode));
                    emitter.complete();
                    return;
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)))
                {
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        if (line.startsWith("data: "))
                        {
                            String data = line.substring(6).trim();
                            if ("[DONE]".equals(data))
                            {
                                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                                break;
                            }
                            fullAnswer.append(data);
                            emitter.send(SseEmitter.event().name("message").data(data));
                        }
                    }
                }
                emitter.complete();

                if (sessionId != null && !sessionId.isEmpty() && fullAnswer.length() > 0) {
                    chatSessionService.saveMessage(sessionId, "assistant", fullAnswer.toString());
                }
            }
            catch (Exception e)
            {
                log.error("流式对话转发失败", e);
                try { emitter.send(SseEmitter.event().name("error").data("AI 服务异常: " + e.getMessage())); }
                catch (Exception ignored) {}
                emitter.completeWithError(new RuntimeException(e));
            }
        });

        return emitter;
    }

    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, Object> body)
    {
        if (body == null) return error("参数不能为空");
        String sessionId = (String) body.get("sessionId");
        String question = (String) body.get("question");
        if (question == null || question.trim().isEmpty()) return error("问题不能为空");

        if (sessionId != null) {
            chatSessionService.saveMessage(sessionId, "user", question.trim());
        }

        try
        {
            org.springframework.util.LinkedMultiValueMap<String, String> form = new org.springframework.util.LinkedMultiValueMap<>();
            form.add("question", question);
            form.add("session_id", sessionId != null ? sessionId : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, String>> request =
                    new HttpEntity<>(form, headers);

            Map response = restTemplate.postForObject(pythonBaseUrl() + "/chat", request, Map.class);
            String answer = "";
            if (response != null && Integer.valueOf(200).equals(response.get("code")))
            {
                answer = (String) response.get("data");
            }
            else
            {
                String msg = response != null ? (String) response.get("msg") : "未知错误";
                return error("AI 服务错误: " + msg);
            }

            if (sessionId != null && answer != null) {
                chatSessionService.saveMessage(sessionId, "assistant", answer);
            }

            return success(answer);
        }
        catch (Exception e)
        {
            log.error("对话转发失败", e);
            return error("AI 服务异常: " + e.getMessage());
        }
    }

    // ==================== 图像分类 / 异常检测（转发到 Python 算法服务） ====================

    @PostMapping("/image-classify")
    public AjaxResult imageClassify(@RequestParam("file") MultipartFile file)
    {
        try {
            org.springframework.core.io.ByteArrayResource fileResource = new org.springframework.core.io.ByteArrayResource(file.getBytes())
            {
                @Override
                public String getFilename() { return file.getOriginalFilename(); }
            };

            org.springframework.util.LinkedMultiValueMap<String, Object> form = new org.springframework.util.LinkedMultiValueMap<>();
            form.add("file", fileResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> request =
                    new HttpEntity<>(form, headers);

            Map response = restTemplate.postForObject(pythonBaseUrl() + "/classify", request, Map.class);
            if (response != null && Integer.valueOf(200).equals(response.get("code")))
            {
                return success(response.get("data"));
            }
            return success(response);
        }
        catch (Exception e)
        {
            log.error("图像分类转发失败", e);
            return error("图像分类服务异常: " + e.getMessage());
        }
    }

    // ==================== 骨骼分类 ====================
    @PostMapping("/predict")
    public AjaxResult predict(@RequestParam("file") MultipartFile file)
    {
        try {
            org.springframework.core.io.ByteArrayResource fileResource = new org.springframework.core.io.ByteArrayResource(file.getBytes())
            {
                @Override
                public String getFilename() { return file.getOriginalFilename(); }
            };

            org.springframework.util.LinkedMultiValueMap<String, Object> form = new org.springframework.util.LinkedMultiValueMap<>();
            form.add("file", fileResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> request =
                new HttpEntity<>(form, headers);

            Map response = restTemplate.postForObject(pythonBaseUrl() + "/predict", request, Map.class);
            if (response != null && Integer.valueOf(200).equals(response.get("code")))
            {
                return success(response.get("data"));
            }
            return success(response);
        }
        catch (Exception e)
        {
            log.error("骨骼分类转发失败", e);
            return error("骨骼分类服务异常: " + e.getMessage());
        }
    }

        @PostMapping("/object-detect")
    public AjaxResult objectDetect()
    {
        try
        {
            Map response = restTemplate.postForObject(pythonBaseUrl() + "/detect", null, Map.class);
            if (response != null && Integer.valueOf(200).equals(response.get("code")))
            {
                return success(response.get("data"));
            }
            return success(response);
        }
        catch (Exception e)
        {
            log.error("异常检测转发失败", e);
            return error("异常检测服务异常: " + e.getMessage());
        }
    }
}
