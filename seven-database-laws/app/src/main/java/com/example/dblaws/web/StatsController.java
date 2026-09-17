package com.example.dblaws.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dblaws.model.CustomerSpend;
import com.example.dblaws.model.MonthlySales;
import com.example.dblaws.service.StatsService;

/**
 * Report endpoints.
 *
 * Law 1 - "sales per month" and "top customers" are access patterns discovered
 * from the product team; both are answered with database-side aggregation.
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

	private final StatsService statsService;

	public StatsController(StatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping("/monthly-sales")
	public List<MonthlySales> monthlySales() {
		return statsService.monthlySales();
	}

	@GetMapping("/top-customers")
	public List<CustomerSpend> topCustomers() {
		return statsService.topCustomers();
	}
}