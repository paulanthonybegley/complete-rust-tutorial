package com.example.observability.web;

import com.example.observability.domain.AlertStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The receiving end of Alertmanager's webhook receiver. Alertmanager POSTs fired alerts
 * here; the app stores them in PostgreSQL so the /alerts lab can show the full loop.
 * (See docker/alertmanager/alertmanager.yml -> receiver shop-webhook.)
 */
@RestController
public class AlertWebhookController {

    private final AlertStore store;

    public AlertWebhookController(AlertStore store) {
        this.store = store;
    }

    @PostMapping("/internal/alert")
    public String receive(@RequestBody JsonNode body) {
        String status = body.path("status").asText("firing");
        JsonNode alerts = body.path("alerts");
        if (alerts.isArray()) {
            for (JsonNode alert : alerts) {
                store.insert(
                        alert.path("fingerprint").asText(""),
                        alert.path("labels").path("alertname").asText("unknown"),
                        alert.path("status").asText(status),
                        alert.path("startsAt").asText(""),
                        alert.path("labels").toString(),
                        alert.path("annotations").toString());
            }
        }
        return "ok";
    }
}