package com.example.dblaws.model;

import java.math.BigDecimal;

/**
 * "Top customers" projection (Law 1 - an access pattern discovered from product
 * owners: the finance team wants the highest spenders first).
 */
public class CustomerSpend {

	private final Long customerId;
	private final String customerName;
	private final BigDecimal totalSpent;

	public CustomerSpend(Long customerId, String customerName, BigDecimal totalSpent) {
		this.customerId = customerId;
		this.customerName = customerName;
		this.totalSpent = totalSpent;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public BigDecimal getTotalSpent() {
		return totalSpent;
	}
}