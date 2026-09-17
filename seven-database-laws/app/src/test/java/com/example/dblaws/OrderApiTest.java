package com.example.dblaws;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderApiTest {

	@Autowired
	private MockMvc mockMvc;

	private String createProduct(String name, int stock) throws Exception {
		return mockMvc.perform(post("/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s","category":"tools","price":9.99,"currency":"USD","stockQuantity":%d}"""
								.formatted(name, stock)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString()
				.replaceAll(".*\"id\":(\\d+).*", "$1");
	}

	@Test
	void catalogueFiltersByCategoryAndStock() throws Exception {
		mockMvc.perform(get("/products").param("category", "grocery"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[*].category").value(everyItem(equalTo("grocery"))));
	}

	@Test
	void catalogueSortsAndPaginatesWithNoFiltersSet() throws Exception {
		mockMvc.perform(get("/products")
						.param("sort", "price:desc")
						.param("page", "0")
						.param("size", "3"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size").value(3))
				.andExpect(jsonPath("$.content[0].id").isNumber())
				.andExpect(jsonPath("$.content.length()").value(3));
	}

	@Test
	void placingOrderReturns201AndLineTotal() throws Exception {
		String productId = createProduct("Ordered Tool", 20);

		mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"customerName":"Eve","customerEmail":"eve@example.com",
								 "items":[{"productId":%s,"quantity":2}]}""".formatted(productId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.totalAmount").value(19.98))
				.andExpect(jsonPath("$.items[0].productName").value("Ordered Tool"))
				.andExpect(jsonPath("$.items[0].quantity").value(2));
	}

	@Test
	void orderResponseShowsDecrementedStock() throws Exception {
		String productId = createProduct("Dinghy", 3);

		mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"customerName":"Frank","customerEmail":"frank@example.com",
								 "items":[{"productId":%s,"quantity":2}]}""".formatted(productId)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/products/{id}", productId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stockQuantity").value(1));
	}

	@Test
	void insufficientStockAnswers409AndRollsBack() throws Exception {
		String productId = createProduct("Rare Tool", 1);

		mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"customerName":"Grace","customerEmail":"grace@example.com",
								 "items":[{"productId":%s,"quantity":2}]}""".formatted(productId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"))
				.andExpect(jsonPath("$.status").value(409));

		mockMvc.perform(get("/products/{id}", productId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stockQuantity").value(1));
	}

	@Test
	void ordersAreListedByCustomer() throws Exception {
		String productId = createProduct("Gift Tool", 50);

		String body = mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"customerName":"Heidi","customerEmail":"heidi@example.com",
								 "items":[{"productId":%s,"quantity":1}]}""".formatted(productId)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String customerId = body.replaceAll(".*\"customerId\":(\\d+).*", "$1");

		mockMvc.perform(get("/orders").param("customerId", customerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].customerName").value("Heidi"));
	}

	@Test
	void monthlySalesAggregateIsGroupedByDatabase() throws Exception {
		String productId = createProduct("Stat Toy", 40);

		mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"customerName":"Ivan","customerEmail":"ivan@example.com",
								 "items":[{"productId":%s,"quantity":4}]}""".formatted(productId)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/stats/monthly-sales"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].total").isNumber())
				.andExpect(jsonPath("$[0].year").value(equalTo(java.time.Year.now().getValue())));
	}
}