package com.example.dblaws.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.dblaws.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	Optional<Customer> findByEmail(String email);

	boolean existsByEmail(String email);
}