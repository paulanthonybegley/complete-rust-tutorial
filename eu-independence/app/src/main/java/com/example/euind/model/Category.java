package com.example.euind.model;

/**
 * The nine theme groups that come out of the video. Each maps to an app
 * screen and to one or more lessons in lesson_plans.md.
 */
public enum Category {
	SPEECH("The speech", "sot"),
	ECONOMY("Economy & speed", "econ"),
	RAW_MATERIALS("Critical materials", "raw"),
	DEFENCE("Defence & security", "def"),
	ENERGY("Energy & grids", "ene"),
	CLIMATE("Climate", "cli"),
	AI("AI & tech", "ai"),
	PARTNERSHIPS("Partnerships & trade", "par"),
	MEDIA("Media & sources", "med");

	private final String label;
	private final String slug;

	Category(String label, String slug) {
		this.label = label;
		this.slug = slug;
	}

	public String getLabel() {
		return label;
	}

	public String getSlug() {
		return slug;
	}
}