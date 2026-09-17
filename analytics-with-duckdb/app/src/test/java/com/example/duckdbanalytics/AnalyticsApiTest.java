package com.example.duckdbanalytics;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/**
 * End-to-end checks of the analytics API. The whole suite runs against one
 * in-memory DuckDB engine (`jdbc:duckdb:` in application-test.yml), seeded by
 * {@code seed/seed.sql} — no Docker, no file, same dataset as the live app.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsApiTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void databaseInfoReportsEngineAndSeedSizes() throws Exception {
		mockMvc.perform(get("/database/info"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.duckdbVersion").value(containsString("1.3")))
				.andExpect(jsonPath("$.threads").value(greaterThan(0)))
				.andExpect(jsonPath("$.orders").value(50000))
				.andExpect(jsonPath("$.orderLines").value(greaterThan(200000)))
				.andExpect(jsonPath("$.events").value(300000))
				.andExpect(jsonPath("$.tables").isArray());
	}

	@Test
	void revenueByCategoryCoversAllFourCategories() throws Exception {
		mockMvc.perform(get("/analytics/revenue-by-category"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(4))
				.andExpect(jsonPath("$[0].category").isNotEmpty())
				.andExpect(jsonPath("$[0].revenue").isNumber());
	}

	@Test
	void revenueTrendBucketsByMonthAndByDay() throws Exception {
		mockMvc.perform(get("/analytics/revenue-trend").param("bucket", "month"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(12));
		mockMvc.perform(get("/analytics/revenue-trend").param("bucket", "day"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(365));
		mockMvc.perform(get("/analytics/revenue-trend").param("bucket", "year"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void topCustomersAreRankedBySpendAndWindowedByLimit() throws Exception {
		mockMvc.perform(get("/analytics/top-customers").param("limit", "3"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].spend").isNumber())
				.andExpect(jsonPath("$[0].country").isNotEmpty());
		mockMvc.perform(get("/analytics/top-customers").param("limit", "0"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void ordersSplitBetweenCompletedAndCancelled() throws Exception {
		mockMvc.perform(get("/analytics/orders-by-status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[?(@.status=='completed')].count").exists())
				.andExpect(jsonPath("$[?(@.status=='cancelled')].count").exists());
	}

	@Test
	void logLevelsShowRareEventsAmidstInformationLogs() throws Exception {
		Map<String, Long> levels = logLevelCounts();
		org.junit.jupiter.api.Assertions.assertTrue(levels.get("INFO") > 200_000,
				"INFO should dominate: " + levels);
		for (String rare : List.of("ERROR", "WARN")) {
			long n = levels.get(rare);
			org.junit.jupiter.api.Assertions.assertTrue(n > 2_400 && n < 3_200,
					rare + " should be ~1% of events: " + levels);
		}
		org.junit.jupiter.api.Assertions.assertTrue(levels.get("FATAL") > 200,
				"FATAL should exist but be ~0.1%: " + levels);
	}

	private Map<String, Long> logLevelCounts() throws Exception {
		String body = mockMvc.perform(get("/analytics/log-levels"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(4))
				.andReturn().getResponse().getContentAsString();
		java.util.Map<String, Long> counts = new java.util.HashMap<>();
		for (Object row : JsonPath.<List<?>>read(body, "$")) {
			@SuppressWarnings("unchecked")
			Map<String, Object> entry = (Map<String, Object>) row;
			counts.put((String) entry.get("level"), ((Number) entry.get("count")).longValue());
		}
		return counts;
	}

	@Test
	void explainReturnsPlanForKnownMetricAndRejectsUnknownOnes() throws Exception {
		mockMvc.perform(get("/analytics/explain").param("metric", "revenue-by-category"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("revenue-by-category"))
				.andExpect(jsonPath("$.plan").value(containsString("SCAN")));
		mockMvc.perform(get("/analytics/explain").param("metric", "no-such-query"))
				.andExpect(status().isBadRequest());
	}
}