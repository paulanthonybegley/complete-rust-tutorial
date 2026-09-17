package com.example.apirules.model;

import java.math.BigDecimal;

public record OrderItem(Long productId, String productName, int quantity, BigDecimal unitPrice) {
}