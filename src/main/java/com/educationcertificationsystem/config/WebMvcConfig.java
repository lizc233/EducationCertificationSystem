package com.educationcertificationsystem.config;

import com.educationcertificationsystem.auth.AuditInterceptor;
import com.educationcertificationsystem.auth.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    private final AuditInterceptor auditInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor, AuditInterceptor auditInterceptor) {
        this.authInterceptor = authInterceptor;
        this.auditInterceptor = auditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/**", "/notice/**")
            .excludePathPatterns("/api/auth/login", "/api/auth/ping");
        registry.addInterceptor(auditInterceptor)
            .addPathPatterns("/api/**", "/notice/**");
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
