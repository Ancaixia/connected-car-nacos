package com.example.connectedcar.domain;

/** 大屏汇总指标。 */
public class DashboardSummary {

    private int vehicleCount;
    private int onlineCount;
    private long telemetryCount;
    private long alarmCount;
    private double avgSpeedLastMinute;

    public int getVehicleCount() {
        return vehicleCount;
    }

    public void setVehicleCount(int vehicleCount) {
        this.vehicleCount = vehicleCount;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public long getTelemetryCount() {
        return telemetryCount;
    }

    public void setTelemetryCount(long telemetryCount) {
        this.telemetryCount = telemetryCount;
    }

    public long getAlarmCount() {
        return alarmCount;
    }

    public void setAlarmCount(long alarmCount) {
        this.alarmCount = alarmCount;
    }

    public double getAvgSpeedLastMinute() {
        return avgSpeedLastMinute;
    }

    public void setAvgSpeedLastMinute(double avgSpeedLastMinute) {
        this.avgSpeedLastMinute = avgSpeedLastMinute;
    }
}
