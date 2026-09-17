package com.example.apirules.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.apirules.exception.ConflictException;
import com.example.apirules.exception.NotFoundException;
import com.example.apirules.model.CreateOrderRequest;
import com.example.apirules.model.Order;
import com.example.apirules.model.OrderItem;
import com.example.apirules.model.PageResponse;
import com.example.apirules.model.Product;

@Service
public class OrderService {

	private static final Map<Order.Status, Set<Order.Status>> TRANSITIONS = Map.of(
			Order.Status.CREATED, Set.of(Order.Status.CONFIRMED, Order.Status.CANCELLED),
			Order.Status.CONFIRMED, Set.of(Order.Status.SHIPPED, Order.Status.CANCELLED),
			Order.Status.SHIPPED, Set.of(Order.Status.DELIVERED),
			Order.Status.DELIVERED, Set.of(),
			Order.Status.CANCELLED, Set.of());

	private final ProductService productService;
	private final Map<Long, Order> orders = new ConcurrentHashMap<>();
	private final AtomicLong nextId = new AtomicLong(2);

	public OrderService(ProductService productService) {
		this.productService = productService;
		orders.put(1L, new Order(1L, "Alice", Order.Status.CREATED,
				List.of(new OrderItem(1L, "Clean Code", 2, new BigDecimal("39.99"))),
				Instant.parse("2026-05-01T10:00:00Z")));
		orders.put(2L, new Order(2L, "Bob", Order.Status.CONFIRMED,
				List.of(new OrderItem(4L, "Tea Kettle", 1, new BigDecimal("34.50"))),
				Instant.parse("2026-05-10T09:30:00Z")));
	}

	/**
	 * API Law 4 - make status codes useful. A request that cannot be satisfied
	 * because it conflicts with the current state of the system (insufficient
	 * stock here) answers 409, not 200-with-an-error-in-the-body.
	 */
	public Order create(String customerName, List<CreateOrderRequest.Item> items) {
		List<OrderItem> orderItems = items.stream().map(item -> {
			Product product = productService.findById(item.productId())
					.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
							"Product " + item.productId() + " was not found"));
			if (product.stock() < item.quantity()) {
				throw new ConflictException("INSUFFICIENT_STOCK",
						"Only " + product.stock() + " of product " + item.productId()
								+ " are in stock, " + item.quantity() + " requested");
			}
			return new OrderItem(product.id(), product.name(), item.quantity(), product.price());
		}).toList();

		orderItems.forEach(item -> productService.reduceStock(item.productId(), item.quantity()));

		long id = nextId.incrementAndGet();
		Order order = new Order(id, customerName, Order.Status.CREATED, orderItems, Instant.now());
		orders.put(id, order);
		return order;
	}

	public Order mustFind(Long id) {
		Order order = orders.get(id);
		if (order == null) {
			throw new NotFoundException("ORDER_NOT_FOUND", "Order " + id + " was not found");
		}
		return order;
	}

	public PageResponse<Order> list(int page, int size) {
		List<Order> all = orders.values().stream()
				.sorted(Comparator.comparing(Order::id))
				.toList();
		long total = all.size();
		int totalPages = (int) Math.ceil((double) total / size);
		int from = Math.min(page * size, (int) total);
		int to = Math.min(from + size, (int) total);
		List<Order> content = total == 0 ? List.of() : all.subList(from, to);
		return new PageResponse<>(content, page, size, total, totalPages);
	}

	/**
	 * API Law 4 - a transition that conflicts with the current state (either
	 * the same status again, or an illegal jump) answers 409.
	 */
	public Order updateStatus(Long id, Order.Status target) {
		Order order = mustFind(id);
		if (order.status() == target) {
			throw new ConflictException("ORDER_ALREADY_IN_STATUS",
					"Order " + id + " is already in status " + target);
		}
		if (!TRANSITIONS.get(order.status()).contains(target)) {
			throw new ConflictException("INVALID_STATUS_TRANSITION",
					"Cannot move order " + id + " from " + order.status() + " to " + target);
		}
		Order updated = new Order(order.id(), order.customerName(), target, order.items(), order.createdAt());
		orders.put(id, updated);
		return updated;
	}
}