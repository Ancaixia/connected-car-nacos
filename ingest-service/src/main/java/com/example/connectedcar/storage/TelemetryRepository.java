package com.example.connectedcar.storage;

import com.example.connectedcar.domain.Telemetry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 遥测时序仓库。
 *
 * 真实场景对应 TDengine/ClickHouse：
 * CREATE TABLE telemetry (ts TIMESTAMP, vin NCHAR(17), ...) TIMESTAMP(ts);
 * 按 (vin, ts) 组合索引/排序键，支持高频点查。
 */
@Repository
public class TelemetryRepository {

    private static final RowMapper<Telemetry> MAPPER = (rs, rowNum) -> {
        Telemetry t = new Telemetry();
        t.setId(rs.getLong("id"));
        t.setVin(rs.getString("vin"));
        t.setTs(rs.getTimestamp("ts").toLocalDateTime());
        t.setLat(rs.getDouble("lat"));
        t.setLon(rs.getDouble("lon"));
        t.setSpeed(rs.getDouble("speed"));
        t.setRpm(rs.getInt("rpm"));
        t.setFuelPct(rs.getDouble("fuel_pct"));
        t.setEngineTemp(rs.getDouble("engine_temp"));
        t.setGear(rs.getInt("gear"));
        return t;
    };

    private final JdbcTemplate jdbc;

    public TelemetryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(Telemetry t) {
        jdbc.update("""
                INSERT INTO telemetry (vin, ts, lat, lon, speed, rpm, fuel_pct, engine_temp, gear)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                t.getVin(), Timestamp.valueOf(t.getTs()), t.getLat(), t.getLon(),
                t.getSpeed(), t.getRpm(), t.getFuelPct(), t.getEngineTemp(), t.getGear());
    }

    public List<Telemetry> findByVin(String vin, int limit) {
        return jdbc.query("""
                SELECT * FROM (
                    SELECT * FROM telemetry WHERE vin = ? ORDER BY ts DESC LIMIT ?
                ) ORDER BY ts ASC
                """, MAPPER, vin, limit);
    }

    public long countAll() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM telemetry", Long.class);
        return count == null ? 0 : count;
    }

    public double avgSpeedLastMinute() {
        Timestamp threshold = Timestamp.valueOf(java.time.LocalDateTime.now().minusMinutes(1));
        Double avg = jdbc.queryForObject(
                "SELECT AVG(speed) FROM telemetry WHERE ts >= ?", Double.class, threshold);
        return avg == null ? 0 : Math.round(avg * 10) / 10.0;
    }
}
