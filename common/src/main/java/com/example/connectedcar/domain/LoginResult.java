package com.example.connectedcar.domain;

/**
 * 登录校验结果（ingest-service → query-service 跨服务传递）。
 *
 * <p>用强类型替代 {@code Map<String, Object>}，让 OpenFeign 能直接反序列化出
 * {@link User}，调用方无需再做强制类型转换。
 */
public class LoginResult {

    private boolean valid;
    private User user;

    public LoginResult() {
    }

    public LoginResult(boolean valid, User user) {
        this.valid = valid;
        this.user = user;
    }

    /** 校验通过。 */
    public static LoginResult ok(User user) {
        return new LoginResult(true, user);
    }

    /** 校验失败（用户名不存在或密码错误）。 */
    public static LoginResult fail() {
        return new LoginResult(false, null);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
