package com.example.apirules.model;

import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, String customerName, Order.Status status, List<OrderItem> items,
		Instant createdAt) {
}