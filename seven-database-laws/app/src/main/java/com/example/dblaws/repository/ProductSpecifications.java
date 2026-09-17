package com.example.dblaws.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import com.example.dblaws.model.Product;

/**
 * Law 3 - builds exactly the WHERE clause the catalogue screen asked for. Each
 * predicate is added only for a non-null filter, and every column referenced
 * is indexed, so the database can pick an Index Scan instead of a Seq Scan.
 *
 * Keeping nulls out of the query is deliberate: Hibernate binds a null
 * parameter as bytea, which Postgres cannot lower() or cast to numeric.
 */
public final class ProductSpecifications {

	private ProductSpecifications() {
	}

	public static Specification<Product> withFilters(String category, BigDecimal minPrice, BigDecimal maxPrice,
			Boolean inStock) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (category != null) {
				predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
			}
			if (minPrice != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
			}
			if (maxPrice != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
			}
			if (inStock != null) {
				predicates.add(inStock
						? cb.greaterThan(root.get("stockQuantity"), 0)
						: cb.equal(root.get("stockQuantity"), 0));
			}
			return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}