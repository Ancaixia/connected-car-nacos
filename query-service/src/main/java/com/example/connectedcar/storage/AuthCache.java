package com.example.connectedcar.storage;

import com.example.connectedcar.domain.User;
import com.example.connectedcar.support.SessionUnavailableException;
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
            log.error("写入 Redis 会话失败 token={}: {}", token, e.getMessage());
            throw new SessionUnavailableException("写入登录会话失败", e);
        }
    }

    /**
     * 读取会话。
     *
     * @return 会话不存在或已过期返回 null（正常情况）；Redis 自身故障则抛异常
     */
    public User getSession(String token) {
        String json;
        try {
            json = redis.opsForValue().get(SESSION_PREFIX + token);
        } catch (Exception e) {
            log.error("读取 Redis 会话失败 token={}: {}", token, e.getMessage());
            throw new SessionUnavailableException("读取登录会话失败", e);
        }
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, User.class);
        } catch (Exception e) {
            log.error("解析 Redis 会话失败 token={}: {}", token, e.getMessage());
            throw new SessionUnavailableException("解析登录会话失败", e);
        }
    }

    public void removeSession(String token) {
        try {
            redis.delete(SESSION_PREFIX + token);
        } catch (Exception e) {
            log.error("删除 Redis 会话失败 token={}: {}", token, e.getMessage());
            throw new SessionUnavailableException("删除登录会话失败", e);
        }
    }

    /**
     * 滑动续期：失败只记日志，不抛异常。
     *
     * <p>续期属于尽力而为的操作，Redis 抖动不应让一次本可正常完成的请求失败——
     * 会话未过期时照常放行，等 Redis 恢复后再续即可。
     */
    public void renew(String token) {
        try {
            redis.expire(SESSION_PREFIX + token, TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("续期 Redis 会话失败 token={}: {}", token, e.getMessage());
        }
    }
}
