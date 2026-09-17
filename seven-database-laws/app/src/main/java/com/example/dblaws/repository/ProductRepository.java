package com.example.dblaws.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.dblaws.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	/**
	 * Law 1 - one combined query for the catalogue screen instead of three
	 * separate lookups. Filters are built indirectly through
	 * {@link ProductSpecifications}: each predicate is added only when its
	 * filter is present, so no null parameter is ever bound (Hibernate defaults
	 * a null bind to bytea and Postgres rejects lower(bytea)/numeric casts).
	 */

	/**
	 * Law 2 - a single atomic UPDATE that decrements stock only when enough is
	 * available. Because it runs inside the same transaction as the order
	 * insert, a failure on a later line rolls back every earlier decrement.
	 */
	@Modifying(clearAutomatically = true)
	@Query("""
			update Product p set p.stockQuantity = p.stockQuantity - :quantity
			where p.id = :productId and p.stockQuantity >= :quantity
			""")
	int decrementStockIfAvailable(@Param("productId") Long productId, @Param("quantity") int quantity);
}