package com.ruoyi.web.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.AsrService;
import com.ruoyi.system.service.ChatService;
import com.ruoyi.system.service.TtsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

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

    private final AsrService asrService;
    private final ChatService chatService;
    private final TtsService ttsService;

    public AlgorithmController(AsrService asrService, ChatService chatService, TtsService ttsService)
    {
        this.asrService = asrService;
        this.chatService = chatService;
        this.ttsService = ttsService;
    }

    /**
     * 智能对话 — 纯大模型对话
     * POST /algorithm/chat
     * Body: { "question": "..." }
     * 返回: { "msg": "回答内容", "code": 200 }
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, String> body)
    {
        String question = body != null ? body.get("question") : null;
        if (question == null || question.trim().isEmpty())
        {
            return error("问题不能为空");
        }
        String answer = chatService.chat(question.trim());
        return success(answer);
    }

    /**
     * 语音识别 — 纯 ASR
     * POST /algorithm/asr
     * 接收音频文件，返回识别文本
     * 返回: { "msg": "识别文本", "code": 200 }
     */
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

    /**
     * 语音合成 — 纯 TTS
     * POST /algorithm/tts
     * Body: { "text": "..." }
     * 返回音频文件流（wav 格式）
     */
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

    /**
     * 全链路：语音 → 对话 → 语音
     * POST /algorithm/pipeline
     * 接收音频文件，经过 ASR → DeepSeek → TTS，返回合成的音频
     * 返回音频文件流（wav 格式）
     */
    @PostMapping("/pipeline")
    public ResponseEntity<byte[]> pipeline(@RequestParam("file") MultipartFile file)
    {
        if (file.isEmpty())
        {
            return ResponseEntity.badRequest().body("请上传音频文件".getBytes());
        }
        try
        {
            // Step 1: ASR 语音识别
            log.info("Pipeline Step 1: ASR 语音识别开始");
            String recognizedText = asrService.recognize(file);
            log.info("Pipeline Step 1: ASR 完成，识别文本: {}", recognizedText);

            // 构造一个完整的查询上下文
            String question = "请根据以下内容进行分析和回答：\n" + recognizedText;

            // Step 2: DeepSeek 对话
            log.info("Pipeline Step 2: 大模型对话开始");
            String answer = chatService.chat(question);
            log.info("Pipeline Step 2: 大模型回答完成，长度: {}", answer.length());

            // Step 3: TTS 语音合成
            log.info("Pipeline Step 3: TTS 语音合成开始");
            byte[] audioData = ttsService.synthesize(answer);
            log.info("Pipeline Step 3: TTS 合成完成，大小: {} bytes", audioData.length);

            // 在响应头中附加识别文本和回答，方便前端展示
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

    // 保留原有的图像分类和目标检测接口（占位）
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
