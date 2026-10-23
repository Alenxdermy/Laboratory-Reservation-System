package com.lab.dao;

import com.lab.entity.OperationLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志 DAO
 */
@Repository
public class LogDao {

    private final JdbcTemplate jdbcTemplate;

    public LogDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<OperationLog> ROW_MAPPER = (rs, rowNum) -> {
        OperationLog log = new OperationLog();
        log.setId(rs.getInt("id"));
        log.setUserId((Integer) rs.getObject("user_id"));
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setDetail(rs.getString("detail"));
        if (rs.getTimestamp("create_time") != null) {
            log.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        }
        return log;
    };

    public int insert(OperationLog log) {
        String sql = "INSERT INTO `log` (user_id, username, action, detail) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, log.getUserId(), log.getUsername(), log.getAction(), log.getDetail());
    }

    public List<OperationLog> findAll(String keyword, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM `log` WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (username LIKE ? OR action LIKE ? OR detail LIKE ?)");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        sql.append(" ORDER BY id DESC LIMIT ?");
        args.add(limit);
        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, args.toArray());
    }
}
