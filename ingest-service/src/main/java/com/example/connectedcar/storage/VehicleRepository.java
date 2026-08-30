package com.example.connectedcar.storage;

import com.example.connectedcar.domain.Vehicle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/** 车辆主数据仓库（H2 模拟 MySQL/PostgreSQL 侧表）。 */
@Repository
public class VehicleRepository {

    private static final RowMapper<Vehicle> MAPPER = (rs, rowNum) -> {
        Vehicle vehicle = new Vehicle();
        vehicle.setVin(rs.getString("vin"));
        vehicle.setPlate(rs.getString("plate"));
        vehicle.setModel(rs.getString("model"));
        vehicle.setOwner(rs.getString("owner"));
        vehicle.setStatus(rs.getString("status"));
        vehicle.setLastSpeed(rs.getDouble("last_speed"));
        vehicle.setLastLat(rs.getDouble("last_lat"));
        vehicle.setLastLon(rs.getDouble("last_lon"));
        Timestamp lastSeen = rs.getTimestamp("last_seen");
        vehicle.setLastSeen(lastSeen == null ? null : lastSeen.toLocalDateTime());
        return vehicle;
    };

    private final JdbcTemplate jdbc;

    public VehicleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Vehicle> findAll() {
        return jdbc.query("SELECT * FROM vehicle ORDER BY vin", MAPPER);
    }

    public List<String> findAllVins() {
        return jdbc.queryForList("SELECT vin FROM vehicle ORDER BY vin", String.class);
    }

    public Vehicle findByVin(String vin) {
        return jdbc.query("SELECT * FROM vehicle WHERE vin = ?", MAPPER, vin)
                .stream().findFirst().orElse(null);
    }

    public void updateLastState(String vin, double speed, double lat, double lon, LocalDateTime ts) {
        jdbc.update("""
                UPDATE vehicle SET status = 'ONLINE', last_speed = ?, last_lat = ?,
                       last_lon = ?, last_seen = ? WHERE vin = ?
                """, speed, lat, lon, Timestamp.valueOf(ts), vin);
    }

    public void markOffline(LocalDateTime before) {
        jdbc.update("UPDATE vehicle SET status = 'OFFLINE' WHERE last_seen < ?", Timestamp.valueOf(before));
    }
}
