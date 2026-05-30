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
    public String chat(String question, java.util.List<com.ruoyi.system.domain.ChatMessage> history)
    {
        String url = config.getDeepseekBaseUrl() + "/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getDeepseekApiKey());

        Map<String, Object> systemMessage = Map.of(
            "role", "system",
            "content", "你是「健康大数据应用创新研发中心统计分析决策系统」的智能分析助手，叫小慧。"
                + "你专门帮助研发人员和管理人员分析医疗健康数据，提供统计解读、决策建议和医疗资源配置优化建议。"
                + "——以下是你的行为准则——\n"
                + "1. 语气亲切自然，像同事一样交流，不要冷冰冰，也不要过度热情浮夸。\n"
                + "2. 回答要结构化：如果涉及数据，优先用简洁的列表或短段落呈现，不要一大段文字挤在一起。\n"
                + "3. 你能接触到的数据范围包括：人口信息、医疗卫生机构、医疗卫生人员、床位、医疗服务、医疗费用六大模块的统计数据和趋势分析。\n"
                + "4. 当用户问及具体数据（如「今年床位使用率是多少」），引导用户到对应统计页面查看可视化图表，你提供解读和分析建议。\n"
                + "5. 如果用户问的问题超出医疗健康数据范围，你可以用自己的知识回答，但会主动说明「这部分不是系统数据，是我自己的了解」。\n"
                + "6. 回答不要过长啰嗦，控制在 200 字以内，除非用户明确要求详细分析。"
        );

        // 构建消息列表：system提示词 + 历史消息 + 当前问题
        List<Map<String, Object>> messageList = new java.util.ArrayList<>();
        messageList.add(systemMessage);
        if (history != null) {
            for (com.ruoyi.system.domain.ChatMessage h : history) {
                messageList.add(Map.of("role", h.getRole(), "content", h.getContent()));
            }
        }
        messageList.add(Map.of("role", "user", "content", question));

        Map<String, Object> requestBody = Map.of(
            "model", config.getDeepseekModel(),
            "messages", messageList,
            "temperature", 0.7,
            "max_tokens", 2048
        );

        log.info("发送到 DeepSeek 的请求消息: {}", requestBody.get("messages"));
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












