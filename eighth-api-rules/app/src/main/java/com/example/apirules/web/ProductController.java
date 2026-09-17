package com.example.apirules.web;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.apirules.exception.NotFoundException;
import com.example.apirules.model.CreateProductRequest;
import com.example.apirules.model.PageResponse;
import com.example.apirules.model.Product;
import com.example.apirules.model.ProductResponse;
import com.example.apirules.model.ProductV1;
import com.example.apirules.model.UpdateProductRequest;
import com.example.apirules.service.ProductService;

import jakarta.validation.Valid;

/**
 * API Laws 1, 2, 3, 4, 6, 7 and 8 all converge here.
 *
 * Law 1 - the URL identifies a resource ({@code /products}), never an action
 *         ({@code /getProducts} or {@code /createProduct} would be wrong).
 * Law 2 - one convention everywhere: lowercase plural nouns in snake/kebab
 *         combined with camelCase JSON properties.
 * Law 3 - GET retrieve, POST create, PUT replace, PATCH partial update,
 *         DELETE remove. Each does exactly its HTTP meaning.
 * Law 4 - status codes are meaningful: 200/201/204 for success,
 *         400/404/409/422 for the different failure categories.
 * Law 6 - filtering/sorting/pagination live in query parameters, not the path.
 * Law 7 - endpoints evolve via Spring's native API versioning: version "1.0"
 *         and "1.1" handlers coexist for {@code GET /products/{id}}.
 * Law 8 - one response shape per concept, camelCase keys, ISO-8601 dates.
 */
@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping(version = "1.0+")
	public PageResponse<ProductResponse> list(
			@RequestParam(defaultValue = "") String category,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) Boolean inStock,
			@RequestParam(defaultValue = "name:asc") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		PageResponse<Product> result = productService.list(category, minPrice, maxPrice, inStock, sort, page, size);
		List<ProductResponse> content = result.content().stream().map(this::toResponse).toList();
		return new PageResponse<>(content, result.page(), result.size(), result.totalElements(), result.totalPages());
	}

	/** API Law 7 - the original contract, unchanged for v1.0 clients. */
	@GetMapping(path = "/{id}", version = "1.0")
	public ProductV1 getProductV1(@PathVariable Long id) {
		return productService.findById(id)
				.map(p -> new ProductV1(p.id(), p.name(), p.category(), p.price(), p.createdAt()))
				.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product " + id + " was not found"));
	}

	/** API Law 7 - v1.1 adds currency/stock, a non-breaking additive change. */
	@GetMapping(path = "/{id}", version = "1.1")
	public ProductResponse getProductV11(@PathVariable Long id) {
		return productService.findById(id)
				.map(this::toResponse)
				.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product " + id + " was not found"));
	}

	/** API Law 3/4 - POST creates. Returns 201 plus a Location header. */
	@PostMapping(version = "1.0+")
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request,
			UriComponentsBuilder uriBuilder) {
		Product product = productService.create(request);
		URI location = uriBuilder.path("/products/{id}").buildAndExpand(product.id()).toUri();
		return ResponseEntity.created(location).body(toResponse(product));
	}

	/** API Law 3 - PUT replaces the whole resource and is idempotent. */
	@PutMapping(path = "/{id}", version = "1.0+")
	public ProductResponse replace(@PathVariable Long id, @Valid @RequestBody CreateProductRequest request) {
		return toResponse(productService.replace(id, request));
	}

	/** API Law 3 - PATCH updates only the fields that were sent. */
	@PatchMapping(path = "/{id}", version = "1.0+")
	public ProductResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
		return toResponse(productService.update(id, request));
	}

	/** API Law 3/4 - DELETE removes the resource and answers 204, no body. */
	@DeleteMapping(path = "/{id}", version = "1.0+")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		productService.delete(id);
	}

	private ProductResponse toResponse(Product product) {
		return new ProductResponse(product.id(), product.name(), product.category(), product.price(),
				product.currency(), product.stock(), product.createdAt());
	}
}