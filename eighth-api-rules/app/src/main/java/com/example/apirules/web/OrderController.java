package com.example.apirules.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.apirules.exception.BadRequestException;
import com.example.apirules.model.CreateOrderRequest;
import com.example.apirules.model.Order;
import com.example.apirules.model.OrderItem;
import com.example.apirules.model.OrderResponse;
import com.example.apirules.model.PageResponse;
import com.example.apirules.model.StatusUpdateRequest;
import com.example.apirules.service.OrderService;

import jakarta.validation.Valid;

/**
 * API Law 1 - orders are resources: create with POST, read with GET.
 * API Law 2 - the nested sub-resource {@code /orders/{id}/items} is a
 * predictable extension of the parent resource, not a bespoke action URL.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping(version = "1.0+")
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
			UriComponentsBuilder uriBuilder) {
		Order order = orderService.create(request.customerName(), request.items());
		URI location = uriBuilder.path("/orders/{id}").buildAndExpand(order.id()).toUri();
		return ResponseEntity.created(location).body(toResponse(order));
	}

	@GetMapping(version = "1.0+")
	public PageResponse<OrderResponse> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		PageResponse<Order> result = orderService.list(page, size);
		List<OrderResponse> content = result.content().stream().map(this::toResponse).toList();
		return new PageResponse<>(content, result.page(), result.size(), result.totalElements(), result.totalPages());
	}

	@GetMapping(path = "/{id}", version = "1.0+")
	public OrderResponse get(@PathVariable Long id) {
		return toResponse(orderService.mustFind(id));
	}

	@GetMapping(path = "/{id}/items", version = "1.0+")
	public List<OrderItem> items(@PathVariable Long id) {
		return orderService.mustFind(id).items();
	}

	/**
	 * API Law 3/4 - a status change is a partial update on the status
	 * sub-resource, answered with 200; a transition that conflicts with the
	 * current state is answered with 409.
	 */
	@PatchMapping(path = "/{id}/status", version = "1.0+")
	public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
		return toResponse(orderService.updateStatus(id, parseStatus(request.status())));
	}

	private Order.Status parseStatus(String raw) {
		try {
			return Order.Status.valueOf(raw.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new BadRequestException("INVALID_STATUS",
					"Unknown order status '" + raw + "'. Supported values: "
							+ List.of(Order.Status.values()).toString());
		}
	}

	private OrderResponse toResponse(Order order) {
		return new OrderResponse(order.id(), order.customerName(), order.status(), order.items(), order.createdAt());
	}
}