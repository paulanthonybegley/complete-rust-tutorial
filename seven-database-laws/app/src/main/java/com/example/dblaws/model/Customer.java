package com.example.dblaws.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * A customer who places orders.
 *
 * Access pattern (Law 1): accounts are looked up by address at checkout, so the
 * unique index below exists for exactly that query.
 */
@Entity
@Table(name = "customers", indexes = @Index(name = "idx_customers_email", columnList = "email", unique = true))
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private Instant createdAt;

	protected Customer() {
	}

	public Customer(String name, String email, Instant createdAt) {
		this.name = name;
		this.email = email;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}