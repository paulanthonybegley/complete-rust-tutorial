package com.example.duckdbanalytics.model;

/** Distribution of orders by status. */
public record StatusCount(String status, Long count) {
}