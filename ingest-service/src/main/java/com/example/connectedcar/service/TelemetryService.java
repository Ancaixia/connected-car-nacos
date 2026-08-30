package com.example.connectedcar.service;

import com.example.connectedcar.domain.Telemetry;
import com.example.connectedcar.storage.TelemetryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;

    public TelemetryService(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    public List<Telemetry> history(String vin, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return telemetryRepository.findByVin(vin, safeLimit);
    }
}
