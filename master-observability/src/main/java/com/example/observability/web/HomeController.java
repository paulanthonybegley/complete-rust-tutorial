package com.example.observability.web;

import com.example.observability.client.JaegerClient;
import com.example.observability.client.LokiClient;
import com.example.observability.domain.ShopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final ShopService shop;
    private final JaegerClient jaeger;
    private final LokiClient loki;

    public HomeController(ShopService shop, JaegerClient jaeger, LokiClient loki) {
        this.shop = shop;
        this.jaeger = jaeger;
        this.loki = loki;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/keystone")
    public String keystone(Model model) {
        model.addAttribute("runningTrace", "");
        return "keystone";
    }

    @PostMapping("/keystone/run")
    public String run(@RequestParam(defaultValue = "success") String mode, Model model) {
        long customerId = switch (mode) {
            case "vip" -> 1;
            case "fail" -> 4;
            default -> 2;
        };
        long slowMs = "slow".equals(mode) ? 450 : 0;
        boolean forceFail = "fail".equals(mode);

        ShopService.CheckoutResult result = shop.checkout(customerId, 3, 1, "keystone", forceFail, slowMs);

        model.addAttribute("result", result);

        String traceId = result.traceId();
        model.addAttribute("jaegerUrl", "http://localhost:16686/trace/" + traceId);
        model.addAttribute("trace", traceId == null || traceId.isEmpty() ? null : jaeger.byId(traceId));
        model.addAttribute("logQuery", "{service=\"app\"} |= \"" + safe(traceId) + "\"");
        model.addAttribute("logs", traceId == null || traceId.isEmpty()
                ? null : loki.query("{service=\"app\"} |= \"" + traceId + "\"", 20));
        return "partials/keystone :: result";
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}