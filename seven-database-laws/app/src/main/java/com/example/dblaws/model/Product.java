package com.example.dblaws.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * A product in the catalogue.
 *
 * Law 3 - the {@link Index} annotations declare the indexes that make the
 * filtering queries fast; the optimizer (also discussed in Law 3) can then
 * serve them without a full table scan.
 *
 * Law 2 - {@code version} is an optimistic-lock token. When two requests try to
 * write the same row, the database rejects the second one instead of silently
 * overwriting it.
 */
@Entity
@Table(name = "products", indexes = {
		@Index(name = "idx_products_category", columnList = "category"),
		@Index(name = "idx_products_name", columnList = "name")
})
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(nullable = false)
	private int stockQuantity;

	@Version
	private long version;

	@Column(nullable = false)
	private Instant createdAt;

	protected Product() {
	}

	public Product(String name, String category, BigDecimal price, String currency, int stockQuantity,
			Instant createdAt) {
		this.name = name;
		this.category = category;
		this.price = price;
		this.currency = currency;
		this.stockQuantity = stockQuantity;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getCurrency() {
		return currency;
	}

	public int getStockQuantity() {
		return stockQuantity;
	}

	public long getVersion() {
		return version;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}