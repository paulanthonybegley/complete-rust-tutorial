package com.example.dblaws.config;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.dblaws.model.Customer;
import com.example.dblaws.model.PlaceOrderRequest;
import com.example.dblaws.model.Product;
import com.example.dblaws.repository.CustomerRepository;
import com.example.dblaws.repository.ProductRepository;
import com.example.dblaws.service.OrderService;

/**
 * Seeds the empty database with catalogue data (idempotent - it is a no-op when
 * products already exist). Seeding via the application instead of data.sql keeps
 * the schema/bootstrap order unambiguous and lets the same path run on Postgres
 * and H2.
 */
@Component
public class DataInitializer implements CommandLineRunner {

	private final ProductRepository productRepository;
	private final CustomerRepository customerRepository;
	private final OrderService orderService;

	public DataInitializer(ProductRepository productRepository, CustomerRepository customerRepository,
			OrderService orderService) {
		this.productRepository = productRepository;
		this.customerRepository = customerRepository;
		this.orderService = orderService;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (productRepository.count() > 0) {
			return;
		}

		Product coffee = save("Organic Coffee Beans", "grocery", "12.50", 320);
		Product kettle = save("Electric Kettle", "home", "34.50", 90);
		Product clock = save("Digital Clock", "home", "21.00", 140);
		Product book = save("Clean Code", "books", "39.99", 5);
		Product notebook = save("Notebook", "office", "4.99", 500);
		Product pen = save("Gel Pen", "office", "2.49", 800);

		Customer alice = customerRepository.save(new Customer("Alice Rivera", "alice@example.com", Instant.now()));
		Customer bob = customerRepository.save(new Customer("Bob Chen", "bob@example.com", Instant.now()));
		Customer carol = customerRepository.save(new Customer("Carol Nguyen", "carol@example.com", Instant.now()));

		orderService.placeOrder(new PlaceOrderRequest(alice.getName(), alice.getEmail(),
				java.util.List.of(new PlaceOrderRequest.Item(book.getId(), 1), new PlaceOrderRequest.Item(pen.getId(), 3))));
		orderService.placeOrder(new PlaceOrderRequest(bob.getName(), bob.getEmail(),
				java.util.List.of(new PlaceOrderRequest.Item(coffee.getId(), 2), new PlaceOrderRequest.Item(kettle.getId(), 1))));
		orderService.placeOrder(new PlaceOrderRequest(carol.getName(), carol.getEmail(),
				java.util.List.of(new PlaceOrderRequest.Item(notebook.getId(), 10), new PlaceOrderRequest.Item(coffee.getId(), 1))));
	}

	private Product save(String name, String category, String price, int stock) {
		return productRepository.save(new Product(name, category, new BigDecimal(price), "USD", stock, Instant.now()));
	}
}