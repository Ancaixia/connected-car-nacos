package com.example.connectedcar.storage;

import com.example.connectedcar.domain.AlarmEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/** 报警事件仓库。 */
@Repository
public class AlarmRepository {

    private static final RowMapper<AlarmEvent> MAPPER = (rs, rowNum) -> {
        AlarmEvent alarm = new AlarmEvent();
        alarm.setId(rs.getLong("id"));
        alarm.setVin(rs.getString("vin"));
        alarm.setTs(rs.getTimestamp("ts").toLocalDateTime());
        alarm.setType(rs.getString("type"));
        alarm.setSeverity(rs.getString("severity"));
        alarm.setMessage(rs.getString("message"));
        alarm.setValue(rs.getDouble("val"));
        return alarm;
    };

    private final JdbcTemplate jdbc;

    public AlarmRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(AlarmEvent alarm) {
        jdbc.update("""
                INSERT INTO alarm_event (vin, ts, type, severity, message, val)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                alarm.getVin(), Timestamp.valueOf(alarm.getTs()), alarm.getType(),
                alarm.getSeverity(), alarm.getMessage(), alarm.getValue());
    }

    public List<AlarmEvent> findRecent(int limit) {
        return jdbc.query("SELECT * FROM alarm_event ORDER BY ts DESC LIMIT ?", MAPPER, limit);
    }

    public List<AlarmEvent> findByVin(String vin, int limit) {
        return jdbc.query("""
                SELECT * FROM alarm_event WHERE vin = ? ORDER BY ts DESC LIMIT ?
                """, MAPPER, vin, limit);
    }

    public long countAll() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM alarm_event", Long.class);
        return count == null ? 0 : count;
    }
}
