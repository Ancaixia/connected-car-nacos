package com.example.connectedcar.storage;

import com.example.connectedcar.domain.Vehicle;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 车辆最新状态热数据（Redis）。
 *
 * 秒级实时查询用：流处理每处理一条遥测就覆盖一次 Redis 中的最新点，
 * TTL 自动过期（车辆离线后热数据失效，列表展示回落到 H2 的 last_* 值）。
 */
@Component
public class VehicleCache {

    private static final Logger log = LoggerFactory.getLogger(VehicleCache.class);

    private static final String KEY_PREFIX = "vehicle:status:";
    private static final long TTL_SECONDS = 30;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public VehicleCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 写入/覆盖某车最新状态。 */
    public void putLatestState(String vin, double speed, double lat, double lon, LocalDateTime ts) {
        try {
            Vehicle v = new Vehicle();
            v.setStatus("ONLINE");
            v.setLastSpeed(speed);
            v.setLastLat(lat);
            v.setLastLon(lon);
            v.setLastSeen(ts);
            redis.opsForValue().set(KEY_PREFIX + vin, objectMapper.writeValueAsString(v),
                    TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入 Redis 热数据失败 vin={}: {}", vin, e.getMessage());
        }
    }

    /** 读取某车最新状态；不存在/过期返回 null。 */
    public Vehicle getLatestState(String vin) {
        try {
            String json = redis.opsForValue().get(KEY_PREFIX + vin);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, Vehicle.class);
        } catch (Exception e) {
            log.warn("读取 Redis 热数据失败 vin={}: {}", vin, e.getMessage());
            return null;
        }
    }
}
