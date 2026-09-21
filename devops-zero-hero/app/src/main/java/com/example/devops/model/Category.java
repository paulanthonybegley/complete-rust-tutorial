package com.example.devops.model;

/**
 * The nine course themes; each concept card belongs to one.
 */
public enum Category {

	CULTURE("Culture"),
	VERSION_CONTROL("Version control"),
	PIPELINES("Pipelines"),
	CONTAINERS("Containers"),
	ORCHESTRATION("Orchestration"),
	IAC("Infrastructure as code"),
	OBSERVABILITY("Observability"),
	SECURITY("Security"),
	SHIP("Ship it");

	private final String label;

	Category(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}