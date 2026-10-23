package com.lab.dao;

import com.lab.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * 用户 DAO：直接操作数据库
 */
@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setRealName(rs.getString("real_name"));
        if (rs.getTimestamp("create_time") != null) {
            user.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        }
        return user;
    };

    public User findByUsername(String username) {
        String sql = "SELECT * FROM `user` WHERE username = ?";
        List<User> list = jdbcTemplate.query(sql, ROW_MAPPER, username);
        return list.isEmpty() ? null : list.get(0);
    }

    public User findById(Integer id) {
        String sql = "SELECT * FROM `user` WHERE id = ?";
        List<User> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(User user) {
        String sql = "INSERT INTO `user` (username, password, role, real_name) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getRealName());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? 0 : key.intValue();
    }

    public List<User> findAll() {
        return jdbcTemplate.query(
                "SELECT id, username, role, real_name, create_time FROM `user` ORDER BY id", ROW_MAPPER);
    }

    public int updateRole(Integer id, String role) {
        return jdbcTemplate.update("UPDATE `user` SET role = ? WHERE id = ?", role, id);
    }
}
