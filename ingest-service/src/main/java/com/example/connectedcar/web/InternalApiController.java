package com.example.connectedcar.web;

import com.example.connectedcar.domain.AlarmEvent;
import com.example.connectedcar.domain.DashboardSummary;
import com.example.connectedcar.domain.Telemetry;
import com.example.connectedcar.domain.User;
import com.example.connectedcar.domain.Vehicle;
import com.example.connectedcar.service.AlarmService;
import com.example.connectedcar.service.StatsService;
import com.example.connectedcar.service.TelemetryService;
import com.example.connectedcar.service.UserService;
import com.example.connectedcar.service.VehicleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 内部查询/校验 API（前缀 /api/internal）。
 *
 * 仅供 query-service 通过 OpenFeign 调用，不直接面向前端/公网。
 */
@RestController
@RequestMapping("/api/internal")
public class InternalApiController {

    private final VehicleService vehicleService;
    private final TelemetryService telemetryService;
    private final AlarmService alarmService;
    private final StatsService statsService;
    private final UserService userService;

    public InternalApiController(VehicleService vehicleService,
                                 TelemetryService telemetryService,
                                 AlarmService alarmService,
                                 StatsService statsService,
                                 UserService userService) {
        this.vehicleService = vehicleService;
        this.telemetryService = telemetryService;
        this.alarmService = alarmService;
        this.statsService = statsService;
        this.userService = userService;
    }

    @PostMapping("/auth/validate")
    public Map<String, Object> validateLogin(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        return userService.validateLogin(username, password);
    }

    @GetMapping("/vehicles")
    public List<Vehicle> listVehicles() {
        return vehicleService.listVehicles();
    }

    @GetMapping("/vehicles/{vin}")
    public Vehicle getVehicle(@PathVariable String vin) {
        return vehicleService.getVehicle(vin);
    }

    @GetMapping("/vehicles/{vin}/telemetry")
    public List<Telemetry> telemetry(@PathVariable String vin,
                                     @RequestParam(defaultValue = "100") int limit) {
        return telemetryService.history(vin, limit);
    }

    @GetMapping("/alarms/recent")
    public List<AlarmEvent> recentAlarms(@RequestParam(defaultValue = "50") int limit) {
        return alarmService.recent(limit);
    }

    @GetMapping("/vehicles/{vin}/alarms")
    public List<AlarmEvent> vehicleAlarms(@PathVariable String vin,
                                          @RequestParam(defaultValue = "50") int limit) {
        return alarmService.byVehicle(vin, limit);
    }

    @GetMapping("/dashboard/summary")
    public DashboardSummary dashboardSummary() {
        return statsService.summary();
    }

    @GetMapping("/dashboard/pipeline")
    public Map<String, Object> dashboardPipeline() {
        return statsService.pipeline();
    }
}
