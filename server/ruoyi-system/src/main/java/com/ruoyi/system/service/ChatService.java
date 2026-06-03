package com.ruoyi.system.service;

import com.ruoyi.common.config.AlgorithmConfig;
import com.ruoyi.system.domain.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 智能对话服务 — 基于 DeepSeek API，支持 function calling 和流式输出
 */
@Service
public class ChatService
{
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final AlgorithmConfig config;
    private final RestTemplate restTemplate;
    private final DatabaseQueryService queryService;
    private final ObjectMapper objectMapper;

    public ChatService(AlgorithmConfig config, RestTemplate restTemplate, DatabaseQueryService queryService) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper();
    }

    /** 定义工具列表 */
    private List<Map<String, Object>> getTools() {
        Map<String, Object> sqlParamProps = new HashMap<>();
        sqlParamProps.put("type", "object");
        Map<String, Object> sqlProps = new HashMap<>();
        sqlProps.put("type", "string");
        sqlProps.put("description", "完整的SELECT语句");
        Map<String, Object> sqlParamProperties = new HashMap<>();
        sqlParamProperties.put("sql", sqlProps);
        sqlParamProps.put("properties", sqlParamProperties);
        sqlParamProps.put("required", List.of("sql"));

        Map<String, Object> queryTool = new HashMap<>();
        queryTool.put("type", "function");
        Map<String, Object> queryFn = new HashMap<>();
        queryFn.put("name", "query_database");
        queryFn.put("description", "执行SQL查询语句获取数据库中的数据。支持查询任何表（medical_institution, medical_staff, medical_bed, medical_service, medical_cost, dim_population, dim_region等），适用于统计、列表、趋势分析等所有数据查询需求。仅支持SELECT操作。");
        queryFn.put("parameters", sqlParamProps);
        queryTool.put("function", queryFn);

        return List.of(queryTool);
    }

    /** 构建系统 prompt */
    private String buildSystemPrompt() {
        return "你是「健康大数据应用创新研发中心统计分析决策系统」的智能分析助手，叫小慧。" +
            "你专门帮助研发人员和管理人员分析医疗健康数据，提供统计解读、决策建议和医疗资源配置优化建议。" +
            "——以下是你的行为准则——\n" +
            "1. 语气亲切自然，像同事一样交流，不要冷冰冰，也不要过度热情浮夸。\n" +
            "2. 回答要结构化：如果涉及数据，优先用简洁的列表或短段落呈现，不要一大段文字挤在一起。\n" +
            "3. 当用户询问具体数据时（如「有多少家机构」「今年费用趋势」），使用 query_database 工具查询数据库获取真实数据，然后基于查询结果回答。\n" +
            "4. 如果用户问的问题不是数据相关的（如闲聊、常识），你可以用自己的知识直接回答。\n" +
            "5. 回答不要过长啰嗦，控制在 200 字以内，除非用户明确要求详细分析。\n" +
            "6. 数据库中的表包括但不限于：medical_institution(医疗机构), medical_staff(医务人员), medical_bed(医疗床位), medical_service(医疗服务), medical_cost(医疗费用), dim_population(人口), dim_region(区域)。";
    }

    /** 构建消息列表 */
    private List<Map<String, Object>> buildMessages(String question, List<ChatMessage> history) {
        List<Map<String, Object>> messageList = new ArrayList<>();
        messageList.add(Map.of("role", "system", "content", buildSystemPrompt()));
        if (history != null) {
            for (ChatMessage h : history) {
                messageList.add(Map.of("role", h.getRole(), "content", h.getContent()));
            }
        }
        messageList.add(Map.of("role", "user", "content", question));
        return messageList;
    }

    // ==================== 流式对话 ====================

    /**
     * 流式对话 — 通过 SseEmitter 逐块推送 DeepSeek 的 SSE 响应
     */
        public void chatStream(String question, List<ChatMessage> history, java.util.function.Consumer<String> onChunk, Runnable onDone, java.util.function.Consumer<String> onError) {
        String url = config.getDeepseekBaseUrl() + "/v1/chat/completions";

        try {
            List<Map<String, Object>> messageList = buildMessages(question, history);

            // 第一轮请求：带 tools 定义
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getDeepseekModel());
            requestBody.put("messages", messageList);
            requestBody.put("tools", getTools());
            requestBody.put("stream", true);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4096);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            URI deepseekUri = new URI(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) deepseekUri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + config.getDeepseekApiKey());
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                try (BufferedReader errorReader = new BufferedReader(
                        new java.io.InputStreamReader(conn.getErrorStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    StringBuilder errorBody = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorBody.append(line);
                    }
                    log.error("DeepSeek API 返回错误: {} body: {}", responseCode, errorBody);
                }
                onError.accept("AI 服务返回错误: " + responseCode);
                return;
            }

            // 逐行读取 SSE 流，处理 tool_calls
            StringBuilder fullContent = new StringBuilder();
            StringBuilder functionCallBuffer = new StringBuilder();
            String currentFunctionName = null;
            String currentArguments = null;
            boolean collectingFunction = false;

            try (BufferedReader reader = new BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(data);
                            com.fasterxml.jackson.databind.JsonNode choices = jsonNode.get("choices");
                            if (choices != null && choices.isArray() && choices.size() > 0) {
                                com.fasterxml.jackson.databind.JsonNode delta = choices.get(0).get("delta");
                                com.fasterxml.jackson.databind.JsonNode finishReason = choices.get(0).get("finish_reason");

                                if (delta != null) {
                                    // 处理文本内容
                                    if (delta.has("content") && !delta.get("content").isNull()) {
                                        String chunk = delta.get("content").asText();
                                        fullContent.append(chunk);
                                        onChunk.accept(chunk);
                                    }
                                    // 处理 tool_calls
                                    if (delta.has("tool_calls") && !delta.get("tool_calls").isNull()) {
                                        com.fasterxml.jackson.databind.JsonNode toolCalls = delta.get("tool_calls");
                                        for (com.fasterxml.jackson.databind.JsonNode tc : toolCalls) {
                                            com.fasterxml.jackson.databind.JsonNode fn = tc.get("function");
                                            if (fn != null) {
                                                if (fn.has("name") && !fn.get("name").isNull()) {
                                                    currentFunctionName = fn.get("name").asText();
                                                }
                                                if (fn.has("arguments") && !fn.get("arguments").isNull()) {
                                                    functionCallBuffer.append(fn.get("arguments").asText());
                                                }
                                            }
                                        }
                                        collectingFunction = true;
                                    }
                                }

                                // 检查是否结束（有 finish_reason）
                                if (finishReason != null && !finishReason.isNull() && !"null".equals(finishReason.asText())) {
                                    String reason = finishReason.asText();
                                    if ("tool_calls".equals(reason) && collectingFunction && currentFunctionName != null) {
                                        // AI 要调工具，执行查询
                                        log.info("流式检测到 tool_calls: {} args: {}", currentFunctionName, functionCallBuffer.toString());
                                        String result = "";
                                        if ("query_database".equals(currentFunctionName)) {
                                            Map<String, Object> args = objectMapper.readValue(functionCallBuffer.toString(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                                            String sql = (String) args.get("sql");
                                            result = queryService.executeQuery(sql);
                                        } else {
                                            result = "未知工具: " + currentFunctionName;
                                        }

                                        // 构造第二轮请求（不带 stream）
                                        Map<String, Object> toolMsg = new HashMap<>();
                                        toolMsg.put("role", "tool");
                                        toolMsg.put("tool_call_id", "call_" + System.currentTimeMillis());
                                        toolMsg.put("content", result);
                                        messageList.add(toolMsg);

                                        Map<String, Object> secondBody = new HashMap<>();
                                        secondBody.put("model", config.getDeepseekModel());
                                        secondBody.put("messages", messageList);
                                        secondBody.put("temperature", 0.7);
                                        secondBody.put("max_tokens", 2048);

                                        String secondJson = objectMapper.writeValueAsString(secondBody);

                                        // 发送第二轮请求（非流式，因为 function calling 结果通常不长）
                                        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                                        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                                        headers.setBearerAuth(config.getDeepseekApiKey());
                                        org.springframework.http.HttpEntity<Map<String, Object>> secondRequest = new org.springframework.http.HttpEntity<>(secondBody, headers);
                                        Map secondResponse = restTemplate.postForObject(url, secondRequest, Map.class);
                                        if (secondResponse != null && secondResponse.containsKey("choices")) {
                                            java.util.List<Map<String, Object>> secondChoices = (java.util.List<Map<String, Object>>) secondResponse.get("choices");
                                            if (!secondChoices.isEmpty()) {
                                                Map<String, Object> secondChoice = secondChoices.get(0);
                                                Map<String, Object> secondMessage = (Map<String, Object>) secondChoice.get("message");
                                                if (secondMessage != null && secondMessage.containsKey("content")) {
                                                    String finalContent = (String) secondMessage.get("content");
                                                    // 逐字推送
                                                    for (char c : finalContent.toCharArray()) {
                                                        onChunk.accept(String.valueOf(c));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            log.warn("解析 SSE 数据行失败: {}", data, e);
                        }
                    }
                }
            }

            log.info("流式对话完成，总长度: {}", fullContent.length());
            onDone.run();

        } catch (Exception e) {
            log.error("流式对话失败", e);
            onError.accept("AI 服务异常: " + e.getMessage());
        }
    }
public String chat(String question, List<ChatMessage> history) {
        String url = config.getDeepseekBaseUrl() + "/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getDeepseekApiKey());

        List<Map<String, Object>> messageList = buildMessages(question, history);

        // 第一轮请求：带 tools 定义
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getDeepseekModel());
        requestBody.put("messages", messageList);
        requestBody.put("tools", getTools());
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4096);

        log.info("发送到 DeepSeek 的第一轮请求");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            Map response = restTemplate.postForObject(url, request, Map.class);
            if (response == null || !response.containsKey("choices")) {
                return "抱歉，AI 返回格式异常，请稍后重试。";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices.isEmpty()) return "抱歉，AI 返回为空。";

            Map<String, Object> choice = choices.get(0);
            Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
            if (messageObj == null) return "抱歉，AI 返回异常。";

            // 检查是否有 tool_calls
            List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) messageObj.get("tool_calls");
            if (toolCalls != null && !toolCalls.isEmpty()) {
                String content = (String) messageObj.get("content");
                if (content == null) content = "";

                Map<String, Object> assistantMsg = new HashMap<>();
                assistantMsg.put("role", "assistant");
                assistantMsg.put("content", content);
                assistantMsg.put("tool_calls", toolCalls);
                messageList.add(assistantMsg);

                for (Map<String, Object> tc : toolCalls) {
                    String functionName = (String) ((Map<String, Object>) tc.get("function")).get("name");
                    String arguments = (String) ((Map<String, Object>) tc.get("function")).get("arguments");

                    log.info("工具调用: {} args: {}", functionName, arguments);

                    String result = "";
                    if ("query_database".equals(functionName)) {
                        Map<String, Object> args = objectMapper.readValue(arguments, new TypeReference<Map<String, Object>>() {});
                        String sql = (String) args.get("sql");
                        result = queryService.executeQuery(sql);
                    } else {
                        result = "未知工具: " + functionName;
                    }

                    Map<String, Object> toolMsg = new HashMap<>();
                    toolMsg.put("role", "tool");
                    toolMsg.put("tool_call_id", tc.get("id"));
                    toolMsg.put("content", result);
                    messageList.add(toolMsg);
                }

                // 第二轮请求
                Map<String, Object> secondBody = new HashMap<>();
                secondBody.put("model", config.getDeepseekModel());
                secondBody.put("messages", messageList);
                secondBody.put("temperature", 0.7);
                secondBody.put("max_tokens", 2048);

                log.info("发送到 DeepSeek 的第二轮请求(含工具结果)");
                HttpEntity<Map<String, Object>> secondRequest = new HttpEntity<>(secondBody, headers);
                Map secondResponse = restTemplate.postForObject(url, secondRequest, Map.class);

                if (secondResponse != null && secondResponse.containsKey("choices")) {
                    List<Map<String, Object>> secondChoices = (List<Map<String, Object>>) secondResponse.get("choices");
                    if (!secondChoices.isEmpty()) {
                        Map<String, Object> secondChoice = secondChoices.get(0);
                        Map<String, Object> secondMessage = (Map<String, Object>) secondChoice.get("message");
                        if (secondMessage != null && secondMessage.containsKey("content")) {
                            String finalContent = (String) secondMessage.get("content");
                            log.info("DeepSeek 最终回答完成，长度: {}", finalContent.length());
                            return finalContent;
                        }
                    }
                }
                return "抱歉，AI 处理查询结果时出错。";
            }

            if (messageObj.containsKey("content")) {
                String content = (String) messageObj.get("content");
                log.info("DeepSeek 回答完成(无工具调用)，长度: {}", content.length());
                return content;
            }

            return "抱歉，AI 返回格式异常。";
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败", e);
            return "AI 服务暂时不可用，错误: " + e.getMessage();
        }
    }
}
