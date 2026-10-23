package com.lab.controller;

import com.lab.common.Result;
import com.lab.service.UploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传接口：实验报告 / 设备图片
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "type", required = false) String type,
                                              @RequestAttribute("uid") Integer uid,
                                              @RequestAttribute("username") String username) {
        return Result.ok("上传成功", uploadService.save(file, type, uid, username));
    }
}
