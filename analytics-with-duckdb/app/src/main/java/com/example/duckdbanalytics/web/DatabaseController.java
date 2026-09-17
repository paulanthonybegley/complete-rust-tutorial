package com.example.duckdbanalytics.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.duckdbanalytics.model.DatabaseInfo;
import com.example.duckdbanalytics.service.AnalyticsService;

/** Live facts about the embedded DuckDB engine and the seeded dataset. */
@RestController
@RequestMapping("/database")
public class DatabaseController {

	private final AnalyticsService service;

	public DatabaseController(AnalyticsService service) {
		this.service = service;
	}

	@GetMapping("/info")
	public DatabaseInfo info() {
		return service.databaseInfo();
	}
}