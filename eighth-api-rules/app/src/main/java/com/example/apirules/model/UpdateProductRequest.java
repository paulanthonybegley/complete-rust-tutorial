package com.example.apirules.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * PATCH body (partial update, Law 3). Every field is optional:
 * a null field means "leave it unchanged".
 */
public record UpdateProductRequest(
		@Size(min = 1, message = "name cannot be empty") String name,
		@Size(min = 1, message = "category cannot be empty") String category,
		@DecimalMin(value = "0.01", message = "price must be positive") BigDecimal price,
		@Size(min = 3, message = "currency must use a 3-letter code") String currency,
		@Min(value = 0, message = "stock cannot be negative") Integer stock) {
}