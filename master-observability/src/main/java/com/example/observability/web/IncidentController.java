package com.example.observability.web;

import com.example.observability.client.JaegerClient;
import com.example.observability.client.LokiClient;
import com.example.observability.client.PrometheusClient;
import com.example.observability.domain.OrderLookup;
import com.example.observability.domain.ShopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The capstone drill. Generate a fake incident, then follow the procedure the videos teach:
 * start with a failing order -> open its trace -> read the error on the failing span ->
 * confirm against the metric -> commit to a root cause. If your hypothesis does not match
 * the error_code recorded with the order, you restart the drill (no restart-and-pray).
 */
@Controller
public class IncidentController {

    private static final Map<String, String> HYPOTHESES = Map.of(
            "gateway", "The payment gateway rejected or declined the payment",
            "timeout", "The payment gateway timed out",
            "fraud", "The fraud check flagged the order",
            "stock", "The product ran out of stock");

    private final ShopService shop;
    private final OrderLookup orders;
    private final JaegerClient jaeger;
    private final LokiClient loki;
    private final PrometheusClient prometheus;

    public IncidentController(ShopService shop, OrderLookup orders, JaegerClient jaeger,
                              LokiClient loki, PrometheusClient prometheus) {
        this.shop = shop;
        this.orders = orders;
        this.jaeger = jaeger;
        this.loki = loki;
        this.prometheus = prometheus;
    }

    @GetMapping("/incident")
    public String page(Model model) {
        List<OrderLookup.OrderRow> failures = orders.recentFailures(5);
        model.addAttribute("failures", failures);
        model.addAttribute("failureIds",
                failures.stream().map(f -> "#" + f.id()).collect(java.util.stream.Collectors.joining(", ")));
        return "incident";
    }

    @PostMapping("/incident/generate")
    public String generate(Model model) {
        List<OrderLookup.OrderRow> produced = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            boolean fail = i % 2 == 0;
            long slowMs = (i % 5 == 0) ? 450 : 0;
            ShopService.CheckoutResult result =
                    shop.checkout(1 + (i % 12), 1 + (i % 12), 1 + (i % 3), "incident", fail, slowMs);
            if (!result.ok()) {
                OrderLookup.OrderRow row = orders.byId(result.orderId());
                if (row != null) {
                    produced.add(row);
                }
            }
        }
        model.addAttribute("produced", produced.stream().limit(10).toList());
        return "partials/incident :: generated";
    }

    @GetMapping("/incident/solve")
    public String solve(@RequestParam long orderId, Model model) {
        OrderLookup.OrderRow order = orders.byId(orderId);
        if (order == null) {
            model.addAttribute("error", "Order " + orderId + " not found.");
            return "partials/incident :: solve";
        }
        JaegerClient.TraceInfo trace = jaeger.byId(order.traceId());
        LokiClient.QueryResult logs = loki.query("{service=\"app\"} |= \"" + order.traceId() + "\"", 10);
        double failureRate = prometheus.instantValue(
                "sum(rate(checkout_failure_total{application=\"observability-shop\"}[2m]))");

        model.addAttribute("order", order);
        model.addAttribute("trace", trace);
        model.addAttribute("logs", logs);
        model.addAttribute("failureRate", failureRate);
        model.addAttribute("bones", HYPOTHESES);
        model.addAttribute("jaegerUrl", "http://localhost:16686/trace/" + order.traceId());
        model.addAttribute("answerGiven", null);
        return "partials/incident :: solve";
    }

    @PostMapping("/incident/answer")
    public String answer(@RequestParam long orderId,
                         @RequestParam String pick,
                         Model model) {
        OrderLookup.OrderRow order = orders.byId(orderId);
        JaegerClient.TraceInfo trace = jaeger.byId(order.traceId());
        LokiClient.QueryResult logs = loki.query("{service=\"app\"} |= \"" + order.traceId() + "\"", 10);

        boolean correct = switch (order.errorCode()) {
            case "card_declined", "insufficient_funds" -> "gateway".equals(pick);
            case "timeout" -> "timeout".equals(pick);
            case "fraud_check" -> "fraud".equals(pick);
            default -> false;
        };

        String spanEvidence = trace == null ? "trace not found in Jaeger"
                : trace.spans().stream()
                        .flatMap(s -> s.logs().stream())
                        .map(JaegerClient.LogEvent::exceptionMessage)
                        .filter(m -> m != null && !m.isEmpty())
                        .findFirst().orElse("(no span error logged)");

        model.addAttribute("order", order);
        model.addAttribute("trace", trace);
        model.addAttribute("logs", logs);
        model.addAttribute("bones", HYPOTHESES);
        model.addAttribute("jaegerUrl", "http://localhost:16686/trace/" + order.traceId());
        model.addAttribute("answerGiven", new Answer(pick, HYPOTHESES.getOrDefault(pick, "?"), correct,
                order.errorCode(), spanEvidence));
        return "partials/incident :: solve";
    }

    public record Answer(String pick, String hypothesis, boolean correct,
                         String actualErrorCode, String spanEvidence) {}
}