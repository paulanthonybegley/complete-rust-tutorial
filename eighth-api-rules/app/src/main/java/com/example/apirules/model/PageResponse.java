package com.example.apirules.model;

import java.util.List;

/**
 * API Law 8 - keep formats consistent.
 * Every collection endpoint returns this identical pagination envelope.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}