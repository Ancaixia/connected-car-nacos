package com.example.connectedcar.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 查询/Web 服务入口（BFF / 网关侧）。
 *
 * 职责：
 * 1. 托管前端静态页（login.html / app.js）；
 * 2. 暴露面向前端的 REST API（/api/**），内部通过 OpenFeign 调用 ingest-service；
 * 3. 负责登录态：校验账号密码（调 ingest）、生成 token、用 Redis 存会话。
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.connectedcar.client")
@SpringBootApplication(scanBasePackages = "com.example.connectedcar")
public class QueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryApplication.class, args);
    }
}
