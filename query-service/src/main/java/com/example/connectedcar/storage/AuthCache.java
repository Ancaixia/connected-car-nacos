package com.example.connectedcar.storage;

import com.example.connectedcar.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录态缓存（Redis）。
 *
 * 用户登录后服务端生成 token，把 token -> 用户信息 存入 Redis 并设 TTL，
 * 实现分布式会话——多实例部署时登录态共享，且重启不丢；过期由 Redis 自动回收。
 */
@Component
public class AuthCache {

    private static final Logger log = LoggerFactory.getLogger(AuthCache.class);

    private static final String SESSION_PREFIX = "session:";
    public static final long TTL_SECONDS = 30 * 60;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public AuthCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void saveSession(String token, User user) {
        try {
            String json = objectMapper.writeValueAsString(user);
            redis.opsForValue().set(SESSION_PREFIX + token, json, TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入 Redis 会话失败 token={}: {}", token, e.getMessage());
        }
    }

    public User getSession(String token) {
        try {
            String json = redis.opsForValue().get(SESSION_PREFIX + token);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, User.class);
        } catch (Exception e) {
            log.warn("读取 Redis 会话失败 token={}: {}", token, e.getMessage());
            return null;
        }
    }

    public void removeSession(String token) {
        redis.delete(SESSION_PREFIX + token);
    }

    public void renew(String token) {
        redis.expire(SESSION_PREFIX + token, TTL_SECONDS, TimeUnit.SECONDS);
    }
}
