package com.example.connectedcar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ingest-service 的可热更新配置（托管在 Nacos 配置中心）。
 *
 * <p>用 {@code @ConfigurationProperties} 而非 {@code @Value}：
 * Nacos 配置变更时 Spring Cloud 会重新绑定本 Bean 的属性，
 * 只要使用方<b>每次都调用 getter</b>（而不是构造时缓存到自己的字段），
 * 就能立即拿到新值，无需重启。
 *
 * <p>注意：{@code @Value} 注入的字段不会自动刷新，必须配合 {@code @RefreshScope} 才生效。
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Alarm alarm = new Alarm();
    private final Simulator simulator = new Simulator();
    private final Pipeline pipeline = new Pipeline();

    public Alarm getAlarm() {
        return alarm;
    }

    public Simulator getSimulator() {
        return simulator;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    /** 报警阈值。 */
    public static class Alarm {

        /** 车速上限，超过则触发 SPEEDING。 */
        private double speedLimit = 160.0;

        /** 油量下限，低于则触发 LOW_FUEL。 */
        private double fuelLowLimit = 5.0;

        /** 发动机温度上限，超过则触发 HIGH_TEMP。 */
        private double engineTempLimit = 95.0;

        public double getSpeedLimit() {
            return speedLimit;
        }

        public void setSpeedLimit(double speedLimit) {
            this.speedLimit = speedLimit;
        }

        public double getFuelLowLimit() {
            return fuelLowLimit;
        }

        public void setFuelLowLimit(double fuelLowLimit) {
            this.fuelLowLimit = fuelLowLimit;
        }

        public double getEngineTempLimit() {
            return engineTempLimit;
        }

        public void setEngineTempLimit(double engineTempLimit) {
            this.engineTempLimit = engineTempLimit;
        }
    }

    /**
     * 模拟器节奏。
     *
     * <p>注意：{@code app.simulator.interval-ms} 目前用于 {@code @Scheduled(fixedRateString=...)}，
     * 调度周期在启动时就已注册，改这个值<b>不会</b>立即改变生成频率，需要重启才生效。
     */
    public static class Simulator {

        private long intervalMs = 1000;

        public long getIntervalMs() {
            return intervalMs;
        }

        public void setIntervalMs(long intervalMs) {
            this.intervalMs = intervalMs;
        }
    }

    /** 窗口聚合参数。 */
    public static class Pipeline {

        private int windowSeconds = 60;

        public int getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(int windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }
}
