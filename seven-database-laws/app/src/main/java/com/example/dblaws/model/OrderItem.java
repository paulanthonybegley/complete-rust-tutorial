package com.example.dblaws.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One line of an order.
 *
 * Product name and unit price are snapshotted here on purpose (Law 2): if the
 * product is later renamed or repriced, past invoices must not change. The
 * {@code product_id} column points back at the catalogue but is not used to
 * derive the invoice.
 */
@Entity
@Table(name = "order_items", indexes = @Index(name = "idx_order_items_product_id", columnList = "product_id"))
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private String productName;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(nullable = false)
	private int quantity;

	protected OrderItem() {
	}

	public OrderItem(Order order, Product product, String productName, BigDecimal unitPrice, int quantity) {
		this.order = order;
		this.product = product;
		this.productName = productName;
		this.unitPrice = unitPrice;
		this.quantity = quantity;
	}

	public Long getId() {
		return id;
	}

	public Order getOrder() {
		return order;
	}

	public Product getProduct() {
		return product;
	}

	public String getProductName() {
		return productName;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public int getQuantity() {
		return quantity;
	}
}