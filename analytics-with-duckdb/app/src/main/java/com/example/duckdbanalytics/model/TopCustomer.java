package com.example.duckdbanalytics.model;

import java.math.BigDecimal;

/** Best customers by total spend (lesson 2: joins + aggregation). */
public record TopCustomer(Long id, String name, String country, BigDecimal spend) {
}