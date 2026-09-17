package com.example.dblaws.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dblaws.model.CustomerResponse;
import com.example.dblaws.model.OrderResponse;
import com.example.dblaws.service.CustomerService;
import com.example.dblaws.service.OrderService;

@RestController
@RequestMapping("/customers")
public class CustomerController {

	private final CustomerService customerService;
	private final OrderService orderService;

	public CustomerController(CustomerService customerService, OrderService orderService) {
		this.customerService = customerService;
		this.orderService = orderService;
	}

	@GetMapping
	public List<CustomerResponse> list() {
		return customerService.list();
	}

	@GetMapping("/{id}/orders")
	public List<OrderResponse> orders(@PathVariable Long id) {
		customerService.mustFind(id);
		return orderService.byCustomer(id);
	}
}