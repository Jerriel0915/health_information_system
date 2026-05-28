package com.ruoyi.system.service;

import com.ruoyi.common.config.AlgorithmConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

/**
 * 智能对话服务 — 基于 DeepSeek API (OpenAI 兼容接口)
 */
@Service
public class ChatService
{
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final AlgorithmConfig config;
    private final RestTemplate restTemplate;

    public ChatService(AlgorithmConfig config, RestTemplate restTemplate)
    {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    /**
     * 发送问题到 DeepSeek，获取回答
     *
     * @param question 用户问题
     * @return DeepSeek 的回答文本
     */
    public String chat(String question)
    {
        String url = config.getDeepseekBaseUrl() + "/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getDeepseekApiKey());

        Map<String, Object> message = Map.of(
            "role", "user",
            "content", question
        );

        Map<String, Object> requestBody = Map.of(
            "model", config.getDeepseekModel(),
            "messages", List.of(message),
            "temperature", 0.7,
            "max_tokens", 2048
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try
        {
            Map response = restTemplate.postForObject(url, request, Map.class);
            if (response != null && response.containsKey("choices"))
            {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty())
                {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                    if (messageObj != null && messageObj.containsKey("content"))
                    {
                        String content = (String) messageObj.get("content");
                        log.info("DeepSeek 回答完成，长度: {}", content.length());
                        return content;
                    }
                }
            }
            log.warn("DeepSeek 返回格式异常: {}", response);
            return "抱歉，AI 返回格式异常，请稍后重试。";
        }
        catch (Exception e)
        {
            log.error("DeepSeek API 调用失败", e);
            return "AI 服务暂时不可用，错误: " + e.getMessage();
        }
    }
}

