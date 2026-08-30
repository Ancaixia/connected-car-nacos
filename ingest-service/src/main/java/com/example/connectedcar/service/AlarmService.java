package com.example.connectedcar.service;

import com.example.connectedcar.domain.AlarmEvent;
import com.example.connectedcar.storage.AlarmRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public AlarmService(AlarmRepository alarmRepository) {
        this.alarmRepository = alarmRepository;
    }

    public List<AlarmEvent> recent(int limit) {
        return alarmRepository.findRecent(Math.max(1, Math.min(limit, 200)));
    }

    public List<AlarmEvent> byVehicle(String vin, int limit) {
        return alarmRepository.findByVin(vin, Math.max(1, Math.min(limit, 200)));
    }
}
