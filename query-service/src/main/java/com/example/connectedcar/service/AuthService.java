package com.example.connectedcar.service;

import com.example.connectedcar.client.IngestClient;
import com.example.connectedcar.domain.LoginResult;
import com.example.connectedcar.domain.User;
import com.example.connectedcar.storage.AuthCache;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 登录服务（query 网关侧）。
 *
 * 账号密码校验通过 OpenFeign 调 ingest-service（/api/internal/auth/validate），
 * 本服务只负责：校验通过后生成 token，并把 token -> 用户 写入 Redis 会话（分布式会话）。
 */
@Service
public class AuthService {

    private final IngestClient ingestClient;
    private final AuthCache authCache;

    public AuthService(IngestClient ingestClient, AuthCache authCache) {
        this.ingestClient = ingestClient;
        this.authCache = authCache;
    }

    /**
     * 登录：调采集服务校验账号密码，成功则生成 token 写入 Redis。
     *
     * @return 成功返回 token，失败返回 null
     */
    public String login(String username, String password) {
        Map<String, String> req = new HashMap<>();
        req.put("username", username);
        req.put("password", password);
        LoginResult result = ingestClient.validateLogin(req);
        if (result == null || !result.isValid() || result.getUser() == null) {
            return null;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        authCache.saveSession(token, result.getUser());
        return token;
    }

    public User verify(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return authCache.getSession(token);
    }

    public void logout(String token) {
        authCache.removeSession(token);
    }

    public void renew(String token) {
        authCache.renew(token);
    }
}
