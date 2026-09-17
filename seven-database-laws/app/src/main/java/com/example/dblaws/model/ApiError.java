package com.example.dblaws.model;

import java.time.Instant;
import java.util.List;

public record ApiError(int status, String code, String message, Instant timestamp,
		List<FieldError> fieldErrors) {

	public record FieldError(String field, String message) {
	}

	public static ApiError of(int status, String code, String message) {
		return new ApiError(status, code, message, Instant.now(), List.of());
	}

	public static ApiError of(int status, String code, String message, List<FieldError> errors) {
		return new ApiError(status, code, message, Instant.now(), errors);
	}
}