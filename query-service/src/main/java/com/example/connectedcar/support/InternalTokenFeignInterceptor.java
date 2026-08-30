package com.example.connectedcar.support;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 为发往 ingest-service 的内部调用附加共享密钥请求头。
 *
 * <p>密钥取自 {@code app.internal.token}；为空时不添加请求头，
 * 行为与改造前完全一致，方便本地直接裸跑，不会打断现有部署。
 */
@Component
public class InternalTokenFeignInterceptor implements RequestInterceptor {

    static final String HEADER = "X-Internal-Token";

    private final String token;

    public InternalTokenFeignInterceptor(@Value("${app.internal.token:}") String token) {
        this.token = token == null ? "" : token.trim();
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!token.isEmpty()) {
            template.header(HEADER, token);
        }
    }
}
