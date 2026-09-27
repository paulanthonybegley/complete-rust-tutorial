package com.example.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "obs")
public record ObsProps(
        String prometheusUrl,
        String lokiUrl,
        String jaegerUrl,
        String alertmanagerUrl,
        Sampler sampler) {

    public record Sampler(long slowMs, double keepRatio) {}
}