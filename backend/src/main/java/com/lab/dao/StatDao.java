package com.lab.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计 DAO：周使用率数据
 */
@Repository
public class StatDao {

    private final JdbcTemplate jdbcTemplate;

    public StatDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 统计各实验室在指定时间范围内的预约次数与已批准预约累计时长（小时）
     */
    public List<Map<String, Object>> weeklyUsage(LocalDateTime weekStart, LocalDateTime weekEnd) {
        String sql = "SELECT l.id AS lab_id, l.name AS lab_name, " +
                "COUNT(r.id) AS reservation_count, " +
                "COALESCE(SUM(CASE WHEN r.status = 'approved' " +
                "THEN TIMESTAMPDIFF(MINUTE, r.start_time, r.end_time) / 60.0 ELSE 0 END), 0) AS used_hours " +
                "FROM lab l " +
                "LEFT JOIN reservation r ON r.lab_id = l.id " +
                "AND r.start_time >= ? AND r.start_time < ? " +
                "GROUP BY l.id, l.name ORDER BY l.id";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, weekStart, weekEnd);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("labId", row.get("lab_id"));
            item.put("labName", row.get("lab_name"));
            item.put("count", row.get("reservation_count"));
            item.put("hours", row.get("used_hours"));
            result.add(item);
        }
        return result;
    }
}
