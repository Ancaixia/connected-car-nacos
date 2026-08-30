# 车联网云端数据平台 · 岗位分析 + Java Spring & Vue 示例项目

本文档分两部分：

- **Part 1｜岗位分析**：按“车端采集上传 → 云端接入 → 消息管道 → 存储分析 → 应用服务”的业务本质，拆解车联网 Java 后端岗位的技术栈、匹配度与面试准备。
- **Part 2｜示例项目**：一个开箱可跑的 Spring Boot + Vue 项目，完整演示上述数据链路（车端模拟器 → MQTT 网关 → Kafka → Flink → 时序存储 → REST API → Vue 大屏）。

---

# Part 1｜岗位分析

## 1. 岗位与业务场景理解

**核心结论：这个岗位值得投。** 它属于车联网（Connected Car）云端数据平台方向，业务本质是一条数据管道：

```
车端采集上传 → 云端接入 → 消息管道 → 存储分析 → 应用服务
```

把“每辆车上传数据”这句话拆开，实际是：

1. **车端（T-Box / 车机）**：通过 CAN 总线采集发动机转速、车速、油量、电池电压、GPS 等信号；
2. **上传**：T-Box 内置 4G/5G 通信模组，通过 **MQTT over TLS** 长连接上报到云端（或走 HTTPS 车载网关）；
3. **云端处理**：海量车辆（几十万到千万级）每 1~10 秒上报一条遥测，云端要能扛住高并发接入、低延迟转发、实时规则计算和规模化存储。

这是一个典型的 **IoT + 大数据实时链路** 场景，面试时把这条链路讲清楚，比背八股更有说服力。

## 2. 完整数据链路

| 环节 | 角色 | 典型选型 |
|------|------|----------|
| 车端 | T-Box 采集 CAN 数据，4G/5G + MQTT 上报 | 嵌入式 SDK / 车规通信模组 |
| 消息网关 | 海量 MQTT 长连接接入、鉴权、QoS、Topic 路由 | EMQX（EMQ X）+ Netty |
| 消息管道 | 削峰填谷、解耦、多消费者 | Kafka |
| 流处理 | 清洗、规则报警、窗口聚合、实时指标 | Flink / Flink SQL |
| 存储 | 时序数据、车辆主数据、报警事件 | TDengine / ClickHouse、MySQL/PostgreSQL |
| 应用服务 | 业务 API：车辆管理、轨迹回放、统计报表 | Spring Boot 微服务（Spring Cloud） |
| 应用端 | App / 车机大屏 / 运营后台 | Vue / 小程序 / App |

一句话版本：**车端 T-Box 把 CAN 数据打成 MQTT 消息上传 → EMQX 网关接入 → Kafka 缓冲 → Flink 实时计算（报警、聚合）→ 结果写时序库 → Spring 微服务提供 API → Vue 大屏展示。**

## 3. 分层技术栈详解

### 3.1 接入层：MQTT（EMQX）+ Netty

- **MQTT** 是车联网事实标准协议（低带宽、低功耗、QoS 0/1/2、离线遗嘱）。
- **EMQX** 是开源 MQTT Broker，单集群可支撑百万连接；负责设备鉴权（用户名密码/证书）、Topic 权限（设备只能发布自己 VIN 的 Topic）、消息路由。
- **Netty** 常用于自研高并发网关（TCP 私有协议、GB/T 32960 国标协议接入），掌握 Netty 的 EventLoop、ChannelHandler、粘包拆包是加分项。
- 面试考点：QoS 语义、心跳保活、消息幂等、设备上下线感知。

### 3.2 消息管道：Kafka

- 定位：削峰填谷、异步解耦、多副本高可用、消费组水平扩展。
- 车联网常见用法：`telemetry` Topic（原始遥测）、`alarm` Topic（报警）、`command` Topic（远程控车指令下发）。
- 面试考点：分区与顺序性、消费者组 Rebalance、消息不丢失/不重复（ACK、offset 提交、幂等生产者）、积压排查。

### 3.3 流处理：Flink

- 消费 Kafka 原始遥测，做实时计算：
  - **过滤清洗**：非法数据（超范围车速、NaN）丢弃；
  - **规则报警**：超速、低油量、电池高温、故障码（DTC）→ 生成报警事件；
  - **窗口聚合**：每车每分钟/每小时的平均车速、里程、能耗；
  - **状态管理**：车辆连续上报状态、设备在线率。
- 面试考点：Watermark 与乱序、窗口类型（Tumbling/Sliding/Event-time）、状态与 Checkpoint、背压。

### 3.4 存储：TDengine / ClickHouse

- **时序数据**（遥测、轨迹）量级最大，按时间维度写入和查询，选 TDengine 或 ClickHouse：
  - TDengine：物联网时序库，超级表 + 子表模型（一张车表 = 一个 VIN），写入吞吐高；
  - ClickHouse：列存，适合分析型聚合。
- **关系数据**（车辆档案、用户、订单）放 MySQL/PostgreSQL。
- 面试考点：时序模型设计（Tag/Field/Timestamp）、分区与保留策略、冷热分离、聚合下推。

### 3.5 应用服务：Spring Cloud

- 微服务拆分示例：`device-service`（设备管理）、`telemetry-service`（遥测查询）、`alarm-service`（报警）、`user-service`（用户）、`command-service`（指令下发）。
- 基础设施：Nacos（注册中心/配置中心）、Gateway（网关）、OpenFeign（服务调用）、Sentinel（限流降级）、SkyWalking（链路追踪）。
- 面试考点：服务注册发现、网关路由与鉴权、分布式事务（远程控车要保证“指令必达+结果回执”）、缓存（Redis：车辆在线状态、热数据）。

### 3.6 安全合规

- 传输加密（TLS/mTLS）、设备证书鉴权、数据脱敏（位置、车主信息）、GDPR/个保法合规；
- 远程控车等高风险指令要有风控：二次确认、操作审计、灰度。

## 4. 技术栈总览表（按优先级）

| 优先级 | 环节 | 选型 | 掌握要求 |
|--------|------|------|----------|
| P0 | 服务端语言 | Java 17 + Spring Boot 3 | 必会，日常主力 |
| P0 | 微服务生态 | Spring Cloud Alibaba（Nacos/Gateway/Feign/Sentinel） | 必会 |
| P0 | 消息中间件 | Kafka | 必会，重点 |
| P1 | MQTT 接入 | EMQX + MQTT 协议 | 高优 |
| P1 | 时序存储 | TDengine 或 ClickHouse | 高优（二选一精学） |
| P1 | 实时计算 | Flink / Flink SQL | 高优 |
| P2 | 缓存 | Redis | 常规 |
| P2 | 前端 | Vue 3（大屏/后台） | 能看懂、能改即可 |
| P2 | 运维 | Docker/K8s、日志监控 | 了解 |

## 5. 你的技能匹配度（Go → Java 迁移视角）

假设你有 Go 并发与中间件经验，迁移逻辑如下：

| 维度 | 你的 Go 经验 | Java 侧对应 | 迁移要点 |
|------|--------------|-------------|----------|
| 并发模型 | goroutine + channel | 线程池 + 队列（ExecutorService、BlockingQueue） | 概念互通，重点是线程池参数与阻塞队列选型 |
| 网络编程 | net/http、TCP 服务 | Netty 事件驱动模型 | 读一遍 Netty 的 EventLoop/Handler 机制即可 |
| 中间件 | Redis/Kafka/MQ 使用经验 | 同样中间件，Java 客户端 API | 客户端 API 换皮，核心原理不变，加分 |
| 工程化 | Go 项目结构 | Maven 多模块、Spring Boot 自动装配 | 重点是 Spring 的 IoC/DI 与 Bean 生命周期 |
| 测试 | go test | JUnit 5 + Mockito + Testcontainers | 概念一致，语法迁移快 |
| 领域短板 | 无 | Java 实战 + 车联网领域栈（MQTT/Kafka/Flink/TDengine） | 用本仓库示例项目补 |

**策略**：面试中主打“中间件原理扎实 + 并发模型相通 + 学习迁移快”，用 2~3 个自己动手做的 Java 项目（比如本仓库）证明 Java 实战能力。

## 6. 面试准备

### 6.1 八个高频考点

1. **MQTT 协议细节**：QoS 0/1/2、会话保持（Clean Session）、遗嘱消息、Topic 通配符、心跳。
2. **Kafka 可靠性**：如何保证不丢不重（ACK、幂等、offset 提交时机），如何排查消费积压。
3. **Flink 实时计算**：Watermark、事件时间 vs 处理时间、窗口聚合、Checkpoint 机制。
4. **时序数据库设计**：TDengine 表结构怎么设计（车表=子表）、保留策略、聚合查询下推。
5. **Spring Boot 核心**：自动装配原理、Bean 生命周期、@Transactional 失效场景、线程池。
6. **高并发接入设计**：假设 100 万辆车每 5 秒上报一次，网关/Kafka/存储各层怎么扛（算 QPS：100w/5s = 20 万 QPS）。
7. **分布式一致性**：远程控车指令“至少一次/恰好一次”语义、分布式事务（Seata/TCC）、幂等。
8. **线上问题排查**：CPU 飙高、Full GC、消息积压、接口超时的定位思路（jstack/arthas/链路追踪）。

### 6.2 4~8 周准备路径

| 周 | 主题 | 交付物 |
|----|------|--------|
| 第 1 周 | Java/Spring Boot 基础：IoC、AOP、MVC、JDBC/MyBatis、REST | 跑通本仓库项目，读懂每层代码 |
| 第 2 周 | Spring Cloud：Nacos、Gateway、Feign、Sentinel | 把本项目拆成 2~3 个服务注册到 Nacos |
| 第 3 周 | Kafka 精学：生产者/消费者/事务/幂等 + 本地起 Kafka 压测 | 写一个可靠消息 Demo |
| 第 4 周 | MQTT + EMQX：搭本地 EMQX，实现设备接入与消息路由 | 用 MQTTX 模拟车端接入本项目 |
| 第 5 周 | Flink 入门：DataStream + Flink SQL 窗口聚合 | 对 telemetry Topic 做 1 分钟聚合 Demo |
| 第 6 周 | TDengine/ClickHouse：建表、写入、聚合查询 | 把存储层切到真实时序库 |
| 第 7 周 | 高并发与调优：JVM、线程池、Kafka 参数、限流 | 压测报告 |
| 第 8 周 | 面试题刷题 + 项目复盘（数据链路八股全覆盖） | 30 个高频题答案 + 项目讲稿 |

### 6.3 投递前自查清单

- [ ] 能白板画出完整数据链路（车端 → MQTT → Kafka → Flink → 时序库 → API → 大屏）
- [ ] 能算出规模指标：QPS、存储量（如 100 万车 × 每天 86400 秒 ÷ 5 秒间隔 × 100 字节 ≈ 1.7 TB/天）
- [ ] 能讲清 Kafka 不丢不重、Flink 窗口、TDengine 建表三个方案
- [ ] 有 1 个拿得出手的 Java 实战项目（本仓库即可，建议再加深）
- [ ] 简历上 Go 经验按“并发/中间件/系统设计”迁移描述，突出可复用能力

---

# Part 2｜示例项目：connected-car-demo

## 一句话

一个 **Spring Boot 3 + Vue 3** 的单体示例项目，用内存组件模拟“车端 → MQTT → Kafka → Flink → 时序存储 → API → 大屏”全链路，**零中间件依赖、无网络可跑**，用于学习与面试演示。

## 快速开始

环境要求：JDK 17、Maven 3.8+（本机已验证 Maven 3.9.9）。

```bash
cd connected-car-demo
mvn spring-boot:run
```

打开浏览器：

- 大屏界面：<http://localhost:8080/>
- H2 数据库控制台：<http://localhost:8080/h2-console>（JDBC URL：`jdbc:h2:mem:connectedcar`，用户名 `sa`，密码留空）

> 项目内置了离线 Maven 仓库 `.m2-repo/`（已 gitignore），`.mvn/maven.config` 已把 `maven.repo.local` 指到该项目内仓库，所以断网也能构建。若你删除该目录，则回退到系统本地仓库（首次需要联网下载依赖）。

## 项目结构

```
connected-car-demo/
├── pom.xml
├── .mvn/maven.config            # 指向项目内离线仓库
└── src/main/
    ├── java/com/example/connectedcar/
    │   ├── ConnectedCarApplication.java
    │   ├── domain/               # 车辆、遥测、报警、大屏汇总（POJO）
    │   ├── simulator/            # 车端模拟器：每秒生成 6 台车遥测
    │   ├── gateway/              # MQTT 消息网关（模拟 EMQX 接入）
    │   ├── pipeline/             # KafkaBroker + StreamProcessor（模拟 Kafka+Flink）
    │   ├── storage/              # H2 + JdbcTemplate（模拟时序库）
    │   ├── service/              # 业务服务层
    │   └── web/                  # REST API 控制器
    └── resources/
        ├── application.yml
        ├── schema.sql / data.sql # 建表 + 种子车辆
        └── static/               # Vue 3 大屏（index.html + app.js + style.css）
```

## 示例项目与真实架构对照

| 示例代码 | 对应真实组件 | 说明 |
|----------|--------------|------|
| VehicleSimulator | T-Box + 4G/5G 上传 | 用 @Scheduled 每秒模拟 6 台车上报 |
| MessageGateway | EMQX + Netty 接入网关 | 收消息、拼 Topic、统计接入量 |
| KafkaBroker | Kafka | 用 BlockingQueue 模拟发布订阅 |
| StreamProcessor | Flink 流处理 | 过滤清洗 + 报警规则 + 落库 |
| telemetry 表 | TDengine/ClickHouse 时序表 | 按 (vin, ts) 建索引，模拟时序查询 |
| vehicle / alarm_event 表 | MySQL/PostgreSQL | 主数据与报警事件 |
| /api/** | Spring 微服务 | REST API |
| static/app.js | Vue 3 大屏 | 轮询拉取实时数据 |

## REST API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/vehicles | 车辆列表（含在线状态、最新速度/位置） |
| GET | /api/vehicles/{vin} | 单辆车详情 |
| GET | /api/vehicles/{vin}/telemetry?limit=100 | 车辆遥测历史（时序点查） |
| GET | /api/vehicles/{vin}/alarms?limit=50 | 车辆报警历史 |
| GET | /api/alarms/recent?limit=50 | 全平台最近报警 |
| GET | /api/dashboard/summary | 大屏汇总（在线数、数据点、报警数、平均车速） |
| GET | /api/dashboard/pipeline | 各链路环节累计条数 |

## Vue 学习要点（对应 app.js）

1. **组件化**：页面拆成 PipelineStrip、StatCard、VehicleTable、Sparkline、AlarmFeed 五个组件，每个组件只管一件事；
2. **响应式**：ref() 存数据，模板里自动更新；computed() 派生展示值（如最大车速、车牌映射）；
3. **生命周期**：onMounted 启动 2 秒轮询，onBeforeUnmount 清理定时器，避免内存泄漏；
4. **前后端对接**：fetch('/api/...') 调用 Spring REST API，Promise.all 并发拉取四路数据；
5. **组件通信**：父组件通过 props 传数据，子组件通过 emit('select', vin) 通知父组件选中了哪台车；
6. **SVG 可视化**：Sparkline 组件用 computed 把数据点映射成 SVG 路径，理解“数据 → 图形”的渲染思路后，换 ECharts 只是换层皮。

## 常见问题

- **想触发报警看效果**：改 application.yml 里的 app.alarm.speed-limit（如改为 60），或把模拟器速度随机游走上限调高（VehicleSimulator 中 clamp(..., 0, 180)），重启即可看到超速报警刷屏。
- **想接真实 Kafka/EMQX/TDengine**：把 KafkaBroker、MessageGateway、TelemetryRepository 三个类的实现替换为对应客户端（kafka-clients、Eclipse Paho、TDengine JDBC），接口签名不变，其他层无需改动。
- **中文乱码**：终端建议用 UTF-8（chcp 65001）；源码均为 UTF-8。

## 学习路线建议（结合 Part 1）

1. 先跑起来，对照“示例项目 ↔ 真实架构”表格读懂每一层；
2. 用 curl 或浏览器调一遍 REST API，理解前端数据从哪来；
3. 改代码：加一个新报警规则（如“连续 10 秒怠速”）→ 加深 Flink 规则引擎理解；
4. 把模拟器换成真实 MQTT 客户端（MQTTX）→ 理解接入层；
5. 把 H2 换成 TDengine → 理解时序库。

mvn spring-boot:run
(Get-NetTCPConnection -LocalPort 8080).OwningProcess
Stop-Process -Id 84500 -Force
