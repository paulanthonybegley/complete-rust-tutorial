package com.example.apirules.model;

import java.time.Instant;
import java.util.List;

/**
 * API Law 5 - keep error responses consistent.
 * Every error response, from every endpoint, has exactly this shape.
 */
public record ApiError(int status, String code, String message, Instant timestamp, List<FieldError> fieldErrors) {

	public record FieldError(String field, String message) {
	}

	public static ApiError of(int status, String code, String message) {
		return new ApiError(status, code, message, Instant.now(), List.of());
	}

	public static ApiError of(int status, String code, String message, List<FieldError> fieldErrors) {
		return new ApiError(status, code, message, Instant.now(), fieldErrors);
	}
}