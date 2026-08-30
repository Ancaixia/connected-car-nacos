package com.example.connectedcar.domain;

import java.time.LocalDateTime;

/** 车辆遥测数据点（真实场景为 TDengine/ClickHouse 时序表的一行）。 */
public class Telemetry {

    private Long id;
    private String vin;
    private LocalDateTime ts;
    private double lat;
    private double lon;
    private double speed;
    private int rpm;
    private double fuelPct;
    private double engineTemp;
    private int gear;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public LocalDateTime getTs() {
        return ts;
    }

    public void setTs(LocalDateTime ts) {
        this.ts = ts;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    public double getFuelPct() {
        return fuelPct;
    }

    public void setFuelPct(double fuelPct) {
        this.fuelPct = fuelPct;
    }

    public double getEngineTemp() {
        return engineTemp;
    }

    public void setEngineTemp(double engineTemp) {
        this.engineTemp = engineTemp;
    }

    public int getGear() {
        return gear;
    }

    public void setGear(int gear) {
        this.gear = gear;
    }
}
