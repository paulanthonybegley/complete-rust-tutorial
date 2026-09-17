package com.example.apirules.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.apirules.exception.NotFoundException;
import com.example.apirules.model.CreateProductRequest;
import com.example.apirules.model.PageResponse;
import com.example.apirules.model.Product;
import com.example.apirules.model.UpdateProductRequest;

@Service
public class ProductService {

	private final Map<Long, Product> products = new ConcurrentHashMap<>();
	private final AtomicLong nextId = new AtomicLong(4);

	public ProductService() {
		seed(new Product(1L, "Clean Code", "books", new BigDecimal("39.99"), "USD", 100,
				Instant.parse("2026-01-15T09:00:00Z")));
		seed(new Product(2L, "Digital Clock", "electronics", new BigDecimal("19.50"), "EUR", 50,
				Instant.parse("2026-02-01T08:30:00Z")));
		seed(new Product(3L, "Desk Lamp", "home", new BigDecimal("24.99"), "USD", 0,
				Instant.parse("2026-03-10T14:00:00Z")));
		seed(new Product(4L, "Tea Kettle", "home", new BigDecimal("34.50"), "GBP", 12,
				Instant.parse("2026-04-02T11:20:00Z")));
	}

	private void seed(Product product) {
		products.put(product.id(), product);
	}

	public Optional<Product> findById(Long id) {
		return Optional.ofNullable(products.get(id));
	}

	public Product mustFind(Long id) {
		Product product = products.get(id);
		if (product == null) {
			throw new NotFoundException("PRODUCT_NOT_FOUND", "Product " + id + " was not found");
		}
		return product;
	}

	/**
	 * API Law 6 - the path identifies the resource, query parameters refine it.
	 * Filtering, sorting and pagination are all expressed as query parameters,
	 * never as extra path segments.
	 */
	public PageResponse<Product> list(String category, BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock,
			String sort, int page, int size) {
		java.util.stream.Stream<Product> stream = products.values().stream();
		if (category != null && !category.isBlank()) {
			stream = stream.filter(p -> p.category().equalsIgnoreCase(category));
		}
		if (minPrice != null) {
			stream = stream.filter(p -> p.price().compareTo(minPrice) >= 0);
		}
		if (maxPrice != null) {
			stream = stream.filter(p -> p.price().compareTo(maxPrice) <= 0);
		}
		if (inStock != null) {
			stream = stream.filter(p -> inStock ? p.stock() > 0 : p.stock() == 0);
		}
		List<Product> all = stream.sorted(sortComparator(sort)).toList();
		long total = all.size();
		int totalPages = (int) Math.ceil((double) total / size);
		int from = Math.min(page * size, (int) total);
		int to = Math.min(from + size, (int) total);
		List<Product> content = total == 0 ? List.of() : all.subList(from, to);
		return new PageResponse<>(content, page, size, total, totalPages);
	}

	private Comparator<Product> sortComparator(String sort) {
		String[] parts = sort.split(":");
		boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]);
		Comparator<Product> by = switch (parts[0].toLowerCase()) {
			case "price" -> Comparator.comparing(Product::price);
			case "stock" -> Comparator.comparing(Product::stock);
			case "createdat" -> Comparator.comparing(Product::createdAt);
			default -> Comparator.comparing(Product::name, String.CASE_INSENSITIVE_ORDER);
		};
		return desc ? by.reversed() : by;
	}

	public Product create(CreateProductRequest request) {
		long id = nextId.incrementAndGet();
		Product product = new Product(id, request.name(), request.category(), request.price(), request.currency(),
				request.stock(), Instant.now());
		products.put(id, product);
		return product;
	}

	/** API Law 3 - PUT replaces the entire representation and is idempotent. */
	public Product replace(Long id, CreateProductRequest request) {
		mustFind(id);
		Product product = new Product(id, request.name(), request.category(), request.price(), request.currency(),
				request.stock(), Instant.now());
		products.put(id, product);
		return product;
	}

	/** API Law 3 - PATCH applies a partial update: only provided fields change. */
	public Product update(Long id, UpdateProductRequest request) {
		Product existing = mustFind(id);
		Product product = new Product(id,
				request.name() != null ? request.name() : existing.name(),
				request.category() != null ? request.category() : existing.category(),
				request.price() != null ? request.price() : existing.price(),
				request.currency() != null ? request.currency() : existing.currency(),
				request.stock() != null ? request.stock() : existing.stock(),
				existing.createdAt());
		products.put(id, product);
		return product;
	}

	public void delete(Long id) {
		if (products.remove(id) == null) {
			throw new NotFoundException("PRODUCT_NOT_FOUND", "Product " + id + " was not found");
		}
	}

	public Product reduceStock(Long productId, int quantity) {
		Product product = mustFind(productId);
		Product updated = new Product(product.id(), product.name(), product.category(), product.price(),
				product.currency(), product.stock() - quantity, product.createdAt());
		products.put(productId, updated);
		return updated;
	}
}