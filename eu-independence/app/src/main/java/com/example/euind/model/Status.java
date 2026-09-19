package com.example.euind.model;

/**
 * How far a proposal has travelled. Delivered = announced measures already in
 * motion; the further right, the more speculative the item in the speech.
 */
public enum Status {
	BLOCKED("Blocked"),
	PROPOSED("Proposed"),
	ANNOUNCED("Announced"),
	IN_PROGRESS("In progress"),
	DELIVERED("Delivered");

	private final String label;

	Status(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}