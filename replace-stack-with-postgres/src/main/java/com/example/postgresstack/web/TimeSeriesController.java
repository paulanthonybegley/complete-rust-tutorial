package com.example.postgresstack.web;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TimeSeriesController {

    private final NamedParameterJdbcTemplate jdbc;

    public TimeSeriesController(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Bucket(String month, long events, long devices) {}

    public record Partition(String name, String size, long rows) {}

    public record RangeResult(long count, String from, String to,
                              List<String> explainPlan, List<String> brinPlan) {}

    @GetMapping("/timeseries")
    public String page(Model model) {
        model.addAttribute("buckets", buckets());
        model.addAttribute("partitions", partitions());
        model.addAttribute("query", runQuery("2025-03-01", "2025-03-08"));
        return "timeseries";
    }

    @GetMapping("/timeseries/query")
    public String query(@RequestParam String from, @RequestParam String to, Model model) {
        model.addAttribute("query", runQuery(from, to));
        return "partials/timeseries :: range";
    }

    @PostMapping("/timeseries/seed")
    public String seed(@RequestParam(defaultValue = "50000") int rows, Model model) {
        jdbc.update("""
            INSERT INTO events (id, device, metric, value, occurred_at)
            SELECT (SELECT COALESCE(MAX(id), 0) FROM events) + s.i,
                   'device-' || (1 + floor(random() * 250))::int,
                   (ARRAY['cpu_usage', 'memory_usage', 'network_rx', 'disk_io'])[1 + floor(random() * 4)::int],
                   round((random() * 100)::numeric, 2)::double precision,
                   timestamp '2025-03-10 00:00:00' + ((s.i - 1) / 200) * interval '1 hour' + random() * interval '40 minutes'
            FROM generate_series(1, :rows) AS s(i)
            """, new MapSqlParameterSource("rows", rows));
        model.addAttribute("seeded", rows);
        model.addAttribute("buckets", buckets());
        model.addAttribute("partitions", partitions());
        return "partials/timeseries :: buckets";
    }

    private List<Bucket> buckets() {
        return jdbc.query("""
            SELECT to_char(date_trunc('month', occurred_at), 'YYYY-MM') AS month,
                   COUNT(*) AS cnt, COUNT(DISTINCT device) AS devices
            FROM events
            GROUP BY 1
            ORDER BY 1
            """, new MapSqlParameterSource(), (rs, i) -> new Bucket(
            rs.getString("month"), rs.getLong("cnt"), rs.getLong("devices")));
    }

    private List<Partition> partitions() {
        return jdbc.query("""
            SELECT c.relname AS name,
                   pg_size_pretty(pg_total_relation_size(c.oid)) AS size,
                   c.reltuples::bigint AS rows
            FROM pg_inherits i
            JOIN pg_class c ON c.oid = i.inhrelid
            WHERE i.inhparent = 'events'::regclass
            ORDER BY c.relname
            """, new MapSqlParameterSource(), (rs, i) -> new Partition(
            rs.getString("name"), rs.getString("size"), rs.getLong("rows")));
    }

    private RangeResult runQuery(String from, String to) {
        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("from", Timestamp.valueOf(from + " 00:00:00"))
            .addValue("to", Timestamp.valueOf(to + " 00:00:00"));

        long count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM events WHERE occurred_at >= :from AND occurred_at < :to
            """, p, Long.class);

        List<String> plan = explain("""
            SELECT COUNT(*) FROM events
            WHERE occurred_at >= '%s' AND occurred_at < '%s'
            """.formatted(from + " 00:00:00", to + " 00:00:00"));

        List<String> brinPlan = explainWithSeqScanOff("""
            SELECT COUNT(*) FROM events
            WHERE occurred_at >= '%s' AND occurred_at < '%s'
            """.formatted(from + " 00:00:00", to + " 00:00:00"));

        return new RangeResult(count, from, to, plan, brinPlan);
    }

    private List<String> explain(String sql) {
        return jdbc.getJdbcTemplate().queryForList(
            "EXPLAIN (ANALYZE, BUFFERS, COSTS OFF) " + sql, String.class);
    }

    private List<String> explainWithSeqScanOff(String sql) {
        return jdbc.getJdbcTemplate().execute((Connection con) -> {
            List<String> lines = new ArrayList<>();
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try (Statement st = con.createStatement()) {
                st.execute("SET LOCAL enable_seqscan = off");
                try (ResultSet rs = st.executeQuery("EXPLAIN (ANALYZE, BUFFERS, COSTS OFF) " + sql)) {
                    while (rs.next()) {
                        lines.add(rs.getString(1));
                    }
                }
            } finally {
                con.rollback();
                con.setAutoCommit(autoCommit);
            }
            return lines;
        });
    }
}