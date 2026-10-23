package com.lab.service;

import com.lab.dao.StatDao;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务：各实验室周使用率
 */
@Service
public class StatService {

    /** 每周开放时长（按每天 12 小时、7 天计） */
    private static final double WEEKLY_OPEN_HOURS = 12 * 7;

    private final StatDao statDao;

    public StatService(StatDao statDao) {
        this.statDao = statDao;
    }

    public Map<String, Object> weeklyUsage() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDateTime start = monday.atStartOfDay();
        LocalDateTime end = start.plusDays(7);

        List<Map<String, Object>> rows = statDao.weeklyUsage(start, end);
        List<String> labels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        List<Double> rates = new ArrayList<>();
        List<Map<String, Object>> details = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String name = String.valueOf(row.get("labName"));
            double hours = row.get("hours") == null ? 0 : ((Number) row.get("hours")).doubleValue();
            double rate = Math.min(100.0, Math.round(hours / WEEKLY_OPEN_HOURS * 1000) / 10.0);
            labels.add(name);
            counts.add(row.get("count") == null ? 0 : ((Number) row.get("count")).intValue());
            rates.add(rate);

            Map<String, Object> detail = new HashMap<>();
            detail.put("labId", row.get("labId"));
            detail.put("labName", name);
            detail.put("count", row.get("count"));
            detail.put("hours", Math.round(hours * 10) / 10.0);
            detail.put("rate", rate);
            details.add(detail);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("labels", labels);
        data.put("values", counts);
        data.put("rates", rates);
        data.put("details", details);
        data.put("startDate", start.toLocalDate().toString());
        data.put("endDate", end.minusDays(1).toLocalDate().toString());
        return data;
    }
}
