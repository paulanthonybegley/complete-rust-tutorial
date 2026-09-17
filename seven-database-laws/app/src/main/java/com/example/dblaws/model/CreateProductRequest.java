package com.example.dblaws.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
		@NotBlank String name,
		@NotBlank String category,
		@NotNull @DecimalMin("0.01") BigDecimal price,
		@NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "[A-Z]{3}") String currency,
		@Min(0) int stockQuantity) {
}