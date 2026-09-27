package com.example.observability.web;

import com.example.observability.client.PrometheusClient;
import com.example.observability.domain.AlertStore;
import com.example.observability.domain.ShopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Alerting + the alert-fatigue warning. The rules in docker/prometheus/alert-rules.yml only
 * fire when checkouts actually fail; this page shows the rules' live state, the alerts that
 * went "firing", and the webhook notifications Alertmanager delivered into alert_events.
 */
@Controller
public class AlertsController {

    private final PrometheusClient prometheus;
    private final AlertStore alertStore;
    private final ShopService shop;

    public AlertsController(PrometheusClient prometheus, AlertStore alertStore, ShopService shop) {
        this.prometheus = prometheus;
        this.alertStore = alertStore;
        this.shop = shop;
    }

    @GetMapping("/alerts")
    public String page(Model model) {
        model.addAttribute("view", prometheus.view());
        model.addAttribute("events", alertStore.recent(12));
        return "alerts";
    }

    @GetMapping("/alerts/fragment")
    public String fragment(Model model) {
        model.addAttribute("view", prometheus.view());
        model.addAttribute("events", alertStore.recent(12));
        return "partials/alerts :: overview";
    }

    @PostMapping("/alerts/stress")
    public String stress(@RequestParam(defaultValue = "12") int n, Model model) {
        int count = Math.min(n, 30);
        long failures = 0;
        for (int i = 0; i < count; i++) {
            boolean fail = i % 2 == 0;
            ShopService.CheckoutResult result =
                    shop.checkout(1 + (i % 12), 1 + (i % 12), 1, "alert-lab", fail, 0);
            if (!result.ok()) {
                failures++;
            }
        }
        model.addAttribute("stressRuns", count);
        model.addAttribute("stressFailures", failures);
        model.addAttribute("stressNote",
                "The Prometheus alert rules evaluate every 5s; a failure ratio above 10% for 30s "
                        + "fires FailedCheckoutRateHigh, Alertmanager sends it to the app's webhook, "
                        + "and it lands in the table below within about a minute.");
        return "partials/alerts :: stress";
    }
}