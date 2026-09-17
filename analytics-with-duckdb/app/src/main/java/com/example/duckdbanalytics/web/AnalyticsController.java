package com.example.duckdbanalytics.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.duckdbanalytics.model.CategoryRevenue;
import com.example.duckdbanalytics.model.ExplainResult;
import com.example.duckdbanalytics.model.LogLevelCount;
import com.example.duckdbanalytics.model.RevenueTrendRow;
import com.example.duckdbanalytics.model.StatusCount;
import com.example.duckdbanalytics.model.TopCustomer;
import com.example.duckdbanalytics.service.AnalyticsService;

/**
 * Read-only analytics REST API over the DuckDB dataset. Each endpoint is a
 * lesson: a GROUP BY aggregate, a join, a window over time, or an EXPLAIN.
 */
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

	private final AnalyticsService service;

	public AnalyticsController(AnalyticsService service) {
		this.service = service;
	}

	@GetMapping("/revenue-by-category")
	public List<CategoryRevenue> revenueByCategory() {
		return service.revenueByCategory();
	}

	@GetMapping("/revenue-trend")
	public List<RevenueTrendRow> revenueTrend(@RequestParam(defaultValue = "month") String bucket) {
		return service.revenueTrend(bucket);
	}

	@GetMapping("/top-customers")
	public List<TopCustomer> topCustomers(@RequestParam(defaultValue = "5") int limit) {
		return service.topCustomers(limit);
	}

	@GetMapping("/orders-by-status")
	public List<StatusCount> ordersByStatus() {
		return service.ordersByStatus();
	}

	@GetMapping("/log-levels")
	public List<LogLevelCount> logLevels() {
		return service.logLevels();
	}

	/** The query plan behind the numbers — the taught "look inside" endpoint. */
	@GetMapping("/explain")
	public ExplainResult explain(@RequestParam String metric) {
		return service.explain(metric);
	}
}