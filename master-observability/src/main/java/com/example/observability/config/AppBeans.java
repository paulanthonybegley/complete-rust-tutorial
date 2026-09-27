package com.example.observability.config;

import com.example.observability.obs.TraceDecider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppBeans {

    @Bean
    TraceDecider traceDecider(ObsProps props) {
        return new TraceDecider(props.sampler().slowMs(), props.sampler().keepRatio());
    }
}