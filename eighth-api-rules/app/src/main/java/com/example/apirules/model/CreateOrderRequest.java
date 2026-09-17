package com.example.apirules.model;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
		@NotBlank(message = "customerName is required") String customerName,
		@NotNull(message = "items are required")
		@Size(min = 1, message = "at least one item is required") List<@Valid Item> items) {

	public record Item(
			@NotNull(message = "productId is required") Long productId,
			@Min(value = 1, message = "quantity must be at least 1") int quantity) {
	}
}