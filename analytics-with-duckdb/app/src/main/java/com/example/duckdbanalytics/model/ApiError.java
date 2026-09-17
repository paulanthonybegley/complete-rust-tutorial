package com.example.duckdbanalytics.model;

import java.time.Instant;

/**
 * Consistent error shape across every endpoint (the api-rules course's Law 5 —
 * kept here deliberately so learners see the same contract in both courses).
 */
public record ApiError(int status, String code, String message, Instant timestamp) {

	public static ApiError of(int status, String code, String message) {
		return new ApiError(status, code, message, Instant.now());
	}
}