package com.example.observability.web;

import com.example.observability.client.PrometheusClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * "Observability at scale" — the video's closing section made concrete: log aggregation
 * (the app -> docker -> promtail -> Loki pipeline), trace sampling (see /sampling), and
 * autoscaling on business metrics. Also a quick live view of every target Prometheus is
 * scraping right now.
 */
@Controller
public class ScaleController {

    private final PrometheusClient prometheus;

    public ScaleController(PrometheusClient prometheus) {
        this.prometheus = prometheus;
    }

    @GetMapping("/scale")
    public String page(Model model) {
        int activeTargets = 0;
        int downTargets = 0;
        try {
            JsonNode targets = prometheus.targets();
            JsonNode list = targets.path("data").path("activeTargets");
            if (list.isArray()) {
                for (JsonNode t : list) {
                    if ("up".equals(t.path("health").asText())) {
                        activeTargets++;
                    } else {
                        downTargets++;
                    }
                }
            }
        } catch (RuntimeException ignored) {
        }
        model.addAttribute("activeTargets", activeTargets);
        model.addAttribute("downTargets", downTargets);
        return "scale";
    }
}