package com.example.connectedcar.gateway;

/**
 * 模拟 MQTT 消息信封：topic + 负载。真实场景对应 EMQX 推送的 PUBLISH 报文。
 */
public class MqttMessage {

    private final String topic;
    private final Object payload;

    public MqttMessage(String topic, Object payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public Object getPayload() {
        return payload;
    }
}
