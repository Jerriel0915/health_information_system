package com.ruoyi.web.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.ChatMessage;
import com.ruoyi.system.service.AsrService;
import com.ruoyi.system.service.ChatService;
import com.ruoyi.system.service.ChatSessionService;
import com.ruoyi.system.service.TtsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 算法模块 — 智能分析助手
 *
 * 集成了语音识别 (ASR) + 大模型对话 (DeepSeek) + 语音合成 (TTS)
 */
@RestController
@RequestMapping("/algorithm")
public class AlgorithmController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(AlgorithmController.class);
    private static final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    private final AsrService asrService;
    private final ChatService chatService;
    private final ChatSessionService chatSessionService;
    private final TtsService ttsService;

    public AlgorithmController(AsrService asrService, ChatService chatService, TtsService ttsService, ChatSessionService chatSessionService)
    {
        this.asrService = asrService;
        this.chatService = chatService;
        this.ttsService = ttsService;
        this.chatSessionService = chatSessionService;
    }

    /**
     * 智能对话 — 流式输出
     * GET /algorithm/chat/stream?sessionId=xxx&question=xxx
     * 返回 SSE (text/event-stream)
     */
    @Anonymous
    @GetMapping("/chat/stream")
    public SseEmitter chatStream(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("question") String question)
    {
        // 超时时间 5 分钟
        SseEmitter emitter = new SseEmitter(300000L);

        // 保存用户消息
        if (sessionId != null && !sessionId.isEmpty()) {
            chatSessionService.saveMessage(sessionId, "user", question.trim());
        }

        StringBuilder fullAnswer = new StringBuilder();

        chatService.chatStream(question.trim(), null,
            // onChunk: 每收到一块就推给前端
            chunk -> {
                try {
                    emitter.send(SseEmitter.event().name("message").data(chunk));
                    fullAnswer.append(chunk);
                } catch (Exception e) {
                    log.error("发送 SSE chunk 失败", e);
                }
            },
            // onDone: 流结束，保存消息
            () -> {
                try {
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    if (sessionId != null && !sessionId.isEmpty() && fullAnswer.length() > 0) {
                        chatSessionService.saveMessage(sessionId, "assistant", fullAnswer.toString());
                    }
                } catch (Exception e) {
                    log.error("发送 SSE done 事件失败", e);
                }
                emitter.complete();
            },
            // onError: 出错
            error -> {
                try {
                    emitter.send(SseEmitter.event().name("error").data(error));
                } catch (Exception e) {
                    log.error("发送 SSE error 事件失败", e);
                }
                emitter.completeWithError(new RuntimeException(error));
            }
        );

        return emitter;
    }

    /**
     * 智能对话 — 非流式（保留，用于语音 pipeline 等场景）
     * POST /algorithm/chat
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, Object> body)
    {
        if (body == null) return error("参数不能为空");
        String sessionId = (String) body.get("sessionId");
        String question = (String) body.get("question");
        if (question == null || question.trim().isEmpty()) return error("问题不能为空");

        // 保存用户消息
        if (sessionId != null) {
            chatSessionService.saveMessage(sessionId, "user", question.trim());
        }

        // 获取历史消息
        java.util.List<ChatMessage> history = null;
        if (sessionId != null) {
            history = chatSessionService.getHistory(sessionId);
            if (history.size() > 20) {
                history = history.subList(history.size() - 20, history.size());
            }
        }

        // 调用 DeepSeek
        String answer = chatService.chat(question.trim(), history);

        // 保存回答
        if (sessionId != null) {
            chatSessionService.saveMessage(sessionId, "assistant", answer);
        }

        return success(answer);
    }

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

    @PostMapping("/asr")
    public AjaxResult asr(@RequestParam("file") MultipartFile file)
    {
        if (file.isEmpty())
        {
            return error("请上传音频文件");
        }
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

    @PostMapping("/tts")
    public ResponseEntity<byte[]> tts(@RequestBody Map<String, String> body)
    {
        String text = body != null ? body.get("text") : null;
        if (text == null || text.trim().isEmpty())
        {
            return ResponseEntity.badRequest().body("文本不能为空".getBytes());
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
        catch (Exception e)
        {
            log.error("TTS 合成失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("语音合成失败: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/pipeline")
    public ResponseEntity<byte[]> pipeline(@RequestParam("file") MultipartFile file)
    {
        if (file.isEmpty())
        {
            return ResponseEntity.badRequest().body("请上传音频文件".getBytes());
        }
        try
        {
            log.info("Pipeline Step 1: ASR 语音识别开始");
            String recognizedText = asrService.recognize(file);
            log.info("Pipeline Step 1: ASR 完成，识别文本: {}", recognizedText);

            String question = "请根据以下内容进行分析和回答：\n" + recognizedText;

            log.info("Pipeline Step 2: 大模型对话开始");
            String answer = chatService.chat(question, null);
            log.info("Pipeline Step 2: 大模型回答完成，长度: {}", answer.length());

            log.info("Pipeline Step 3: TTS 语音合成开始");
            byte[] audioData = ttsService.synthesize(answer);
            log.info("Pipeline Step 3: TTS 合成完成，大小: {} bytes", audioData.length);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/wav"));
            headers.setContentLength(audioData.length);
            headers.set("X-Asr-Text", java.net.URLEncoder.encode(recognizedText, "UTF-8"));
            headers.set("X-Answer-Text", java.net.URLEncoder.encode(answer, "UTF-8"));

            return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            log.error("Pipeline 全链路执行失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("全链路执行失败: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/image-classify")
    public AjaxResult imageClassify(@RequestParam("file") MultipartFile file)
    {
        return success("接口已预留，待接入图像分类模型");
    }

    @PostMapping("/object-detect")
    public AjaxResult objectDetect(@RequestParam("file") MultipartFile file)
    {
        return success("接口已预留，待接入目标检测模型");
    }
}
