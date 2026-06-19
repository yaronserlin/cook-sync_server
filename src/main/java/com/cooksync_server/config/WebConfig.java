package com.cooksync_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // החל על כל הנתיבים ב-API
                .allowedOrigins("*") // בסביבת פרודקשן מומלץ להגביל לדומיינים ספציפיים
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // מתודות מורשות
                .allowedHeaders("*")
                .maxAge(3600);
    }
}