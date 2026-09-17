package com.example.dblaws.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * A customer order and its lines.
 *
 * Law 1 - the schema follows the access patterns, not the business diagram:
 * the hot read is "orders for a customer", so {@code customer_id} is indexed;
 * the hot aggregate is "sales per month", so {@code created_at} is indexed.
 */
@Entity
@Table(name = "customer_orders", indexes = {
		@Index(name = "idx_orders_customer_id", columnList = "customer_id"),
		@Index(name = "idx_orders_created_at", columnList = "created_at")
})
public class Order {

	public enum OrderStatus {
		CREATED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(nullable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	protected Order() {
	}

	public Order(Customer customer, OrderStatus status, BigDecimal totalAmount, Instant createdAt) {
		this.customer = customer;
		this.status = status;
		this.totalAmount = totalAmount;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public OrderItem addItem(Product product, int quantity) {
		OrderItem item = new OrderItem(this, product, product.getName(), product.getPrice(), quantity);
		items.add(item);
		return item;
	}
}