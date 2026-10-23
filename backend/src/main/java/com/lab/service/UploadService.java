package com.lab.service;

import com.lab.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务：实验报告、设备图片
 */
@Service
public class UploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip");

    @Value("${lab.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    private final LogService logService;

    public UploadService(LogService logService) {
        this.logService = logService;
    }

    public Map<String, Object> save(MultipartFile file, String type,
                                    Integer userId, String username) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new BizException("不支持的文件类型：" + ext);
        }

        String dateFolder = LocalDate.now().toString().replace("-", "");
        File folder = new File(uploadDir, dateFolder);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new BizException("上传目录创建失败");
        }

        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File target = new File(folder, storedName);
        try {
            file.transferTo(target.getAbsoluteFile());
        } catch (IOException e) {
            throw new BizException("文件保存失败：" + e.getMessage());
        }

        String relativeUrl = "/uploads/" + dateFolder + "/" + storedName;
        Map<String, Object> data = new HashMap<>();
        data.put("name", originalName);
        data.put("storedName", storedName);
        data.put("size", file.getSize());
        data.put("type", type == null ? "report" : type);
        data.put("url", contextPath + relativeUrl);

        logService.record(userId, username, "上传文件", "文件：" + originalName + "，类型：" + type);
        return data;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BizException("文件缺少扩展名");
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
