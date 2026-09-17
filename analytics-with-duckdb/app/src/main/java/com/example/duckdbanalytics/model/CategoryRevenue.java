package com.example.duckdbanalytics.model;

import java.math.BigDecimal;

/** Revenue summed per product category (lesson 4: columnar GROUP BY). */
public record CategoryRevenue(String category, BigDecimal revenue) {
}