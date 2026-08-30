package com.example.connectedcar.simulator;

import com.example.connectedcar.domain.Telemetry;
import com.example.connectedcar.gateway.MessageGateway;
import com.example.connectedcar.storage.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 车端数据模拟器（扮演千万 T-Box 中的几台）。
 *
 * 每秒为每台车生成一个遥测点：速度做随机游走（模拟加减速），
 * 经纬度随速度小幅漂移，油量缓慢下降，发动机温度随转速波动。
 */
@Component
public class VehicleSimulator {

    private static final Logger log = LoggerFactory.getLogger(VehicleSimulator.class);

    private static final double[][] BASE_POSITIONS = {
            {31.2304, 121.4737},
            {39.9042, 116.4074},
            {22.5431, 114.0579},
            {30.5728, 104.0668},
            {30.2741, 120.1551},
            {32.0603, 118.7969}
    };

    private final MessageGateway gateway;
    private final VehicleRepository vehicleRepository;
    private final Random random = new Random();
    private final AtomicLong generatedCount = new AtomicLong();

    private final Map<String, CarState> states = new LinkedHashMap<>();

    public VehicleSimulator(MessageGateway gateway, VehicleRepository vehicleRepository) {
        this.gateway = gateway;
        this.vehicleRepository = vehicleRepository;
        List<String> vins = vehicleRepository.findAllVins();
        double[] seedSpeeds = {20, 40, 60, 80, 100, 165};
        double[] seedFuels = {100, 100, 100, 3, 100, 100};
        for (int i = 0; i < vins.size(); i++) {
            double[] pos = BASE_POSITIONS[i % BASE_POSITIONS.length];
            states.put(vins.get(i), new CarState(pos[0], pos[1], seedSpeeds[i], seedFuels[i]));
        }
    }

    @Scheduled(fixedRateString = "${app.simulator.interval-ms}")
    public void tick() {
        states.forEach((vin, state) -> {
            Telemetry telemetry = new Telemetry();
            telemetry.setVin(vin);
            telemetry.setTs(LocalDateTime.now());

            state.speed = clamp(state.speed + random.nextGaussian() * 4, 0, 180);
            state.fuel = clamp(state.fuel - random.nextDouble() * 0.02, 0, 100);
            if (random.nextInt(2000) == 0) {
                state.fuel = 100;
            }
            state.rpm = (int) clamp(800 + state.speed * 30 + random.nextGaussian() * 100, 700, 6500);
            state.engineTemp = clamp(78 + state.speed * 0.08 + random.nextGaussian() * 2, 60, 110);
            state.lat += state.speed / 3600.0 * 0.01 * (random.nextBoolean() ? 1 : -1);
            state.lon += state.speed / 3600.0 * 0.01 * (random.nextBoolean() ? 1 : -1);

            telemetry.setSpeed(round1(state.speed));
            telemetry.setFuelPct(round1(state.fuel));
            telemetry.setEngineTemp(round1(state.engineTemp));
            telemetry.setRpm(state.rpm);
            telemetry.setGear(computeGear(state.speed));
            telemetry.setLat(round4(state.lat));
            telemetry.setLon(round4(state.lon));

            gateway.onVehicleTelemetry(telemetry);
            generatedCount.incrementAndGet();
        });
    }

    private int computeGear(double speed) {
        if (speed < 1) return 0;
        if (speed < 20) return 1;
        if (speed < 40) return 2;
        if (speed < 60) return 3;
        if (speed < 90) return 4;
        if (speed < 120) return 5;
        return 6;
    }

    public long getGeneratedCount() {
        return generatedCount.get();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private static double round4(double value) {
        return Math.round(value * 10_000) / 10_000.0;
    }

    private static final class CarState {
        double speed;
        double fuel;
        int rpm = 800;
        double engineTemp = 80;
        double lat;
        double lon;

        CarState(double lat, double lon, double speed, double fuel) {
            this.lat = lat;
            this.lon = lon;
            this.speed = speed;
            this.fuel = fuel;
        }
    }
}
