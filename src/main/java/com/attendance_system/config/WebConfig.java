package com.attendance_system.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final DashboardAuthInterceptor dashboardAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dashboardAuthInterceptor)
                .addPathPatterns("/api/dashboard/**", "/api/attendance/**", "/api/attendance-session/**", "/dashboard.html");
    }
}
