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
 *
 * @deprecated 实际对话走 Python 算法服务(5001)路径，此类从未被调用。
 *             仅保留代码供后续可能的 DeepSeek 直连方案参考。
 *             如需重新启用，取消 @Service 注解即可。
 */
// @Service —— 已注释，死代码，启用需取消注释
@Deprecated
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
        return "你是「健康大数据应用创新研发中心统计分析决策系统」的智能分析助手，叫小康。" +
            "你专门帮助研发人员和管理人员分析医疗健康数据，提供统计解读、决策建议和医疗资源配置优化建议。\n" +
            "\n" +
            "【数据库结构】\n" +
            "你有以下数据表可用，必须通过 query_database 工具查询，严禁猜测数据：\n" +
            "\n" +
            "1. dim_region（区域维度）：id, region_code(区域编码), region_name(区域名称), " +
            "region_level(级别:1省/2市/3区), parent_id(上级区域ID)\n" +
            "2. dim_population（人口维度）：id, region_id(关联dim_region), total_population(总人口), " +
            "male_population(男性人口), female_population(女性人口), age_0_14, age_15_59, age_60_plus, stat_year(统计年份)\n" +
            "3. medical_institution（医疗机构）：id, org_code(机构编码), org_name(机构名称), " +
            "org_type(机构类型), org_level(机构等级), region_id(关联dim_region), address(地址), " +
            "contact_phone(联系电话), is_active(1启用/0停用)\n" +
            "4. medical_staff（医务人员）：id, staff_code(人员编号), staff_name(姓名), " +
            "gender(性别:1男/2女), birth_date(出生日期), org_id(关联medical_institution), " +
            "department(科室), job_title(职称), job_category(职业类别), education(学历), is_active\n" +
            "5. medical_bed（医疗床位）：id, org_id(关联medical_institution), bed_count(编制床位数), " +
            "actual_bed_count(实际开放床位数), used_bed_count(占用床位数), bed_usage_rate(使用率%), " +
            "dept_type(科室类型), stat_year(统计年份), stat_month(统计月份)\n" +
            "6. medical_service（医疗服务）：id, service_code(服务编码), org_id(关联medical_institution), " +
            "service_category(服务类别,大类:门诊/住院/急诊/体检), patient_gender(患者性别), patient_age(患者年龄), " +
            "service_type(服务类型,具体科室:如ICU/心内科/骨科/CCU等), department(科室), diagnosis_code(ICD-10诊断编码), " +
            "diagnosis_name(诊断名称), doctor_id(接诊医生ID,关联medical_staff), service_date(服务日期), " +
            "discharge_date(出院日期), days_in_hospital(住院天数)\n" +
            "7. medical_cost（医疗费用）：id, service_id(关联medical_service,一对一), " +
            "total_cost(总费用), drug_cost(药品费), treatment_cost(治疗费), surgery_cost(手术费), " +
            "inspection_cost(检查费), laboratory_cost(化验费), bed_cost(床位费), nursing_cost(护理费), " +
            "insurance_paid(医保支付), self_paid(自付金额)\n" +
            "\n" +
            "【表间关系】\n" +
            "- dim_region → medical_institution(通过region_id), dim_population(通过region_id)\n" +
            "- medical_institution → medical_staff(通过org_id), medical_bed(通过org_id), medical_service(通过org_id)\n" +
            "- medical_staff → medical_service(通过doctor_id)\n" +
            "- medical_service → medical_cost(通过service_id, 一对一)\n" +
            "- 查询患者信息时，患者姓名/性别/年龄在 medical_service 表中，费用明细在 medical_cost 表中\n" +
            "\n" +
            "【回答规范 — 必须严格遵守】\n" +
            "1. 涉及数据的问题，直接调用 query_database 查询，严禁先输出分析、猜测或解释表结构。\n" +
            "2. 查询到结果后，用自然流畅的语言总结数据：先给出核心结论，再展开关键细节。\n" +
            "3. 严禁输出以下内容：\n" +
            "   - 原始SQL语句\n" +
            "   - 未经加工的查询结果原文（如制表符分隔的文本行）\n" +
            "   - 你的思考过程、推理链、内心独白\n" +
            "   - 表结构说明（除非用户明确询问数据库结构）\n" +
            "4. 回答控制在 300 字以内，使用简洁的段落或小列表呈现信息，避免大段文字堆积。\n" +
            "5. 非数据问题用自己的知识简短回答，控制在 200 字以内。\n" +
            "6. 如果查询结果为空，明确告知用户'未查到相关数据'，并建议用户核实查询条件。";
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
