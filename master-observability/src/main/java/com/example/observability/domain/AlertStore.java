package com.example.observability.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Where Alertmanager's webhook notifications land. The /alerts lab reads this table so a
 * learner watches the full loop: Prometheus rule -> Alertmanager -> (webhook) -> this app
 * -> the UI. This is the "would someone have been paged?" evidence.
 */
@Service
public class AlertStore {

    private final JdbcTemplate jdbc;

    public AlertStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(String fingerprint, String alertName, String status,
                       String startsAt, String labels, String annotations) {
        jdbc.update("INSERT INTO alert_events (fingerprint, alert_name, status, starts_at, labels, annotations) "
                + "VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb)",
                fingerprint, alertName, status,
                startsAt == null || startsAt.isEmpty() ? null : Timestamp.from(Instant.parse(startsAt)),
                labels, annotations);
    }

    public List<AlertEvent> recent(int limit) {
        return jdbc.query(
                "SELECT id, alert_name, status, starts_at, labels, annotations, received_at "
                        + "FROM alert_events ORDER BY received_at DESC LIMIT ?",
                (rs, i) -> new AlertEvent(
                        rs.getLong("id"),
                        rs.getString("alert_name"),
                        rs.getString("status"),
                        rs.getTimestamp("starts_at") == null ? null : rs.getTimestamp("starts_at").toInstant(),
                        rs.getString("labels"),
                        rs.getString("annotations"),
                        rs.getTimestamp("received_at").toInstant()),
                limit);
    }

    public long count() {
        Long c = jdbc.queryForObject("SELECT count(*) FROM alert_events", Long.class);
        return c == null ? 0 : c;
    }

    public record AlertEvent(long id, String alertName, String status, Instant startsAt,
                             String labels, String annotations, Instant receivedAt) {}
}