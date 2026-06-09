package com.ruoyi.system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 数据库查询服务 — 安全执行 SELECT 查询，供 DeepSeek function calling 使用
 */
@Service
public class DatabaseQueryService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseQueryService.class);
    private static final int MAX_ROWS = 100;

    private final JdbcTemplate jdbcTemplate;

    public DatabaseQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 安全执行 SELECT 查询，返回结果集
     */
    public String executeQuery(String sql) {
        String trimmed = sql.trim().toUpperCase();
        if (!trimmed.startsWith("SELECT")) {
            return "错误：只允许执行 SELECT 查询";
        }
        if (trimmed.contains("INTO OUTFILE") || trimmed.contains("INTO DUMPFILE")) {
            return "错误：不允许的文件操作";
        }

        log.info("执行查询SQL: {}", sql);
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            if (rows.isEmpty()) {
                return "查询结果为空";
            }
            // 限制返回行数
            if (rows.size() > MAX_ROWS) {
                rows = rows.subList(0, MAX_ROWS);
            }
            // 格式化为文本表格
            StringBuilder sb = new StringBuilder();
            // 表头
            List<String> columns = new ArrayList<>(rows.get(0).keySet());
            sb.append("查询结果 (").append(rows.size()).append(" 行):\n");
            sb.append(String.join("\t", columns)).append("\n");
            // 数据行
            for (Map<String, Object> row : rows) {
                List<String> values = new ArrayList<>();
                for (String col : columns) {
                    Object val = row.get(col);
                    values.add(val != null ? val.toString() : "NULL");
                }
                sb.append(String.join("\t", values)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("SQL查询失败: {}", e.getMessage());
            return "SQL执行错误: " + e.getMessage();
        }
    }
}
