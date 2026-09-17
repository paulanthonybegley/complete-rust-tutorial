package com.example.duckdbanalytics.model;

import java.math.BigDecimal;

/** One bucket of the revenue trend (bucket is a 'YYYY-MM-DD' string). */
public record RevenueTrendRow(String bucket, BigDecimal revenue) {
}