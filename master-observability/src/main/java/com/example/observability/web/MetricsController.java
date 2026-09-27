package com.example.observability.web;

import com.example.observability.client.PrometheusClient;
import com.example.observability.domain.ShopService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class MetricsController {

    private final ShopService shop;
    private final PrometheusClient prometheus;

    public MetricsController(ShopService shop, PrometheusClient prometheus) {
        this.shop = shop;
        this.prometheus = prometheus;
    }

    @GetMapping("/metrics")
    public String page(Model model) {
        model.addAttribute("stats", stats());
        model.addAttribute("generated", null);
        return "metrics";
    }

    @PostMapping("/metrics/traffic")
    public String traffic(@RequestParam(defaultValue = "mixed") String mode,
                          @RequestParam(defaultValue = "10") int n,
                          Model model) {
        int effective = "slow".equals(mode) ? Math.min(n, 15) : Math.min(n, 60);
        List<ShopService.CheckoutResult> results = new ArrayList<>();
        for (int i = 0; i < effective; i++) {
            boolean forceFail = "fail".equals(mode);
            long slowMs = "slow".equals(mode) ? 450 : 0;
            results.add(shop.checkout(1 + (i % 12), 1 + (i % 12), 1 + (i % 3), "lab", forceFail, slowMs));
        }
        long ok = results.stream().filter(ShopService.CheckoutResult::ok).count();
        long failed = effective - ok;

        model.addAttribute("mode", mode);
        model.addAttribute("generated", new Generated(mode, effective, ok, failed, results.stream().limit(20).toList()));
        model.addAttribute("promUrl", "http://localhost:9090");
        model.addAttribute("grafanaUrl", "http://localhost:3000/d/obs-shop-overview");
        model.addAttribute("jaegerUrl", "http://localhost:16686/search");
        return "partials/metrics :: generated";
    }

    @GetMapping("/metrics/stats")
    public String stats(Model model) {
        model.addAttribute("stats", stats());
        return "partials/metrics :: stats";
    }

    private List<MetricStat> stats() {
        List<MetricStat> out = new ArrayList<>();

        JsonNode byOutcome = prometheus.instant("sum by (outcome) (rate(checkout_total{application=\"observability-shop\"}[1m]))");
        out.addAll(series("checkout throughput", byOutcome, "ops", value -> value + " ops/s"));
        JsonNode byReason = prometheus.instant("sum by (reason) (rate(checkout_failure_total{application=\"observability-shop\"}[1m]))");
        out.addAll(series("failure rate by reason", byReason, "ops", value -> value + " fail/s"));

        double p95v = p95();
        double p50v = p50();
        out.add(new MetricStat("checkout latency p95", p95v, "s", fmt(p95v, "s")));
        out.add(new MetricStat("checkout latency p50", p50v, "s", fmt(p50v, "s")));

        JsonNode http = prometheus.instant("sum by (status) (rate(http_server_requests_seconds_count{application=\"observability-shop\"}[1m]))");
        out.addAll(series("http req/s by status", http, "req/s", v -> v + " req/s"));

        double firing = prometheus.instantValue("count(ALERTS{alertstate=\"firing\"})");
        out.add(new MetricStat("alerts firing", firing, "n",
                Double.isNaN(firing) ? "0" : String.valueOf((int) firing)));

        JsonNode stock = prometheus.instant("sum by (product) (stock_level{application=\"observability-shop\"})");
        out.addAll(series("stock levels", stock, "units", v -> String.valueOf(v.intValue()) + " units"));

        return out;
    }

    private static String fmt(double value, String unit) {
        return Double.isNaN(value) ? "—" : value + " " + unit;
    }

    private double p95() {
        return prometheus.instantValue("histogram_quantile(0.95, sum by (le) (rate(checkout_duration_seconds_bucket{application=\"observability-shop\"}[1m])))");
    }

    private double p50() {
        return prometheus.instantValue("histogram_quantile(0.50, sum by (le) (rate(checkout_duration_seconds_bucket{application=\"observability-shop\"}[1m])))");
    }

    private static List<MetricStat> series(String label, JsonNode result, String unit,
                                           java.util.function.Function<Double, String> formatter) {
        List<MetricStat> out = new ArrayList<>();
        JsonNode arr = result.path("data").path("result");
        if (arr.isArray()) {
            for (JsonNode series : arr) {
                JsonNode metric = series.path("metric");
                List<String> parts = new ArrayList<>();
                metric.fields().forEachRemaining(e -> parts.add(e.getKey() + "=" + e.getValue().asText()));
                String valueLabel = parts.isEmpty() ? label : String.join(" ", parts);
                double v = series.path("value").get(1).asDouble(0);
                out.add(new MetricStat(valueLabel, v, unit, formatter.apply(v)));
            }
        }
        out.sort(Comparator.comparing(MetricStat::label));
        return out;
    }

    public record Generated(String mode, int n, long ok, long failed, List<ShopService.CheckoutResult> results) {}

    public record MetricStat(String label, double value, String unit, String display) {}
}