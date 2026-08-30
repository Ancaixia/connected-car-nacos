package com.example.connectedcar.ingest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 采集服务入口。
 *
 * <p>职责：车端模拟器 → MQTT 网关 → Kafka → Flink 流处理（StreamProcessor）→ 落 H2/Redis，
 * 并把自己注册到 Nacos，供 query-service 通过服务名 {@code ingest-service} 用 OpenFeign 调用。
 */
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.example.connectedcar")
public class IngestApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestApplication.class, args);
    }
}
