package com.example.connectedcar.web;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注意：这里必须用 ObjectProvider 延迟获取 AuthInterceptor，不能直接构造器注入。
 *
 * AuthInterceptor → AuthService → IngestClient(Feign)，而 Feign 代理的创建依赖
 * WebMvcAutoConfiguration$EnableWebMvcConfiguration，后者又要收集所有 WebMvcConfigurer
 * （也就是本类）。若直接注入，会形成构造期循环依赖导致启动失败。
 *
 * 改用 ObjectProvider 后，本类构造不再触发该依赖链；真正取用发生在
 * addInterceptors() 回调时，此时 WebMvc 基础设施已就绪。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ObjectProvider<AuthInterceptor> authInterceptorProvider;

    public WebConfig(ObjectProvider<AuthInterceptor> authInterceptorProvider) {
        this.authInterceptorProvider = authInterceptorProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptorProvider.getObject())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/static/**"
                );
    }
}
