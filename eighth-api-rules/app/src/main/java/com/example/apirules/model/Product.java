package com.example.apirules.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Internal domain representation of a product.
 *
 * The stored shape is richer than any single API version (Law 7 lesson):
 * v1.0 exposed a plain {@code price}, while v1.1 adds {@code currency}.
 */
public record Product(Long id, String name, String category, BigDecimal price, String currency, int stock,
		Instant createdAt) {
}