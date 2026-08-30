package com.example.connectedcar.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 内部接口鉴权：校验调用方是否持有约定的共享密钥。
 *
 * <p>仅对 {@code /api/internal/**} 生效。未配置 {@code app.internal.token} 时自动放行，
 * 保持与改造前一致；配置后，缺少或不匹配的请求直接 401，
 * 避免内网里任何人都能直接拉走全部车辆数据。
 */
@Component
public class InternalTokenInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(InternalTokenInterceptor.class);

    private static final String HEADER = "X-Internal-Token";
    private static final String PREFIX = "/api/internal/";

    private final byte[] expected;

    public InternalTokenInterceptor(@Value("${app.internal.token:}") String token) {
        String value = token == null ? "" : token.trim();
        this.expected = value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (!uri.startsWith(PREFIX)) {
            return true;
        }
        if (expected.length == 0) {
            return true;
        }

        String actual = request.getHeader(HEADER);
        byte[] presented = actual == null
                ? new byte[0]
                : actual.getBytes(StandardCharsets.UTF_8);

        if (MessageDigest.isEqual(expected, presented)) {
            return true;
        }

        log.warn("拒绝未授权的内部调用 uri={} remote={}", uri, request.getRemoteAddr());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"未授权的内部调用\"}");
        return false;
    }
}
