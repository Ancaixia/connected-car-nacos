package com.example.connectedcar.pipeline;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存版 Kafka 模拟器。
 *
 * 真实场景：Kafka 集群按 topic 分区存储消息，消费组提交 offset。
 * 本示例用 BlockingQueue 模拟 topic，足够演示发布-订阅语义。
 */
@Component
public class KafkaBroker {

    private final Map<String, BlockingQueue<Object>> topics = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> publishedCount = new ConcurrentHashMap<>();

    /** 发布消息到指定 topic（topicKey 为固定字符串，如 "telemetry"）。 */
    public void publish(String topicKey, Object message) {
        topics.computeIfAbsent(topicKey, k -> new LinkedBlockingQueue<>(100_000))
                .offer(message);
        publishedCount.computeIfAbsent(topicKey, k -> new AtomicLong()).incrementAndGet();
    }

    /** 阻塞订阅：返回一条消息；topicKey 不存在时返回 null。 */
    public Object poll(String topicKey) {
        BlockingQueue<Object> queue = topics.get(topicKey);
        if (queue == null) {
            return null;
        }
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /** 某 topic 累计发布条数（大屏 Kafka 层指标）。 */
    public long getPublishedCount(String topicKey) {
        AtomicLong count = publishedCount.get(topicKey);
        return count == null ? 0 : count.get();
    }
}
