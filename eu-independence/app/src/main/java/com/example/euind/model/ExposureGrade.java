package com.example.euind.model;

/**
 * How exposed Europe is on one critical material, derived in SimService from
 * the single-supplier concentration the learner drags.
 */
public enum ExposureGrade {
	MANAGED("MANAGED", "Multi-supplier, short disruption window"),
	ELEVATED("ELEVATED", "One dominant supplier, days of stock"),
	SEVERE("SEVERE", "Near-monopoly supplier, hours of stock"),
	CRITICAL("CRITICAL", "Single point of failure for industry");

	private final String label;
	private final String reading;

	ExposureGrade(String label, String reading) {
		this.label = label;
		this.reading = reading;
	}

	public String getLabel() {
		return label;
	}

	public String getReading() {
		return reading;
	}
}