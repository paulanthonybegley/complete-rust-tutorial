package com.example.euind.model;

/**
 * A country Europe is pulling closer, and the mechanism used.
 */
public enum PartnerKind {
	ASSOCIATE("Associate member"),
	TRADE("Trade agreement"),
	OBSERVER("Euro-curious observer");

	private final String label;

	PartnerKind(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}