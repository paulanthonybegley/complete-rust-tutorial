package com.example.observability.client;

import com.example.observability.config.ObsProps;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Jaeger trace query API. The /traces lab renders real span trees, the /correlate lab walks
 * from a failing order to its trace, and the /incident lab makes the learner read one.
 */
@Component
public class JaegerClient {

    private final String base;
    private final HttpJson http;

    public JaegerClient(ObsProps props, HttpJson http) {
        this.base = props.jaegerUrl();
        this.http = http;
    }

    public List<TraceInfo> recent(int limit) {
        String url = base + "/api/traces?service=" + HttpJson.enc("observability-shop")
                + "&limit=" + limit
                + "&lookback=" + Duration.ofHours(6).toMinutes() + "m";
        try {
            JsonNode response = http.getJson(url, Duration.ofSeconds(10));
            List<TraceInfo> traces = new ArrayList<>();
            for (JsonNode traceNode : response.path("data")) {
                traces.add(parse(traceNode));
            }
            traces.sort(Comparator.comparingLong(TraceInfo::startMicros).reversed());
            return traces;
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    public TraceInfo byId(String traceId) {
        String url = base + "/api/traces/" + HttpJson.enc(traceId);
        try {
            JsonNode response = http.getJson(url, Duration.ofSeconds(10));
            JsonNode data = response.path("data");
            if (data.isArray() && !data.isEmpty()) {
                return parse(data.get(0));
            }
            return null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public boolean reachable() {
        try {
            http.getJson(base + "/api/services", Duration.ofSeconds(3));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static TraceInfo parse(JsonNode traceNode) {
        String traceId = traceNode.path("traceID").asText();

        Map<String, JsonNode> rawSpans = new HashMap<>();
        long firstStart = Long.MAX_VALUE;
        for (JsonNode spanNode : traceNode.path("spans")) {
            rawSpans.put(spanNode.path("spanID").asText(), spanNode);
            long start = spanNode.path("startTime").asLong(0);
            if (start > 0 && start < firstStart) {
                firstStart = start;
            }
        }
        if (firstStart == Long.MAX_VALUE) {
            firstStart = 0;
        }

        List<SpanInfo> spans = new ArrayList<>();
        Set<String> services = new LinkedHashSet<>();
        boolean hasError = false;
        JsonNode processes = traceNode.path("processes");

        for (JsonNode spanNode : traceNode.path("spans")) {
            String spanId = spanNode.path("spanID").asText();
            String parentId = parentOf(spanNode);
            String op = spanNode.path("operationName").asText(traceId.length() > 8 ? traceId.substring(0, 8) : "?");
            long start = spanNode.path("startTime").asLong(0);
            long duration = spanNode.path("duration").asLong(0);

            String service = serviceOf(processes, spanNode);
            if (!service.isEmpty()) {
                services.add(service);
            }

            Map<String, String> tags = new LinkedHashMap<>();
            for (JsonNode tag : spanNode.path("tags")) {
                tags.put(tag.path("key").asText(), valueToString(tag.path("value")));
            }
            boolean error = "ERROR".equals(tags.get("otel.status_code"))
                    || "true".equals(tags.get("error"));
            List<LogEvent> logs = new ArrayList<>();
            for (JsonNode logNode : spanNode.path("logs")) {
                Map<String, String> fields = new LinkedHashMap<>();
                for (JsonNode field : logNode.path("fields")) {
                    fields.put(field.path("key").asText(), valueToString(field.path("value")));
                }
                logs.add(new LogEvent(logNode.path("timestamp").asLong(0), fields));
            }

            hasError = hasError || error;
            spans.add(new SpanInfo(spanId, parentId, op, start, duration, service, tags, logs, error));
        }

        // assign depth by walking parent chains (handles missing parents gracefully)
        Map<String, Integer> depth = new HashMap<>();
        for (SpanInfo span : spans) {
            int d = 0;
            String cursor = span.parentId();
            Set<String> seen = new HashSet<>();
            while (!cursor.isEmpty() && rawSpans.containsKey(cursor) && seen.add(cursor)) {
                d++;
                cursor = parentOf(rawSpans.get(cursor));
            }
            depth.put(span.spanId(), d);
        }

        long maxEnd = 0;
        for (SpanInfo span : spans) {
            long end = span.startMicros() + span.durationMicros();
            if (end > maxEnd) {
                maxEnd = end;
            }
        }

        return new TraceInfo(traceId, firstStart, firstStart > 0 ? maxEnd - firstStart : maxEnd,
                spans, depth, hasError, List.copyOf(services));
    }

    private static String parentOf(JsonNode spanNode) {
        for (JsonNode ref : spanNode.path("references")) {
            if ("CHILD_OF".equals(ref.path("refType").asText())) {
                return ref.path("spanID").asText();
            }
        }
        return "";
    }

    private static String serviceOf(JsonNode processes, JsonNode spanNode) {
        String processId = spanNode.path("processID").asText("");
        if (processId.isEmpty() || processes == null) {
            return "";
        }
        return processes.path(processId).path("serviceName").asText("");
    }

    private static String valueToString(JsonNode value) {
        if (value == null || value.isNull()) {
            return "";
        }
        if (value.isTextual()) {
            return value.asText();
        }
        if (value.isBoolean()) {
            return String.valueOf(value.asBoolean());
        }
        if (value.isNumber()) {
            return value.asText();
        }
        return value.toString();
    }

    public record TraceInfo(String traceId, long startMicros, long durationMicros,
                            List<SpanInfo> spans, Map<String, Integer> depth,
                            boolean hasError, List<String> services) {
        public int errorSpanCount() {
            return (int) spans.stream().filter(SpanInfo::error).count();
        }
    }

    public record SpanInfo(String spanId, String parentId, String operation,
                           long startMicros, long durationMicros, String service,
                           Map<String, String> tags, List<LogEvent> logs, boolean error) {}

    public record LogEvent(long timestampMicros, Map<String, String> fields) {
        public String exceptionMessage() {
            return fields.getOrDefault("exception.message",
                    fields.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                            .reduce((a, b) -> a + ", " + b).orElse(""));
        }
    }

    public static List<SpanInfo> dfs(List<SpanInfo> spans) {
        List<SpanInfo> ordered = new ArrayList<>();
        ArrayDeque<SpanInfo> stack = new ArrayDeque<>();
        spans.stream().filter(s -> s.parentId().isEmpty()).forEach(stack::push);
        Set<String> visited = new HashSet<>();
        while (!stack.isEmpty()) {
            SpanInfo span = stack.pop();
            if (!visited.add(span.spanId())) {
                continue;
            }
            ordered.add(span);
            spans.stream()
                    .filter(c -> span.spanId().equals(c.parentId()))
                    .sorted(Comparator.comparingLong(SpanInfo::startMicros).reversed())
                    .forEach(stack::push);
        }
        return ordered;
    }
}