package com.example.apirules;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private static final String V11 = "1.1";

	@Test
	void createOrderReturns201AndIsReadable() throws Exception {
		String location = mockMvc.perform(post("/orders")
						.header("API-Version", V11)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"customerName":"Carol","items":[{"productId":2,"quantity":1}]}"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/orders/")))
				.andReturn().getResponse().getHeader("Location");

		mockMvc.perform(get(location).header("API-Version", V11))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerName").value("Carol"))
				.andExpect(jsonPath("$.status").value("CREATED"))
				.andExpect(jsonPath("$.items[0].quantity").value(1));
	}

	@Test
	void orderWithInsufficientStockConflicts() throws Exception {
		// Desk Lamp (id 3) is seeded with zero stock.
		mockMvc.perform(post("/orders")
						.header("API-Version", V11)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"customerName":"Dave","items":[{"productId":3,"quantity":1}]}"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
	}

	@Test
	void orderItemsIsANestedResource() throws Exception {
		mockMvc.perform(get("/orders/1/items").header("API-Version", V11))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].productName").value("Clean Code"))
				.andExpect(jsonPath("$[0].unitPrice").value(39.99));
	}

	@Test
	void validStatusTransitionSucceeds() throws Exception {
		// Seeded order 2 starts as CONFIRMED; CONFIRMED -> SHIPPED is legal.
		mockMvc.perform(patch("/orders/2/status")
						.header("API-Version", V11)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"SHIPPED"}"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SHIPPED"));
	}

	@Test
	void invalidStatusTransitionConflicts() throws Exception {
		// Seeded order 1 starts as CREATED; CREATED -> SHIPPED is illegal.
		mockMvc.perform(patch("/orders/1/status")
						.header("API-Version", V11)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"SHIPPED"}"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
	}

	@Test
	void unknownStatusIsBadRequest() throws Exception {
		mockMvc.perform(patch("/orders/1/status")
						.header("API-Version", V11)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"FROZEN"}"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_STATUS"));
	}
}