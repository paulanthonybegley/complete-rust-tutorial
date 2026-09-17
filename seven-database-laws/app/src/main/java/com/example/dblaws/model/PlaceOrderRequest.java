package com.example.dblaws.model;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

public record PlaceOrderRequest(
		@NotBlank String customerName,
		@NotBlank @Email String customerEmail,
		@NotEmpty List<Item> items) {

	public record Item(@Positive long productId, @Positive int quantity) {
	}
}