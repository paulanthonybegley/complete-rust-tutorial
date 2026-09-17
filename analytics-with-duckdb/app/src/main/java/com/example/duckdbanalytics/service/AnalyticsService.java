package com.example.duckdbanalytics.service;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.duckdbanalytics.model.CategoryRevenue;
import com.example.duckdbanalytics.model.DatabaseInfo;
import com.example.duckdbanalytics.model.ExplainResult;
import com.example.duckdbanalytics.model.LogLevelCount;
import com.example.duckdbanalytics.model.RevenueTrendRow;
import com.example.duckdbanalytics.model.StatusCount;
import com.example.duckdbanalytics.model.TopCustomer;

/**
 * Runs the course's analytical queries against the embedded DuckDB engine.
 *
 * Every method is deliberately plain SQL on the shared dataset — no ORM. That
 * is the teaching point: analytical SQL (GROUP BY, joins, PIVOT) is the tool,
 * and the engine is what makes it fast (lesson 4).
 */
@Service
public class AnalyticsService {

	private static final Map<String, String> EXPLAINABLE = Map.of(
			"revenue-by-category", """
					SELECT pr.category, ROUND(SUM(ol.quantity * ol.unit_price), 2) AS revenue
					FROM order_lines ol
					JOIN orders o   ON o.id = ol.order_id AND o.status = 'completed'
					JOIN product pr ON pr.id = ol.product_id
					GROUP BY pr.category
					ORDER BY revenue DESC""",
			"top-customers", """
					SELECT c.id, c.name, co.name AS country
					FROM customer c
					JOIN country co ON co.id = c.country_id
					WHERE c.id IN (
					    SELECT customer_id FROM orders
					    WHERE status = 'completed'
					    GROUP BY customer_id ORDER BY count(*) DESC LIMIT 5
					)""",
			"log-levels", """
					SELECT level, count(*) AS n
					FROM events
					GROUP BY level
					ORDER BY level""");

	private final JdbcTemplate jdbc;

	public AnalyticsService(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	/** Revenue per category, only completed orders (lesson 4: GROUP BY). */
	public List<CategoryRevenue> revenueByCategory() {
		return jdbc.query("""
				SELECT pr.category, ROUND(SUM(ol.quantity * ol.unit_price), 2) AS revenue
				FROM order_lines ol
				JOIN orders o   ON o.id = ol.order_id AND o.status = 'completed'
				JOIN product pr ON pr.id = ol.product_id
				GROUP BY pr.category
				ORDER BY revenue DESC
				""", (rs, row) -> new CategoryRevenue(rs.getString("category"), rs.getBigDecimal("revenue")));
	}

	/** Revenue trend, bucketed per day or per month (date_trunc). */
	public List<RevenueTrendRow> revenueTrend(String bucket) {
		String period = switch (bucket) {
			case "day", "month" -> bucket;
			default -> throw new IllegalArgumentException("bucket must be 'day' or 'month', was: " + bucket);
		};
		return jdbc.query("""
				SELECT CAST(date_trunc('%s', o.order_date) AS DATE) AS bucket,
				       ROUND(SUM(ol.quantity * ol.unit_price), 2)  AS revenue
				FROM orders o
				JOIN order_lines ol ON ol.order_id = o.id
				WHERE o.status = 'completed'
				GROUP BY 1
				ORDER BY 1
				""".formatted(period), (rs, row) -> new RevenueTrendRow(
						String.valueOf(rs.getDate("bucket")), rs.getBigDecimal("revenue")));
	}

	/** Best customers by spend, joined to country names (lesson 2). */
	public List<TopCustomer> topCustomers(int limit) {
		if (limit < 1 || limit > 100) {
			throw new IllegalArgumentException("limit must be between 1 and 100, was: " + limit);
		}
		return jdbc.query("""
				SELECT c.id, c.name, co.name AS country,
				       ROUND(SUM(ol.quantity * ol.unit_price), 2) AS spend
				FROM customer c
				JOIN orders o      ON o.customer_id = c.id AND o.status = 'completed'
				JOIN order_lines ol ON ol.order_id   = o.id
				JOIN country co    ON co.id = c.country_id
				GROUP BY c.id, c.name, co.name
				ORDER BY spend DESC
				LIMIT ?
				""", (rs, row) -> new TopCustomer(rs.getLong("id"), rs.getString("name"),
						rs.getString("country"), rs.getBigDecimal("spend")), limit);
	}

	/** Order counts split by status. */
	public List<StatusCount> ordersByStatus() {
		return jdbc.query("SELECT status, count(*) AS n FROM orders GROUP BY status ORDER BY status",
				(rs, row) -> new StatusCount(rs.getString("status"), rs.getLong("n")));
	}

	/** Counts per log level — the "dark data / find the 1%" lesson (lesson 5). */
	public List<LogLevelCount> logLevels() {
		return jdbc.query("SELECT level, count(*) AS n FROM events GROUP BY level ORDER BY level",
				(rs, row) -> new LogLevelCount(rs.getString("level"), rs.getLong("n")));
	}

	/** {@code EXPLAIN ANALYZE} for a whitelisted query (lesson 4: the plan). */
	public ExplainResult explain(String metric) {
		String sql = EXPLAINABLE.get(metric);
		if (sql == null) {
			throw new IllegalArgumentException("unknown explain metric: " + metric
					+ " (choose from " + String.join(", ", EXPLAINABLE.keySet()) + ")");
		}
		String plan = jdbc.query("EXPLAIN ANALYZE " + sql, rs -> {
			// DuckDB returns (explain_key, explain_value) rows; the plan text is
			// in the value column(s), so skip the first (key) column.
			int columns = rs.getMetaData().getColumnCount();
			StringBuilder sb = new StringBuilder();
			while (rs.next()) {
				for (int c = (columns > 1 ? 2 : 1); c <= columns; c++) {
					if (sb.length() > 0) {
						sb.append('\n');
					}
					sb.append(rs.getObject(c));
				}
			}
			return sb.toString();
		});
		return new ExplainResult(metric, sql, plan);
	}

	/** Live engine + dataset facts. */
	public DatabaseInfo databaseInfo() {
		String version = jdbc.queryForObject("SELECT version()", String.class);
		int threads = jdbc.queryForObject(
				"SELECT CAST(value AS INTEGER) FROM duckdb_settings() WHERE name = 'threads'",
				Integer.class);
		String maxMemory = jdbc.queryForObject(
				"SELECT value FROM duckdb_settings() WHERE name = 'max_memory'",
				String.class);
		long orders = rowCount("orders");
		long orderLines = rowCount("order_lines");
		long events = rowCount("events");
		List<String> tables = jdbc.query(
				"SELECT table_name FROM information_schema.tables WHERE table_schema = 'main' ORDER BY table_name",
				(rs, row) -> rs.getString(1));
		return new DatabaseInfo(version, threads, maxMemory, orders, orderLines, events, tables);
	}

	private long rowCount(String table) {
		Long count = jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class);
		return count == null ? 0 : count;
	}
}