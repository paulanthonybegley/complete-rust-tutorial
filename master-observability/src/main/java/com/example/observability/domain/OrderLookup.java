package com.example.observability.domain;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * The correlate and incident labs both start from real failed orders — the ones the
 * application's own traffic produced (not the seeded 'legacy-' history rows, whose fake
 * trace ids can never be resolved in Jaeger).
 */
@Service
public class OrderLookup {

    private final JdbcTemplate jdbc;

    public OrderLookup(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OrderRow> recentFailures(int limit) {
        return jdbc.query(
                "SELECT id, customer_id, product_id, status, error_code, trace_id, created_at "
                        + "FROM orders "
                        + "WHERE status = 'failed' AND trace_id IS NOT NULL AND trace_id <> '' "
                        + "  AND trace_id NOT LIKE 'legacy-%' "
                        + "ORDER BY created_at DESC LIMIT ?",
                (rs, i) -> new OrderRow(
                        rs.getLong("id"),
                        rs.getLong("customer_id"),
                        rs.getLong("product_id"),
                        rs.getString("status"),
                        rs.getString("error_code"),
                        rs.getString("trace_id"),
                        rs.getTimestamp("created_at").toInstant()),
                limit);
    }

    public OrderRow byId(long orderId) {
        List<OrderRow> rows = jdbc.query(
                "SELECT id, customer_id, product_id, status, error_code, trace_id, created_at "
                        + "FROM orders WHERE id = ?",
                (rs, i) -> new OrderRow(
                        rs.getLong("id"),
                        rs.getLong("customer_id"),
                        rs.getLong("product_id"),
                        rs.getString("status"),
                        rs.getString("error_code"),
                        rs.getString("trace_id"),
                        rs.getTimestamp("created_at").toInstant()),
                orderId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public record OrderRow(long id, long customerId, long productId, String status,
                           String errorCode, String traceId, Instant createdAt) {}
}