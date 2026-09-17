package com.example.dblaws.web;

import java.math.BigDecimal;
import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.dblaws.model.CreateProductRequest;
import com.example.dblaws.model.PageResponse;
import com.example.dblaws.model.ProductResponse;
import com.example.dblaws.service.ProductService;

import jakarta.validation.Valid;

/**
 * Catalog screen.
 *
 * Law 1 - the query parameters below are the access patterns of the catalogue
 * UI (filter by category/price/stock, sort, paginate); the database serves each
 * one with an index rather than a table scan.
 */
@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public PageResponse<ProductResponse> list(
			@RequestParam(required = false) String category,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) Boolean inStock,
			@RequestParam(defaultValue = "name:asc") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Page<ProductResponse> result = productService.search(category, minPrice, maxPrice, inStock, sort, page, size);
		return new PageResponse<>(result.getContent(), page, size, result.getTotalElements(), result.getTotalPages());
	}

	@GetMapping("/{id}")
	public ProductResponse get(@PathVariable Long id) {
		return productService.mustFind(id);
	}

	@PostMapping
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request,
			UriComponentsBuilder uriBuilder) {
		ProductResponse product = productService.create(request);
		URI location = uriBuilder.path("/products/{id}").buildAndExpand(product.id()).toUri();
		return ResponseEntity.status(HttpStatus.CREATED).location(location).body(product);
	}
}