package com.example.connectedcar.client;

import com.example.connectedcar.domain.AlarmEvent;
import com.example.connectedcar.domain.DashboardSummary;
import com.example.connectedcar.domain.Telemetry;
import com.example.connectedcar.domain.User;
import com.example.connectedcar.domain.Vehicle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 调用采集服务（ingest-service）的 OpenFeign 客户端。
 *
 * <p>FeignClient 的 name 即 Nacos 中注册的服务名；Spring Cloud OpenFeign 会据此
 * 从 Nacos 拉取实例列表并做负载均衡（底层 Ribbon/Spring Cloud LoadBalancer）。
 *
 * <p>内部接口前缀 {@code /api/internal} 与对外前端接口 {@code /api} 区分，避免被网关/鉴权误伤。
 */
@FeignClient(name = "ingest-service")
public interface IngestClient {

    /** 校验用户名密码（登录链路跨服务调用）。 */
    @PostMapping("/api/internal/auth/validate")
    Map<String, Object> validateLogin(@RequestBody Map<String, String> req);

    @GetMapping("/api/internal/vehicles")
    List<Vehicle> listVehicles();

    @GetMapping("/api/internal/vehicles/{vin}")
    Vehicle getVehicle(@PathVariable("vin") String vin);

    @GetMapping("/api/internal/vehicles/{vin}/telemetry")
    List<Telemetry> telemetry(@PathVariable("vin") String vin,
                              @RequestParam("limit") int limit);

    @GetMapping("/api/internal/alarms/recent")
    List<AlarmEvent> recentAlarms(@RequestParam("limit") int limit);

    @GetMapping("/api/internal/vehicles/{vin}/alarms")
    List<AlarmEvent> vehicleAlarms(@PathVariable("vin") String vin,
                                   @RequestParam("limit") int limit);

    @GetMapping("/api/internal/dashboard/summary")
    DashboardSummary dashboardSummary();

    @GetMapping("/api/internal/dashboard/pipeline")
    Map<String, Object> dashboardPipeline();
}
