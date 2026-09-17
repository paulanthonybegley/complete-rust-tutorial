package com.example.dblaws.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.dblaws.model.Order.OrderStatus;

public record OrderResponse(Long id, Long customerId, String customerName, String customerEmail,
		OrderStatus status, BigDecimal totalAmount, Instant createdAt, List<OrderItemResponse> items) {

	public static OrderResponse from(Order order) {
		return new OrderResponse(order.getId(), order.getCustomer().getId(), order.getCustomer().getName(),
				order.getCustomer().getEmail(), order.getStatus(), order.getTotalAmount(), order.getCreatedAt(),
				order.getItems().stream().map(OrderItemResponse::from).toList());
	}
}