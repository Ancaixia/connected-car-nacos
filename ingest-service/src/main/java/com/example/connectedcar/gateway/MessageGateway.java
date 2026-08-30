package com.example.connectedcar.gateway;

import com.example.connectedcar.domain.Telemetry;
import com.example.connectedcar.pipeline.KafkaBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息接入网关（模拟 MQTT Broker 侧接入）。
 *
 * 真实场景：EMQX 集群接收海量车端 MQTT 连接，网关订阅 topic 后写入 Kafka。
 * 本示例用内存对象直接转发，并统计接入条数供大屏展示。
 */
@Component
public class MessageGateway {

    private static final Logger log = LoggerFactory.getLogger(MessageGateway.class);

    private final KafkaBroker kafkaBroker;
    private final AtomicLong receivedCount = new AtomicLong();

    public MessageGateway(KafkaBroker kafkaBroker) {
        this.kafkaBroker = kafkaBroker;
    }

    /** 接收车端上报（模拟 MQTT 消息到达）。 */
    public void onVehicleTelemetry(Telemetry telemetry) {
        receivedCount.incrementAndGet();
        String topic = "vehicle/" + telemetry.getVin() + "/telemetry";
        kafkaBroker.publish("telemetry", new MqttMessage(topic, telemetry));
    }

    public long getReceivedCount() {
        return receivedCount.get();
    }
}
