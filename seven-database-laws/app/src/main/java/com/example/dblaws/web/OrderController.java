package com.example.dblaws.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.dblaws.model.OrderResponse;
import com.example.dblaws.model.PlaceOrderRequest;
import com.example.dblaws.service.OrderService;

import jakarta.validation.Valid;

/**
 * Order screen.
 *
 * Law 2 - {@code POST /orders} is the transaction demo: everything commits
 * together or rolls back (see {@link OrderService#placeOrder}).
 * Law 1 - {@code GET /orders?customerId=} is the hot "orders for a customer"
 * read, served via the {@code idx_orders_customer_id} index.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request,
			UriComponentsBuilder uriBuilder) {
		OrderResponse order = orderService.placeOrder(request);
		URI location = uriBuilder.path("/orders/{id}").buildAndExpand(order.id()).toUri();
		return ResponseEntity.status(HttpStatus.CREATED).location(location).body(order);
	}

	@GetMapping("/{id}")
	public OrderResponse get(@PathVariable Long id) {
		return orderService.mustFind(id);
	}

	@GetMapping
	public List<OrderResponse> byCustomer(@RequestParam Long customerId) {
		return orderService.byCustomer(customerId);
	}
}