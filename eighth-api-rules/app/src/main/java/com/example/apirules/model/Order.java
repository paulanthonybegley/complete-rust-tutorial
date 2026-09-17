package com.example.apirules.model;

import java.time.Instant;
import java.util.List;

public record Order(Long id, String customerName, Status status, List<OrderItem> items, Instant createdAt) {

	public enum Status {
		CREATED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
	}
}