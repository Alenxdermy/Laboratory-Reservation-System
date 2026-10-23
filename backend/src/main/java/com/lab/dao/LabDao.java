package com.lab.dao;

import com.lab.entity.Lab;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * 实验室 DAO
 */
@Repository
public class LabDao {

    private final JdbcTemplate jdbcTemplate;

    public LabDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Lab> ROW_MAPPER = (rs, rowNum) -> {
        Lab lab = new Lab();
        lab.setId(rs.getInt("id"));
        lab.setName(rs.getString("name"));
        lab.setCapacity(rs.getInt("capacity"));
        lab.setStatus(rs.getString("status"));
        lab.setDescription(rs.getString("description"));
        if (rs.getTimestamp("create_time") != null) {
            lab.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        }
        return lab;
    };

    public List<Lab> findAll() {
        return jdbcTemplate.query("SELECT * FROM lab ORDER BY id", ROW_MAPPER);
    }

    public Lab findById(Integer id) {
        List<Lab> list = jdbcTemplate.query("SELECT * FROM lab WHERE id = ?", ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(Lab lab) {
        String sql = "INSERT INTO lab (name, capacity, status, description) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, lab.getName());
            ps.setInt(2, lab.getCapacity());
            ps.setString(3, lab.getStatus());
            ps.setString(4, lab.getDescription());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? 0 : key.intValue();
    }

    public int update(Lab lab) {
        String sql = "UPDATE lab SET name = ?, capacity = ?, status = ?, description = ? WHERE id = ?";
        return jdbcTemplate.update(sql, lab.getName(), lab.getCapacity(),
                lab.getStatus(), lab.getDescription(), lab.getId());
    }

    public int deleteById(Integer id) {
        return jdbcTemplate.update("DELETE FROM lab WHERE id = ?", id);
    }
}
