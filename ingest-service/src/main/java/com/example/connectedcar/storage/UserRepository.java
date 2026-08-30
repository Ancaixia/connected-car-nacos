package com.example.connectedcar.storage;

import com.example.connectedcar.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户账号仓储（从库查 app_user 表；真实场景对应 MySQL/PostgreSQL 的 user 表）。
 *
 * 登录校验下沉到采集服务：query-service 通过 OpenFeign 调本服务校验账号密码，
 * 再自行生成 token 与 Redis 会话，体现微服务间职责划分。
 */
@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    private static final RowMapper<UserRow> ROW_MAPPER = (rs, rowNum) -> {
        UserRow r = new UserRow();
        r.username = rs.getString("username");
        r.password = rs.getString("password");
        r.vin = rs.getString("vin");
        r.name = rs.getString("name");
        r.role = rs.getString("role");
        return r;
    };

    public Optional<UserRow> findByUsername(String username) {
        String sql = "SELECT username, password, vin, name,  role FROM app_user WHERE username = ?";
        return jdbc.query(sql, ROW_MAPPER, username).stream().findFirst();
    }

    public void upgradePassword(String username, String bcryptHash) {
        jdbc.update("UPDATE app_user SET password = ? WHERE username = ?", bcryptHash, username);
    }

    public static final class UserRow {
        public String username;
        public String password;
        public String vin;
        public String name;
        public String role;

        public User toUser() {
            return new User(username, vin, name, role);
        }
    }
}
