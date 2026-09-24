package com.example.postgresstack.web;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class AnalyticsController {

    private final NamedParameterJdbcTemplate jdbc;

    public AnalyticsController(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record DaySales(LocalDate date, BigDecimal revenue, long units, long orders) {}

    public record TopProduct(String product, BigDecimal revenue, long units) {}

    public record Dashboard(List<DaySales> days, List<TopProduct> top, BigDecimal totalRevenue,
                            long totalOrders, String refreshedAt, long mvRowCount) {}

    @GetMapping("/analytics")
    public String page(Model model) {
        model.addAttribute("dashboard", dashboard());
        return "analytics";
    }

    @GetMapping("/analytics/board")
    public String board(Model model) {
        model.addAttribute("dashboard", dashboard());
        return "partials/analytics :: dashboard";
    }

    @PostMapping("/analytics/refresh")
    public String refresh(Model model) {
        long start = System.currentTimeMillis();
        jdbc.getJdbcTemplate().execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_sales");
        jdbc.update("""
            INSERT INTO app_meta (key, value) VALUES ('mv_refreshed_at', now()::text)
            ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value
            """, new MapSqlParameterSource());
        long ms = System.currentTimeMillis() - start;
        model.addAttribute("flash", "REFRESH MATERIALIZED VIEW CONCURRENTLY finished in " + ms + " ms — readers stayed unlocked the whole time.");
        model.addAttribute("dashboard", dashboard());
        return "partials/analytics :: dashboard";
    }

    @GetMapping("/analytics/compare")
    public String compare(Model model) {
        List<String> raw = jdbc.getJdbcTemplate().queryForList("""
            EXPLAIN (ANALYZE, COSTS OFF)
            SELECT ordered_on, product, SUM(units) AS units, SUM(units * unit_price) AS revenue
            FROM orders GROUP BY ordered_on, product
            """, String.class);
        List<String> mv = jdbc.getJdbcTemplate().queryForList("""
            EXPLAIN (ANALYZE, COSTS OFF) SELECT * FROM mv_daily_sales
            """, String.class);

        model.addAttribute("rawTimeMs", executionTime(raw));
        model.addAttribute("mvTimeMs", executionTime(mv));
        model.addAttribute("mvRows", mvRows());
        return "partials/analytics :: compare";
    }

    private double executionTime(List<String> planLines) {
        for (String line : planLines) {
            if (line.contains("Execution Time")) {
                return Double.parseDouble(line.replaceAll(".*Execution Time:\\s*", "").replaceAll(" ms.*", ""));
            }
        }
        return 0;
    }

    private long mvRows() {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM mv_daily_sales", new MapSqlParameterSource(), Long.class);
        return c == null ? 0 : c;
    }

    private Dashboard dashboard() {
        List<DaySales> days = jdbc.query("""
            SELECT ordered_on, SUM(revenue) AS revenue, SUM(units) AS units, SUM(orders) AS orders
            FROM mv_daily_sales
            WHERE ordered_on >= CURRENT_DATE - 13
            GROUP BY ordered_on ORDER BY ordered_on
            """, new MapSqlParameterSource(), (rs, i) -> new DaySales(
            rs.getObject("ordered_on", LocalDate.class),
            rs.getBigDecimal("revenue"),
            rs.getLong("units"),
            rs.getLong("orders")));

        List<TopProduct> top = jdbc.query("""
            SELECT product, SUM(revenue) AS revenue, SUM(units) AS units
            FROM mv_daily_sales GROUP BY product ORDER BY revenue DESC LIMIT 8
            """, new MapSqlParameterSource(), (rs, i) -> new TopProduct(
            rs.getString("product"), rs.getBigDecimal("revenue"), rs.getLong("units")));

        BigDecimal totalRevenue = days.stream()
            .map(DaySales::revenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrders = days.stream().mapToLong(DaySales::orders).sum();

        String refreshedAt = jdbc.queryForObject(
            "SELECT value FROM app_meta WHERE key = 'mv_refreshed_at'",
            new MapSqlParameterSource(), String.class);

        int mvRows = (int) mvRows();
        return new Dashboard(days, top, totalRevenue, totalOrders,
            refreshedAt == null ? "-" : refreshedAt, mvRows);
    }
}