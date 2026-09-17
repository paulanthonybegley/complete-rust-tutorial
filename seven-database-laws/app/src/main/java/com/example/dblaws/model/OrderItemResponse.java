package com.example.dblaws.model;

import java.math.BigDecimal;

public record OrderItemResponse(Long id, Long productId, String productName, BigDecimal unitPrice, int quantity) {

	public static OrderItemResponse from(OrderItem item) {
		return new OrderItemResponse(item.getId(), item.getProduct().getId(), item.getProductName(),
				item.getUnitPrice(), item.getQuantity());
	}
}