package com.example.dblaws.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dblaws.exception.InsufficientStockException;
import com.example.dblaws.exception.NotFoundException;
import com.example.dblaws.model.Customer;
import com.example.dblaws.model.Order;
import com.example.dblaws.model.OrderResponse;
import com.example.dblaws.model.PlaceOrderRequest;
import com.example.dblaws.model.Product;
import com.example.dblaws.repository.CustomerRepository;
import com.example.dblaws.repository.OrderRepository;
import com.example.dblaws.repository.ProductRepository;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final CustomerRepository customerRepository;

	public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
			CustomerRepository customerRepository) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
		this.customerRepository = customerRepository;
	}

	/**
	 * Law 2 - "ask what happens if the data is wrong". This is a single
	 * transaction: every stock decrement and the order insert commit together
	 * or not at all. If any line cannot be fulfilled the transaction is rolled
	 * back and earlier lines keep their stock.
	 *
	 * The stock check is atomic too: the conditional UPDATE
	 * ({@code stock >= quantity}) either takes the stock or affects zero rows,
	 * so two concurrent checkouts of the last unit cannot both succeed.
	 */
	@Transactional
	public OrderResponse placeOrder(PlaceOrderRequest request) {
		Customer customer = findOrCreateCustomer(request.customerName(), request.customerEmail());

		Order order = new Order(customer, Order.OrderStatus.CREATED, BigDecimal.ZERO, Instant.now());

		for (PlaceOrderRequest.Item item : request.items()) {
			Product product = productRepository.findById(item.productId())
					.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
							"Product " + item.productId() + " was not found"));

			int decremented = productRepository.decrementStockIfAvailable(product.getId(), item.quantity());
			if (decremented == 0) {
				throw new InsufficientStockException("INSUFFICIENT_STOCK",
						"Only " + product.getStockQuantity() + " of product " + product.getId()
								+ " are in stock, " + item.quantity() + " requested");
			}
			order.addItem(product, item.quantity());
		}

		order.setTotalAmount(order.getItems().stream()
				.map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add));
		orderRepository.save(order);
		return OrderResponse.from(order);
	}

	/**
	 * Law 2 - creating the customer is itself a guarded write: the unique index
	 * on email is the final backstop, so a duplicate address can never create a
	 * second account.
	 */
	private Customer findOrCreateCustomer(String name, String email) {
		return customerRepository.findByEmail(email)
				.orElseGet(() -> customerRepository.save(new Customer(name, email, Instant.now())));
	}

	@Transactional(readOnly = true)
	public OrderResponse mustFind(Long id) {
		Order order = orderRepository.findWithItemsById(id)
				.orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Order " + id + " was not found"));
		return OrderResponse.from(order);
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> byCustomer(Long customerId) {
		return orderRepository.findWithItemsByCustomer_IdOrderByCreatedAtDesc(customerId)
				.stream().map(OrderResponse::from).toList();
	}
}