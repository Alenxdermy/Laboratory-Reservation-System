package com.lab.dao;

import com.lab.entity.Reservation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约 DAO
 */
@Repository
public class ReservationDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String BASE_SQL =
            "SELECT r.*, u.username, l.name AS lab_name, l.capacity AS lab_capacity " +
            "FROM reservation r " +
            "LEFT JOIN `user` u ON r.user_id = u.id " +
            "LEFT JOIN lab l ON r.lab_id = l.id ";

    private static final RowMapper<Reservation> ROW_MAPPER = (rs, rowNum) -> {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setLabId(rs.getInt("lab_id"));
        if (rs.getTimestamp("start_time") != null) {
            r.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        }
        if (rs.getTimestamp("end_time") != null) {
            r.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        }
        r.setPeople(rs.getInt("people"));
        r.setPurpose(rs.getString("purpose"));
        r.setStatus(rs.getString("status"));
        if (rs.getTimestamp("create_time") != null) {
            r.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        }
        r.setUsername(rs.getString("username"));
        r.setLabName(rs.getString("lab_name"));
        r.setLabCapacity(rs.getInt("lab_capacity"));
        return r;
    };

    public int insert(Reservation r) {
        String sql = "INSERT INTO reservation (user_id, lab_id, start_time, end_time, people, purpose, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getLabId());
            ps.setObject(3, r.getStartTime());
            ps.setObject(4, r.getEndTime());
            ps.setInt(5, r.getPeople() == null ? 1 : r.getPeople());
            ps.setString(6, r.getPurpose());
            ps.setString(7, r.getStatus() == null ? "pending" : r.getStatus());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? 0 : key.intValue();
    }

    public Reservation findById(Integer id) {
        List<Reservation> list = jdbcTemplate.query(BASE_SQL + "WHERE r.id = ?", ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Reservation> findByUser(Integer userId) {
        return jdbcTemplate.query(BASE_SQL + "WHERE r.user_id = ? ORDER BY r.start_time DESC",
                ROW_MAPPER, userId);
    }

    public List<Reservation> findAll(String status, Integer labId, String keyword) {
        StringBuilder sql = new StringBuilder(BASE_SQL).append("WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND r.status = ?");
            args.add(status);
        }
        if (labId != null) {
            sql.append(" AND r.lab_id = ?");
            args.add(labId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.username LIKE ? OR l.name LIKE ?)");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        sql.append(" ORDER BY r.start_time DESC");
        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, args.toArray());
    }

    /**
     * 统计同实验室、时间段重叠、且状态为 pending/approved 的预约人数总和
     * 重叠判定：existing.start_time < newEnd AND existing.end_time > newStart
     */
    public int sumOverlappingPeople(Integer labId, LocalDateTime start, LocalDateTime end, Integer excludeId) {
        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(people), 0) FROM reservation " +
                "WHERE lab_id = ? AND status IN ('pending', 'approved') " +
                "AND start_time < ? AND end_time > ?");
        List<Object> args = new ArrayList<>();
        args.add(labId);
        args.add(end);
        args.add(start);
        if (excludeId != null) {
            sql.append(" AND id <> ?");
            args.add(excludeId);
        }
        Integer sum = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return sum == null ? 0 : sum;
    }

    public int updateStatus(Integer id, String status) {
        return jdbcTemplate.update("UPDATE reservation SET status = ? WHERE id = ?", status, id);
    }

    public int deleteById(Integer id) {
        return jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    public int countByLab(Integer labId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE lab_id = ?", Integer.class, labId);
        return count == null ? 0 : count;
    }
}
