package com.ruoyi.web.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 算法模块 — 预留接口
 *
 * 使用现成大模型 API（DeepSeek / 阿里云等），
 * 待 API Key 配置到位后接入真实调用。
 */
@RestController
@RequestMapping("/algorithm")
public class AlgorithmController extends BaseController
{

    /**
     * 智能对话 — 接入 DeepSeek API (SpringAI)
     * POST /algorithm/chat
     * Body: { "question": "..." }
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody(required = false) String question)
    {
        // TODO: 接入 SpringAI + DeepSeek API
        return success("接口已预留，待接入 DeepSeek API");
    }

    /**
     * 图像分类
     * POST /algorithm/image-classify
     * 接收图片文件，返回分类结果
     */
    @PostMapping("/image-classify")
    public AjaxResult imageClassify(@RequestParam("file") MultipartFile file)
    {
        // TODO: 接入图像分类模型
        return success("接口已预留，待接入模型");
    }

    /**
     * 目标检测
     * POST /algorithm/object-detect
     * 接收图片文件，返回检测结果
     */
    @PostMapping("/object-detect")
    public AjaxResult objectDetect(@RequestParam("file") MultipartFile file)
    {
        // TODO: 接入目标检测模型
        return success("接口已预留，待接入模型");
    }

    /**
     * 语音识别 (ASR) — 接入阿里云语音识别
     * POST /algorithm/asr
     * 接收音频文件，返回识别文本
     */
    @PostMapping("/asr")
    public AjaxResult asr(@RequestParam("file") MultipartFile file)
    {
        // TODO: 接入阿里云 ASR API
        return success("接口已预留，待接入阿里云 ASR");
    }

    /**
     * 语音合成 (TTS) — 接入阿里云语音合成
     * POST /algorithm/tts
     * Body: { "text": "..." }
     * 返回音频流
     */
    @PostMapping("/tts")
    public AjaxResult tts(@RequestBody(required = false) String text)
    {
        // TODO: 接入阿里云 TTS API
        return success("接口已预留，待接入阿里云 TTS");
    }
}