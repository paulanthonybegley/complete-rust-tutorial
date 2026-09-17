package com.example.dblaws.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dblaws.exception.NotFoundException;
import com.example.dblaws.model.Customer;
import com.example.dblaws.model.CustomerResponse;
import com.example.dblaws.repository.CustomerRepository;

@Service
public class CustomerService {

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional(readOnly = true)
	public List<CustomerResponse> list() {
		return customerRepository.findAll().stream().map(CustomerResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public Customer mustFind(Long id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer " + id + " was not found"));
	}
}