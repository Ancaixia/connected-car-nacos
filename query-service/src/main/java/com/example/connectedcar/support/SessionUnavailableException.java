package com.example.connectedcar.support;

/**
 * 会话存储（Redis）不可用，与"未登录/会话已过期"是两种不同语义。
 *
 * <p>前者属于基础设施故障，应返回 503 并明确提示，
 * 不能退化成 401，否则用户会误以为是自己的会话过期了。
 */
public class SessionUnavailableException extends RuntimeException {

    public SessionUnavailableException(String message) {
        super(message);
    }

    public SessionUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
