package com.example.connectedcar.domain;

import java.time.LocalDateTime;

/** 报警事件（流处理实时识别后落库，推送 App / 大屏）。 */
public class AlarmEvent {

    private Long id;
    private String vin;
    private LocalDateTime ts;
    /** SPEEDING / LOW_FUEL / HIGH_TEMP */
    private String type;
    /** HIGH / MEDIUM / LOW */
    private String severity;
    private String message;
    private double value;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
