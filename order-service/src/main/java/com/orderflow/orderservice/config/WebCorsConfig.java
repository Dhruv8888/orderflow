package com.orderflow.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Phase 8: allows the Angular dashboard (running on localhost:4200 during development)
 * to call this service's REST endpoints directly from the browser. Without this, the
 * browser's same-origin policy blocks every request from the dashboard with a CORS error
 * before it even reaches this controller â€” this is a browser-side restriction, not
 * something Postman/curl ever hit, which is why it didn't come up in Phases 1-7.
 *
 * Scoped to /orders/** only (not a blanket allow-all) and to the known dev origin only.
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/orders/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}