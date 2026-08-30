package com.example.connectedcar.web;

import com.example.connectedcar.domain.User;
import com.example.connectedcar.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：除认证接口与静态资源外，所有 /api 请求必须带有效 token。
 *
 * token 通过请求头 Authorization: Bearer <token> 传递；拦截器校验 Redis 中的会话，
 * 命中则刷新 TTL（滑动过期），未命中返回 401。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/")) {
            return true;
        }
        if (!uri.startsWith("/api/")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        User user = authService.verify(token);
        if (user == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"未登录或会话已过期\"}");
            } catch (Exception ignored) {
            }
            return false;
        }
        authService.renew(token);
        request.setAttribute("currentUser", user);
        return true;
    }
}
