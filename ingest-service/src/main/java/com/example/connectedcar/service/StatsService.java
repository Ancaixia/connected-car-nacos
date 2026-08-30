package com.example.connectedcar.service;

import com.example.connectedcar.domain.DashboardSummary;
import com.example.connectedcar.gateway.MessageGateway;
import com.example.connectedcar.pipeline.KafkaBroker;
import com.example.connectedcar.pipeline.StreamProcessor;
import com.example.connectedcar.simulator.VehicleSimulator;
import com.example.connectedcar.storage.AlarmRepository;
import com.example.connectedcar.storage.TelemetryRepository;
import com.example.connectedcar.storage.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StatsService {

    private final VehicleRepository vehicleRepository;
    private final TelemetryRepository telemetryRepository;
    private final AlarmRepository alarmRepository;
    private final VehicleSimulator simulator;
    private final MessageGateway gateway;
    private final KafkaBroker kafkaBroker;
    private final StreamProcessor streamProcessor;

    public StatsService(VehicleRepository vehicleRepository,
                        TelemetryRepository telemetryRepository,
                        AlarmRepository alarmRepository,
                        VehicleSimulator simulator,
                        MessageGateway gateway,
                        KafkaBroker kafkaBroker,
                        StreamProcessor streamProcessor) {
        this.vehicleRepository = vehicleRepository;
        this.telemetryRepository = telemetryRepository;
        this.alarmRepository = alarmRepository;
        this.simulator = simulator;
        this.gateway = gateway;
        this.kafkaBroker = kafkaBroker;
        this.streamProcessor = streamProcessor;
    }

    public DashboardSummary summary() {
        DashboardSummary summary = new DashboardSummary();
        summary.setVehicleCount(vehicleRepository.findAll().size());
        summary.setOnlineCount((int) vehicleRepository.findAll().stream()
                .filter(v -> "ONLINE".equals(v.getStatus())).count());
        summary.setTelemetryCount(telemetryRepository.countAll());
        summary.setAlarmCount(alarmRepository.countAll());
        summary.setAvgSpeedLastMinute(telemetryRepository.avgSpeedLastMinute());
        return summary;
    }

    public Map<String, Object> pipeline() {
        Map<String, Object> stages = new LinkedHashMap<>();
        stages.put("simulator", simulator.getGeneratedCount());
        stages.put("gateway", gateway.getReceivedCount());
        stages.put("kafka", kafkaBroker.getPublishedCount("telemetry"));
        stages.put("streamProcessor", streamProcessor.getProcessedCount());
        stages.put("storage", telemetryRepository.countAll());
        return stages;
    }
}
