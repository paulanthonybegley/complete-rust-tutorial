package com.example.dblaws.model;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(Long id, String name, String category, BigDecimal price, String currency,
		int stockQuantity, Instant createdAt) {

	public static ProductResponse from(Product p) {
		return new ProductResponse(p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getCurrency(),
				p.getStockQuantity(), p.getCreatedAt());
	}
}