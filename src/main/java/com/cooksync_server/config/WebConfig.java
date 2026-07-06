package com.cooksync_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class responsible for defining global Cross-Origin Resource
 * Sharing (CORS) policies. Ensures the API can securely accept requests from
 * external client applications.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures CORS mappings to define which origins, HTTP methods, and
     * headers are permitted. Applies these security settings to all API
     * endpoints globally.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * // Automatically invoked by the Spring framework during context initialization.
     * // No manual invocation is required by the developer.
     * }</pre>
     *
     * @param registry The Spring registry used to define and register CORS
     * configurations.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
