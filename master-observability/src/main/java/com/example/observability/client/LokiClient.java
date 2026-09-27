package com.example.observability.client;

import com.example.observability.config.ObsProps;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Loki query API. The /logs lab runs LogQL and renders what Loki actually stored — the
 * difference between "the app logged JSON" and "Loki can be queried".
 */
@Component
public class LokiClient {

    private final String base;
    private final HttpJson http;

    public LokiClient(ObsProps props, HttpJson http) {
        this.base = props.lokiUrl();
        this.http = http;
    }

    public QueryResult query(String logQl, long limit) {
        String url = base + "/loki/api/v1/query_range"
                + "?query=" + HttpJson.enc(logQl)
                + "&limit=" + limit
                + "&direction=backward";
        try {
            JsonNode response = http.getJson(url);
            return parse(response, logQl);
        } catch (RuntimeException e) {
            return new QueryResult(logQl, false, e.getMessage(), List.of());
        }
    }

    private static QueryResult parse(JsonNode response, String logQl) {
        List<LogLine> lines = new ArrayList<>();
        JsonNode data = response.path("data");
        if ("streams".equals(response.path("data").path("resultType").asText())) {
            JsonNode streams = data.path("result");
            if (streams.isArray()) {
                for (JsonNode stream : streams) {
                    String labels = stream.path("stream").toString();
                    JsonNode values = stream.path("values");
                    for (JsonNode value : values) {
                        long ns = value.get(0).asLong(0);
                        String line = value.get(1).asText();
                        lines.add(new LogLine(ns, line, labels));
                    }
                }
            }
        }
        lines.sort((a, b) -> Long.compare(b.ns(), a.ns()));
        return new QueryResult(logQl, true, null, lines);
    }

    public record QueryResult(String logQl, boolean ok, String error, List<LogLine> lines) {}

    public record LogLine(long ns, String text, String streamLabels) {
        public String isoTime() {
            return Instant.ofEpochMilli(ns / 1_000_000L).toString();
        }
    }
}