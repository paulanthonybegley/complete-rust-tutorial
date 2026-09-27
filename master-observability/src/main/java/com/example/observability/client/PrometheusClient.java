package com.example.observability.client;

import com.example.observability.config.ObsProps;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Prometheus query API. The /metrics, /alerts and /scale labs use it to show the stack's
 * actual answer to "how is the shop doing?".
 */
@Component
public class PrometheusClient {

    private final String base;
    private final HttpJson http;

    public PrometheusClient(ObsProps props, HttpJson http) {
        this.base = props.prometheusUrl();
        this.http = http;
    }

    public JsonNode instant(String query) {
        return http.getJson(base + "/api/v1/query?query=" + HttpJson.enc(query));
    }

    /** Double value of the first instant-query result, or NaN when empty/scalar. */
    public double instantValue(String query) {
        JsonNode node = safeInstant(query);
        if (node == null) {
            return Double.NaN;
        }
        JsonNode result = node.path("data").path("result");
        if (result.isArray() && !result.isEmpty()) {
            JsonNode value = result.get(0).path("value");
            if (value.isArray() && value.size() == 2) {
                return value.get(1).asDouble(Double.NaN);
            }
        }
        return Double.NaN;
    }

    public JsonNode rules() {
        return http.getJson(base + "/api/v1/rules");
    }

    public JsonNode alerts() {
        return http.getJson(base + "/api/v1/alerts");
    }

    public JsonNode targets() {
        return http.getJson(base + "/api/v1/targets?state=active");
    }

    private JsonNode safeInstant(String query) {
        try {
            return instant(query);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Flat view of every rule's state plus the currently-active alerts. */
    public record RulesView(List<RuleView> rules, List<AlertView> activeAlerts, boolean reachable) {}

    public record RuleView(String name, String state, String group, long activeAlertCount) {}

    public record AlertView(String name, String state, String severity, String labelsSummary) {}

    public RulesView view() {
        JsonNode response;
        try {
            response = rules();
        } catch (RuntimeException e) {
            return new RulesView(List.of(), List.of(), false);
        }
        List<RuleView> rules = new ArrayList<>();
        JsonNode groups = response.path("data").path("groups");
        if (groups.isArray()) {
            for (JsonNode group : groups) {
                String groupName = group.path("name").asText();
                JsonNode groupRules = group.path("rules");
                for (JsonNode rule : groupRules) {
                    rules.add(new RuleView(
                            rule.path("name").asText(),
                            rule.path("state").asText(groupName.equals("shop.business") ? "inactive" : "inactive"),
                            groupName,
                            rule.path("alerts").size()));
                }
            }
        }
        List<AlertView> active = new ArrayList<>();
        JsonNode alertResponse = alerts();
        JsonNode alertData = alertResponse.path("data").path("alerts");
        if (alertData.isArray()) {
            for (JsonNode alert : alertData) {
                if (!"firing".equals(alert.path("state").asText())) {
                    continue;
                }
                active.add(new AlertView(
                        alert.path("labels").path("alertname").asText(),
                        alert.path("state").asText(),
                        alert.path("labels").path("severity").asText("none"),
                        conciseLabels(alert.path("labels"))));
            }
        }
        return new RulesView(rules, active, true);
    }

    private static String conciseLabels(JsonNode labels) {
        List<String> parts = new ArrayList<>();
        labels.fields().forEachRemaining(e -> parts.add(e.getKey() + "=" + e.getValue().asText()));
        return String.join(", ", parts);
    }
}