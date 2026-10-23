package com.lab.controller;

import com.lab.common.Result;
import com.lab.service.StatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计接口：各实验室周使用率
 */
@RestController
@RequestMapping("/api/stats")
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/weekly-usage")
    public Result<Map<String, Object>> weeklyUsage() {
        return Result.ok(statService.weeklyUsage());
    }
}
