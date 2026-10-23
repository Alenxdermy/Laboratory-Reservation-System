package com.lab.controller;

import com.lab.common.Guard;
import com.lab.common.Result;
import com.lab.entity.OperationLog;
import com.lab.service.LogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志接口（仅管理员）
 */
@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public Result<List<OperationLog>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer limit,
                                           @RequestAttribute("role") String role) {
        Guard.requireAdmin(role);
        return Result.ok(logService.list(keyword, limit));
    }
}
