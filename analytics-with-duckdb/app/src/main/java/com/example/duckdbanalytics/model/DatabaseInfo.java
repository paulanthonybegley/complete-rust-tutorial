package com.example.duckdbanalytics.model;

import java.util.List;

/** Live facts about the embedded engine and the seeded dataset. */
public record DatabaseInfo(
		String duckdbVersion,
		int threads,
		String maxMemory,
		long orders,
		long orderLines,
		long events,
		List<String> tables) {
}