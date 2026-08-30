package com.example.connectedcar.service;

import com.example.connectedcar.domain.User;
import com.example.connectedcar.storage.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 账号校验服务（采集服务侧）。
 *
 * 负责：从 app_user 表查账号、校验密码（兼容明文与 bcrypt 哈希，首次登录自动升级）。
 * 不负责 token 与会话——那属于 query-service（网关/BFF）的职责。
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 校验用户名密码。
     *
     * @return 成功返回包含 valid=true 与 user 的 Map；失败 valid=false
     */
    public Map<String, Object> validateLogin(String username, String password) {
        UserRepository.UserRow row = userRepository.findByUsername(username).orElse(null);
        if (row == null || !matches(row.password, password)) {
            return Map.of("valid", false);
        }
        if (isPlain(row.password)) {
            userRepository.upgradePassword(username, passwordEncoder.encode(password));
        }
        User user = row.toUser();
        return Map.of("valid", true, "user", user);
    }

    private boolean isPlain(String stored) {
        return stored == null || !stored.startsWith("$2");
    }

    private boolean matches(String stored, String raw) {
        if (isPlain(stored)) {
            return stored != null && stored.equals(raw);
        }
        return passwordEncoder.matches(raw, stored);
    }
}
