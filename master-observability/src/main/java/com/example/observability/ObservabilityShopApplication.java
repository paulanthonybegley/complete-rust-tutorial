package com.example.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ObservabilityShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservabilityShopApplication.class, args);
    }
}