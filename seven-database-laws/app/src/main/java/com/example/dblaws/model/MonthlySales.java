package com.example.dblaws.model;

import java.math.BigDecimal;

/**
 * Monthly sales aggregate (Law 1 - the reporting team reads "sales per period",
 * so the DB answers it with a GROUP BY instead of loading every order into the
 * application).
 */
public class MonthlySales {

	private final int year;
	private final int month;
	private final BigDecimal total;

	public MonthlySales(int year, int month, BigDecimal total) {
		this.year = year;
		this.month = month;
		this.total = total;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public BigDecimal getTotal() {
		return total;
	}
}