package com.example.apirules.model;

import jakarta.validation.constraints.NotBlank;

public record StatusUpdateRequest(@NotBlank(message = "status is required") String status) {
}