package com.example.euind.model;

/**
 * A candidate for the European Security Council builder in /lab. Regions feed
 * the balance score; population feeds the coverage score.
 */
public record CouncilMember(String id, String country, String flag, long population, Region region,
		boolean permanent, String rationale) {

	public boolean isSmall() {
		return population < 10_000_000;
	}

	public enum Region {
		EUROPE_WEST, EUROPE_SOUTH, EUROPE_EAST, EUROPE_NORTH, EUROPE_CENTRAL
	}
}