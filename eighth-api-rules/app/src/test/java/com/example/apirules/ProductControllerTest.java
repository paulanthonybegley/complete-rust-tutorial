package com.example.apirules;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private static final String V1 = "1.0";
	private static final String V11 = "1.1";

	@Test
	void listProductsUsesConsistentPaginationEnvelope() throws Exception {
		mockMvc.perform(get("/products").header("API-Version", V11))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.content[0].name").isNotEmpty());
	}

	@Test
	void filtersProductsByQueryParameter() throws Exception {
		mockMvc.perform(get("/products")
						.param("category", "home")
						.header("API-Version", V11))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.content[*].category").value(everyItem(equalTo("home"))));
	}

	@Test
	void sortsAndPaginatesResults() throws Exception {
		mockMvc.perform(get("/products")
						.param("sort", "price:asc")
						.param("size", "2")
						.header("API-Version", V11))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size").value(2))
				.andExpect(jsonPath("$.totalElements").value(4))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	void getProductV1OmitsNewerFields() throws Exception {
		mockMvc.perform(get("/products/1").header("API-Version", V1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").isNumber())
				.andExpect(jsonPath("$.currency").doesNotExist())
				.andExpect(jsonPath("$.stock").doesNotExist());
	}

	@Test
	void getProductV11IncludesNewerFields() throws Exception {
		mockMvc.perform(get("/products/1").header("API-Version", V11))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currency").value("USD"));
	}

	@Test
	void missingApiVersionHeaderIsRejected() throws Exception {
		mockMvc.perform(get("/products/1"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createProductReturns201WithLocation() throws Exception {
		mockMvc.perform(post("/products")
						.header("API-Version", V11)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Notebook","category":"office","price":4.99,"currency":"USD","stock":200}"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/products/")))
				.andExpect(jsonPath("$.id").isNumber());
	}

	@Test
	void invalidCreateRequestReturns422WithFieldErrors() throws Exception {
		mockMvc.perform(post("/products")
						.header("API-Version", V11)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"","category":"office","price":-1,"currency":"U","stock":-5}"""))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.fieldErrors").isArray())
				.andExpect(jsonPath("$.fieldErrors[0].field").isNotEmpty());
	}

	@Test
	void missingProductReturnsConsistentErrorBody() throws Exception {
		mockMvc.perform(get("/products/999").header("API-Version", V11))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
				.andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.timestamp").isNotEmpty());
	}

	@Test
	void putReplacesWholeResource() throws Exception {
		mockMvc.perform(put("/products/1")
						.header("API-Version", V11)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Clean Code (Revised)","category":"books","price":42.99,"currency":"USD","stock":150}"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Clean Code (Revised)"))
				.andExpect(jsonPath("$.price").value(42.99));
	}

	@Test
	void patchUpdatesOnlyProvidedFields() throws Exception {
		mockMvc.perform(patch("/products/2")
						.header("API-Version", V11)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"price":21.00}"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Digital Clock"))
				.andExpect(jsonPath("$.currency").value("EUR"))
				.andExpect(jsonPath("$.price").value(21.00));
	}

	@Test
	void deleteRemovesResourceThenItIsGone() throws Exception {
		String location = mockMvc.perform(post("/products")
						.header("API-Version", V11)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Temp Item","category":"office","price":1.00,"currency":"USD","stock":5}"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getHeader("Location");

		String id = location.substring(location.lastIndexOf('/') + 1);

		mockMvc.perform(delete("/products/" + id).header("API-Version", V11))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/products/" + id).header("API-Version", V11))
				.andExpect(status().isNotFound());
	}
}