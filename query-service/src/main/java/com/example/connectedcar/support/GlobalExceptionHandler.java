package com.example.connectedcar.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 全局异常处理。
 *
 * <p>{@link SessionUnavailableException} 既可能来自 Controller（登录时写会话失败），
 * 也可能来自 {@code AuthInterceptor.preHandle}（校验时读会话失败）——后者抛出的异常
 * 同样会交由 DispatcherServlet 的异常解析器处理，因此这里能统一兜住，返回 503。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SessionUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleSessionUnavailable(SessionUnavailableException e) {
        log.error("会话服务不可用: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("success", false, "message", "会话服务暂时不可用，请稍后重试"));
    }
}
