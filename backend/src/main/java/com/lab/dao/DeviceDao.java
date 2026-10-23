package com.lab.dao;

import com.lab.entity.Device;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备 DAO
 */
@Repository
public class DeviceDao {

    private final JdbcTemplate jdbcTemplate;

    public DeviceDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Device> ROW_MAPPER = (rs, rowNum) -> {
        Device device = new Device();
        device.setId(rs.getInt("id"));
        device.setLabId(rs.getInt("lab_id"));
        device.setName(rs.getString("name"));
        device.setModel(rs.getString("model"));
        device.setStatus(rs.getString("status"));
        device.setLabName(rs.getString("lab_name"));
        if (rs.getTimestamp("create_time") != null) {
            device.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        }
        return device;
    };

    public List<Device> findAll(Integer labId, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT d.*, l.name AS lab_name FROM device d LEFT JOIN lab l ON d.lab_id = l.id WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (labId != null) {
            sql.append(" AND d.lab_id = ?");
            args.add(labId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (d.name LIKE ? OR d.model LIKE ?)");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        sql.append(" ORDER BY d.id");
        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, args.toArray());
    }

    public Device findById(Integer id) {
        List<Device> list = jdbcTemplate.query(
                "SELECT d.*, l.name AS lab_name FROM device d LEFT JOIN lab l ON d.lab_id = l.id WHERE d.id = ?",
                ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(Device device) {
        String sql = "INSERT INTO device (lab_id, name, model, status) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, device.getLabId());
            ps.setString(2, device.getName());
            ps.setString(3, device.getModel());
            ps.setString(4, device.getStatus());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? 0 : key.intValue();
    }

    public int update(Device device) {
        String sql = "UPDATE device SET lab_id = ?, name = ?, model = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, device.getLabId(), device.getName(),
                device.getModel(), device.getStatus(), device.getId());
    }

    public int deleteById(Integer id) {
        return jdbcTemplate.update("DELETE FROM device WHERE id = ?", id);
    }

    public int countByLab(Integer labId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM device WHERE lab_id = ?", Integer.class, labId);
        return count == null ? 0 : count;
    }
}
