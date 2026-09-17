package com.example.duckdbanalytics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.duckdbanalytics.model.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

	/** Bad parameter values (unknown bucket, unknown explain metric, ...). */
	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<ApiError> badRequest(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(ApiError.of(400, "BAD_REQUEST", ex.getMessage()));
	}

	/** Everything else — SQL problems surface here; the logs explain them. */
	@ExceptionHandler(RuntimeException.class)
	ResponseEntity<ApiError> internal(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiError.of(500, "INTERNAL_ERROR", ex.getMessage()));
	}
}