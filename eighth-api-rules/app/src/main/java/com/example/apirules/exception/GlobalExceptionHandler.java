package com.example.apirules.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.apirules.model.ApiError;

/**
 * API Law 5 - keep error responses consistent, and Law 4 - make status codes
 * useful. Every mapped exception produces a structured {@link ApiError} body
 * with the correct HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleNotFound(NotFoundException ex) {
		return ApiError.of(HttpStatus.NOT_FOUND.value(), ex.getCode(), ex.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiError handleConflict(ConflictException ex) {
		return ApiError.of(HttpStatus.CONFLICT.value(), ex.getCode(), ex.getMessage());
	}

	@ExceptionHandler(BadRequestException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleBadRequest(BadRequestException ex) {
		return ApiError.of(HttpStatus.BAD_REQUEST.value(), ex.getCode(), ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ApiError handleValidation(MethodArgumentNotValidException ex) {
		List<ApiError.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(field -> new ApiError.FieldError(field.getField(), field.getDefaultMessage()))
				.toList();
		return ApiError.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), "VALIDATION_FAILED",
				"One or more fields failed validation", errors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleUnreadableBody(HttpMessageNotReadableException ex) {
		return ApiError.of(HttpStatus.BAD_REQUEST.value(), "MALFORMED_JSON", "Request body could not be read as JSON");
	}
}