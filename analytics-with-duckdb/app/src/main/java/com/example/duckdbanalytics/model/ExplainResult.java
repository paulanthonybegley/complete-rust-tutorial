package com.example.duckdbanalytics.model;

/** A named analytical query plus its {@code EXPLAIN ANALYZE} plan text. */
public record ExplainResult(String name, String sql, String plan) {
}