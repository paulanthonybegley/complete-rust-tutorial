package com.example.apirules.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The current (v1.1+) response shape for a product.
 * Added {@code currency} in v1.1 - an additive, non-breaking change.
 */
public record ProductResponse(Long id, String name, String category, BigDecimal price, String currency, int stock,
		Instant createdAt) {
}