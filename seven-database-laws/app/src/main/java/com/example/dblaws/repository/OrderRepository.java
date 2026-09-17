package com.example.dblaws.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.dblaws.model.CustomerSpend;
import com.example.dblaws.model.MonthlySales;
import com.example.dblaws.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@EntityGraph(attributePaths = "items")
	Optional<Order> findWithItemsById(Long id);

	/**
	 * Law 1 - the "orders for a customer" screen is the most frequent read in
	 * the system. The index declared in {@code @Table(indexes = ...)} makes this
	 * query fast without a full table scan.
	 */
	@EntityGraph(attributePaths = "items")
	List<Order> findWithItemsByCustomer_IdOrderByCreatedAtDesc(Long customerId);

	/**
	 * Law 1 - the finance dashboard needs total sales grouped by month. Pushing
	 * the GROUP BY into the database avoids pulling every order row into Java
	 * just to sum it.
	 */
	@Query("""
			select new com.example.dblaws.model.MonthlySales(
				year(o.createdAt),
				month(o.createdAt),
				sum(o.totalAmount))
			from Order o
			group by year(o.createdAt), month(o.createdAt)
			order by year(o.createdAt), month(o.createdAt)
			""")
	List<MonthlySales> monthlySales();

	/**
	 * Law 1 - "top 10 customers by spend" is a common access pattern.
	 */
	@Query("""
			select new com.example.dblaws.model.CustomerSpend(
				c.id, c.name, sum(o.totalAmount))
			from Order o join o.customer c
			group by c.id, c.name
			order by sum(o.totalAmount) desc
			""")
	List<CustomerSpend> topCustomersBySpend();

	/**
	 * Law 1 - the order-list screen can optionally filter by status.
	 */
	@EntityGraph(attributePaths = "items")
	List<Order> findWithItemsByStatusOrderByCreatedAtDesc(Order.OrderStatus status);
}