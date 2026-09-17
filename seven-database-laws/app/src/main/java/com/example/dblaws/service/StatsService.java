package com.example.dblaws.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dblaws.model.CustomerSpend;
import com.example.dblaws.model.MonthlySales;
import com.example.dblaws.repository.OrderRepository;

/**
 * Law 1 - reports are access patterns too. Both queries push the grouping and
 * aggregation into the database rather than streaming rows into the app.
 */
@Service
public class StatsService {

	private final OrderRepository orderRepository;

	public StatsService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Transactional(readOnly = true)
	public List<MonthlySales> monthlySales() {
		return orderRepository.monthlySales();
	}

	@Transactional(readOnly = true)
	public List<CustomerSpend> topCustomers() {
		return orderRepository.topCustomersBySpend();
	}
}