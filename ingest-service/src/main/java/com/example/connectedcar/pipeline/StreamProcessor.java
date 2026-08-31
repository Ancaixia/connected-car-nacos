package com.example.connectedcar.pipeline;

import com.example.connectedcar.config.AppProperties;
import com.example.connectedcar.domain.AlarmEvent;
import com.example.connectedcar.domain.Telemetry;
import com.example.connectedcar.gateway.MqttMessage;
import com.example.connectedcar.storage.AlarmRepository;
import com.example.connectedcar.storage.TelemetryRepository;
import com.example.connectedcar.storage.VehicleCache;
import com.example.connectedcar.storage.VehicleRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 流处理任务（模拟 Flink 实时计算）。
 *
 * 真实场景：Flink 从 Kafka 消费，做清洗过滤、报警规则计算、窗口聚合后，
 * 结果写入 TDengine/ClickHouse 与报警服务。本示例用单线程消费循环等价演示。
 */
@Component
public class StreamProcessor {

    private static final Logger log = LoggerFactory.getLogger(StreamProcessor.class);

    private final KafkaBroker kafkaBroker;
    private final TelemetryRepository telemetryRepository;
    private final AlarmRepository alarmRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleCache vehicleCache;

    private final AtomicLong processedCount = new AtomicLong();

    private final Map<String, Boolean> alarmActive = new ConcurrentHashMap<>();

    private final AppProperties appProperties;

    public StreamProcessor(KafkaBroker kafkaBroker,
                           TelemetryRepository telemetryRepository,
                           AlarmRepository alarmRepository,
                           VehicleRepository vehicleRepository,
                           VehicleCache vehicleCache,
                           AppProperties appProperties) {
        this.kafkaBroker = kafkaBroker;
        this.telemetryRepository = telemetryRepository;
        this.alarmRepository = alarmRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleCache = vehicleCache;
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void start() {
        ExecutorService consumer = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "flink-stream-processor");
            thread.setDaemon(true);
            return thread;
        });
        consumer.submit(this::consumeLoop);
    }

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            Object raw = kafkaBroker.poll("telemetry");
            if (raw instanceof MqttMessage message
                    && message.getPayload() instanceof Telemetry telemetry) {
                process(telemetry);
            }
        }
    }

    private void process(Telemetry telemetry) {
        if (telemetry.getSpeed() < 0 || telemetry.getSpeed() > 300
                || telemetry.getRpm() < 0 || telemetry.getFuelPct() < 0
                || telemetry.getFuelPct() > 100) {
            log.warn("丢弃非法遥测: {}", telemetry.getVin());
            return;
        }

        detectAlarms(telemetry);

        telemetryRepository.insert(telemetry);

        vehicleRepository.updateLastState(
                telemetry.getVin(), telemetry.getSpeed(),
                telemetry.getLat(), telemetry.getLon(), telemetry.getTs());
        vehicleCache.putLatestState(
                telemetry.getVin(), telemetry.getSpeed(),
                telemetry.getLat(), telemetry.getLon(), telemetry.getTs());

        processedCount.incrementAndGet();
    }

    private void detectAlarms(Telemetry t) {
        // 每条数据都实时读取，Nacos 上改了阈值可立即生效。
        // 注意：不要把这些值再缓存到本类字段，否则会退化成启动时的固定值。
        AppProperties.Alarm cfg = appProperties.getAlarm();

        if (shouldFire("SPEEDING:" + t.getVin(), t.getSpeed() > cfg.getSpeedLimit())) {
            saveAlarm(t, "SPEEDING", "HIGH",
                    String.format("车速 %.0f km/h 超过限速 %.0f km/h", t.getSpeed(), cfg.getSpeedLimit()),
                    t.getSpeed());
        }
        if (shouldFire("LOW_FUEL:" + t.getVin(), t.getFuelPct() < cfg.getFuelLowLimit())) {
            saveAlarm(t, "LOW_FUEL", "MEDIUM",
                    String.format("油量剩余 %.1f%%，请及时加油", t.getFuelPct()),
                    t.getFuelPct());
        }
        if (shouldFire("HIGH_TEMP:" + t.getVin(), t.getEngineTemp() > cfg.getEngineTempLimit())) {
            saveAlarm(t, "HIGH_TEMP", "HIGH",
                    String.format("发动机温度 %.1f°C 过高，存在热失控风险", t.getEngineTemp()),
                    t.getEngineTemp());
        }
    }

    private boolean shouldFire(String key, boolean condition) {
        if (condition) {
            if (!Boolean.TRUE.equals(alarmActive.get(key))) {
                alarmActive.put(key, true);
                return true;
            }
            return false;
        }
        alarmActive.put(key, false);
        return false;
    }

    private void saveAlarm(Telemetry t, String type, String severity, String message, double value) {
        AlarmEvent alarm = new AlarmEvent();
        alarm.setVin(t.getVin());
        alarm.setTs(t.getTs());
        alarm.setType(type);
        alarm.setSeverity(severity);
        alarm.setMessage(message);
        alarm.setValue(value);
        alarmRepository.insert(alarm);
        log.info("[报警] {} {} {}", t.getVin(), type, message);
    }

    public long getProcessedCount() {
        return processedCount.get();
    }
}
