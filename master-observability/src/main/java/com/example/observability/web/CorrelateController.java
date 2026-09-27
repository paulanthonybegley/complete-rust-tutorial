package com.example.observability.web;

import com.example.observability.client.JaegerClient;
import com.example.observability.client.LokiClient;
import com.example.observability.client.PrometheusClient;
import com.example.observability.domain.OrderLookup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * The debugging work-flow the second video preaches: start top-down. A failed order ->
 * its trace -> the error attached to a span -> the logs for that trace_id -> the metric
 * it moved. No tail -f, no restarts.
 */
@Controller
public class CorrelateController {

    private final OrderLookup orders;
    private final JaegerClient jaeger;
    private final LokiClient loki;
    private final PrometheusClient prometheus;

    public CorrelateController(OrderLookup orders, JaegerClient jaeger,
                               LokiClient loki, PrometheusClient prometheus) {
        this.orders = orders;
        this.jaeger = jaeger;
        this.loki = loki;
        this.prometheus = prometheus;
    }

    @GetMapping("/correlate")
    public String page(@RequestParam(required = false) Long orderId, Model model) {
        List<OrderLookup.OrderRow> failures = orders.recentFailures(8);
        model.addAttribute("failures", failures);
        model.addAttribute("selected", orderId == null ? (failures.isEmpty() ? null : failures.get(0)) : orders.byId(orderId));
        return "correlate";
    }

    @GetMapping("/correlate/evidence")
    public String evidence(@RequestParam long orderId, Model model) {
        OrderLookup.OrderRow order = orders.byId(orderId);
        if (order == null) {
            model.addAttribute("error", "No such order " + orderId);
            return "partials/correlate :: evidence";
        }
        JaegerClient.TraceInfo trace = order.traceId() == null || order.traceId().isEmpty()
                ? null : jaeger.byId(order.traceId());
        LokiClient.QueryResult logs = order.traceId() == null || order.traceId().isEmpty()
                ? null : loki.query("{service=\"app\"} |= \"" + order.traceId() + "\"", 20);
        double failureRate = prometheus.instantValue(
                "sum(rate(checkout_failure_total{application=\"observability-shop\"}[2m]))");
        String rootCause = rootCauseFrom(trace);

        model.addAttribute("order", order);
        model.addAttribute("trace", trace);
        model.addAttribute("logs", logs);
        model.addAttribute("failureRate", Double.isNaN(failureRate) ? -1 : failureRate);
        model.addAttribute("rootCause", rootCause);
        model.addAttribute("jaegerUrl", order.traceId() == null ? "" : "http://localhost:16686/trace/" + order.traceId());
        return "partials/correlate :: evidence";
    }

    private static String rootCauseFrom(JaegerClient.TraceInfo trace) {
        if (trace == null) {
            return null;
        }
        return trace.spans().stream()
                .flatMap(s -> s.logs().stream())
                .map(JaegerClient.LogEvent::exceptionMessage)
                .filter(msg -> msg != null && !msg.isEmpty())
                .findFirst()
                .orElse(null);
    }
}