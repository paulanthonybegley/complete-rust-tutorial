package com.example.apirules.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The v1.0 response shape (API Law 7 lesson).
 * Older clients still receive exactly this payload: it has no {@code currency}
 * field and no {@code stock} field, so nothing they relied on breaks.
 */
public record ProductV1(Long id, String name, String category, BigDecimal price, Instant createdAt) {
}