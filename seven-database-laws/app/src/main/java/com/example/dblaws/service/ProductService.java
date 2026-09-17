package com.example.dblaws.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.dblaws.exception.NotFoundException;
import com.example.dblaws.model.CreateProductRequest;
import com.example.dblaws.model.Product;
import com.example.dblaws.model.ProductResponse;
import com.example.dblaws.repository.ProductRepository;
import com.example.dblaws.repository.ProductSpecifications;

@Service
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	/**
	 * Law 3 - the filter/sort/pagination options map onto one query built by
	 * {@link ProductSpecifications} so missing filters stay out of the WHERE
	 * clause and the database can pick an index plan instead of scanning the
	 * whole table for every request.
	 */
	public Page<ProductResponse> search(String category, BigDecimal minPrice, BigDecimal maxPrice,
			Boolean inStock, String sort, int page, int size) {
		Sort order = switch (sort == null ? "" : sort.toLowerCase()) {
			case "price:asc" -> Sort.by(Sort.Direction.ASC, "price");
			case "price:desc" -> Sort.by(Sort.Direction.DESC, "price");
			case "stock:asc" -> Sort.by(Sort.Direction.ASC, "stockQuantity");
			case "stock:desc" -> Sort.by(Sort.Direction.DESC, "stockQuantity");
			default -> Sort.by(Sort.Direction.ASC, "name");
		};
		Page<Product> result = productRepository.findAll(
				ProductSpecifications.withFilters(category, minPrice, maxPrice, inStock),
				PageRequest.of(page, size, order));
		return result.map(ProductResponse::from);
	}

	public ProductResponse mustFind(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product " + id + " was not found"));
		return ProductResponse.from(product);
	}

	public ProductResponse create(CreateProductRequest request) {
		Product saved = productRepository.save(new Product(request.name(), request.category(),
				request.price(), request.currency(), request.stockQuantity(), Instant.now()));
		return ProductResponse.from(saved);
	}
}