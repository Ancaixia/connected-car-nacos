package com.example.connectedcar.web;

import com.example.connectedcar.client.IngestClient;
import com.example.connectedcar.domain.AlarmEvent;
import com.example.connectedcar.domain.DashboardSummary;
import com.example.connectedcar.domain.Telemetry;
import com.example.connectedcar.domain.Vehicle;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 面向前端的 API（/api/**）。
 *
 * 这里不直接查库，而是经 OpenFeign 调 ingest-service 的内部接口，
 * 把远程数据原样返回给 Vue 大屏。前端无需感知后端已拆分为微服务。
 */
@RestController
@RequestMapping("/api")
public class FrontendController {

    private final IngestClient ingestClient;

    public FrontendController(IngestClient ingestClient) {
        this.ingestClient = ingestClient;
    }

    @GetMapping("/vehicles")
    public List<Vehicle> listVehicles() {
        return ingestClient.listVehicles();
    }

    @GetMapping("/vehicles/{vin}")
    public Vehicle getVehicle(@PathVariable String vin) {
        return ingestClient.getVehicle(vin);
    }

    @GetMapping("/vehicles/{vin}/telemetry")
    public List<Telemetry> telemetry(@PathVariable String vin,
                                     @RequestParam(defaultValue = "100") int limit) {
        return ingestClient.telemetry(vin, limit);
    }

    @GetMapping("/alarms/recent")
    public List<AlarmEvent> recentAlarms(@RequestParam(defaultValue = "50") int limit) {
        return ingestClient.recentAlarms(limit);
    }

    @GetMapping("/vehicles/{vin}/alarms")
    public List<AlarmEvent> vehicleAlarms(@PathVariable String vin,
                                          @RequestParam(defaultValue = "50") int limit) {
        return ingestClient.vehicleAlarms(vin, limit);
    }

    @GetMapping("/dashboard/summary")
    public DashboardSummary dashboardSummary() {
        return ingestClient.dashboardSummary();
    }

    @GetMapping("/dashboard/pipeline")
    public Map<String, Object> dashboardPipeline() {
        return ingestClient.dashboardPipeline();
    }
}
