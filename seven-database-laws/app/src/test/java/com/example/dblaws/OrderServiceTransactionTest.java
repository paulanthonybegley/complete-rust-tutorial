package com.example.dblaws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.dblaws.exception.InsufficientStockException;
import com.example.dblaws.model.Customer;
import com.example.dblaws.model.OrderResponse;
import com.example.dblaws.model.PlaceOrderRequest;
import com.example.dblaws.model.Product;
import com.example.dblaws.repository.CustomerRepository;
import com.example.dblaws.repository.OrderRepository;
import com.example.dblaws.repository.ProductRepository;
import com.example.dblaws.service.OrderService;

/**
 * Law 2 - "ask what happens if the data is wrong".
 *
 * {@link OrderService#placeOrder} is a single transaction. These tests prove
 * that a failing line undoes stock already decremented on earlier lines, i.e.
 * the database never observes a half-committed order.
 */
@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTransactionTest {

	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Test
	void successfulOrderCommitsAndStockDropsAtomically() {
		Product widget = productRepository.save(
				new Product("Widget", "tools", new BigDecimal("10.00"), "USD", 100, Instant.now()));
		Customer customer = customerRepository.save(new Customer("Txn Tester", "txn@example.com", Instant.now()));

		OrderResponse order = orderService.placeOrder(new PlaceOrderRequest(customer.getName(), customer.getEmail(),
				List.of(new PlaceOrderRequest.Item(widget.getId(), 3))));

		assertEquals(97, productRepository.findById(widget.getId()).orElseThrow().getStockQuantity());
		assertEquals(new BigDecimal("30.00"), order.totalAmount());
		assertEquals(1, order.items().size());
	}

	@Test
	void failingLineRollsBackEveryEarlierStockDecrement() {
		Product available = productRepository.save(
				new Product("Available", "tools", new BigDecimal("5.00"), "USD", 10, Instant.now()));
		Product scarce = productRepository.save(
				new Product("Scarce", "tools", new BigDecimal("5.00"), "USD", 1, Instant.now()));
		Customer customer = customerRepository.save(new Customer("Rollback Tester", "rb@example.com", Instant.now()));
		long baselineOrders = orderRepository.count();

		assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(
				new PlaceOrderRequest(customer.getName(), customer.getEmail(),
						List.of(new PlaceOrderRequest.Item(available.getId(), 5),
								new PlaceOrderRequest.Item(scarce.getId(), 2)))));

		assertEquals(10, productRepository.findById(available.getId()).orElseThrow().getStockQuantity(),
				"first line was decremented but the transaction must undo it");
		assertEquals(1, productRepository.findById(scarce.getId()).orElseThrow().getStockQuantity());
		assertEquals(baselineOrders, orderRepository.count(), "no order row may survive the rollback");
	}

	@Test
	void exactLastUnitSucceeds() {
		Product lastUnit = productRepository.save(
				new Product("Last Unit", "tools", new BigDecimal("8.00"), "USD", 1, Instant.now()));
		Customer customer = customerRepository.save(new Customer("Last Buyer", "last@example.com", Instant.now()));

		orderService.placeOrder(new PlaceOrderRequest(customer.getName(), customer.getEmail(),
				List.of(new PlaceOrderRequest.Item(lastUnit.getId(), 1))));

		assertEquals(0, productRepository.findById(lastUnit.getId()).orElseThrow().getStockQuantity());
	}
}