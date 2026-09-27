package com.example.observability.config;

import com.example.observability.web.ObsInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ObsInterceptor obsInterceptor;

    public WebConfig(ObsInterceptor obsInterceptor) {
        this.obsInterceptor = obsInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(obsInterceptor);
    }
}