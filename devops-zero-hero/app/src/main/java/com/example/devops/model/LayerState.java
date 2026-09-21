package com.example.devops.model;

/**
 * One Docker layer and whether a build reused its cache or rebuilt it.
 */
public record LayerState(String name, boolean cached, int seconds) {

	public boolean rebuilt() {
		return !cached();
	}
}