package com.example.clubmanagementsystem.config;

import com.example.clubmanagementsystem.interceptor.AdminInterceptor;
import com.example.clubmanagementsystem.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器：拦截除登录注册和公开浏览外的所有请求
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/club/list",
                        "/club/detail/**",
                        "/activity/list",
                        "/club-comment/list/**",
                        "/club-member/list/**"
                );

        // 管理员拦截器：拦截需要管理员权限的接口
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns(
                        "/club/add",
                        "/club/approve/**",
                        "/club/reject/**",
                        "/club/pending",
                        "/club-member/pending-all",
                        "/activity/approve/**",
                        "/activity/reject/**",
                        "/ai-report/generate",
                        "/ai-report/generate/stream",
                        "/statistics/**"
                );
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(120_000L);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
