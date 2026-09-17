package com.example.apirules.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(
		@NotBlank(message = "name is required") String name,
		@NotBlank(message = "category is required") String category,
		@NotNull(message = "price is required")
		@DecimalMin(value = "0.01", message = "price must be positive") BigDecimal price,
		@NotBlank(message = "currency is required") String currency,
		@Min(value = 0, message = "stock cannot be negative") int stock) {
}