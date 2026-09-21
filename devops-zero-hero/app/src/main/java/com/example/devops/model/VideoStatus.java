package com.example.devops.model;

/**
 * How far the video takes each concept: explained in the fundamentals,
 * practised while building the API, or part of the final shipped stack.
 */
public enum VideoStatus {

	EXPLAINED("Explained"),
	PRACTISED("In the build"),
	SHIPPED("Shipped");

	private final String label;

	VideoStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}