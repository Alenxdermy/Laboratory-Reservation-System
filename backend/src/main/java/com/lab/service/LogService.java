package com.lab.service;

import com.lab.dao.LogDao;
import com.lab.entity.OperationLog;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志服务
 */
@Service
public class LogService {

    private final LogDao logDao;

    public LogService(LogDao logDao) {
        this.logDao = logDao;
    }

    /**
     * 记录关键操作（谁、何时、做了什么）
     */
    public void record(Integer userId, String username, String action, String detail) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setDetail(detail);
        logDao.insert(log);
    }

    public List<OperationLog> list(String keyword, Integer limit) {
        int size = limit == null || limit <= 0 || limit > 500 ? 200 : limit;
        return logDao.findAll(keyword, size);
    }
}
