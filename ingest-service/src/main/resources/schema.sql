-- 车端/车辆静态信息表（真实场景在 MySQL/PostgreSQL）
CREATE TABLE IF NOT EXISTS vehicle (
    vin        VARCHAR(17) PRIMARY KEY,
    plate      VARCHAR(16),
    model      VARCHAR(64),
    owner      VARCHAR(64),
    status     VARCHAR(16) DEFAULT 'OFFLINE',
    last_speed DOUBLE DEFAULT 0,
    last_lat   DOUBLE DEFAULT 0,
    last_lon   DOUBLE DEFAULT 0,
    last_seen  TIMESTAMP
);

-- 遥测时序表（真实场景对应 TDengine / ClickHouse，按 (vin, ts) 排序）
CREATE TABLE IF NOT EXISTS telemetry (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin         VARCHAR(17) NOT NULL,
    ts          TIMESTAMP   NOT NULL,
    lat         DOUBLE,
    lon         DOUBLE,
    speed       DOUBLE,
    rpm         INT,
    fuel_pct    DOUBLE,
    engine_temp DOUBLE,
    gear        INT
);
CREATE INDEX IF NOT EXISTS idx_telemetry_vin_ts ON telemetry (vin, ts);

-- 报警事件表
CREATE TABLE IF NOT EXISTS alarm_event (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin      VARCHAR(17) NOT NULL,
    ts       TIMESTAMP   NOT NULL,
    type     VARCHAR(32),
    severity VARCHAR(16),
    message  VARCHAR(255),
    val      DOUBLE
);
CREATE INDEX IF NOT EXISTS idx_alarm_vin_ts ON alarm_event (vin, ts);

-- 用户账号表（登录从库查；真实场景在 MySQL/PostgreSQL）
CREATE TABLE IF NOT EXISTS app_user (
    username       VARCHAR(64) PRIMARY KEY,
    password       VARCHAR(80) NOT NULL,
    vin            VARCHAR(17),
    name           VARCHAR(64),
    role           VARCHAR(32) DEFAULT 'FLEET_ADMIN'
);
