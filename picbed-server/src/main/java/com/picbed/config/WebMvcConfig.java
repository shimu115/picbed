package com.picbed.config;

import com.picbed.interceptor.SessionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private SessionInterceptor sessionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/api/admin/**", "/api/upload/**", "/api/auth/session", "/api/account/**")
                .excludePathPatterns("/api/public/**", "/api/setup/**", "/api/auth/login", "/api/auth/logout");
    }
}
