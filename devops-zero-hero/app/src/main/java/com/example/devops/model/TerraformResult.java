package com.example.devops.model;

import java.util.List;

/**
 * The plan/apply/destroy summary for the IaC sim.
 */
public record TerraformResult(List<ResourceChange> changes, int unchanged, String mode, String verdict) {

	public long add() {
		return changes.stream().filter(c -> c.action().equals("add")).count();
	}

	public long update() {
		return changes.stream().filter(c -> c.action().equals("change")).count();
	}

	public long replace() {
		return changes.stream().filter(c -> c.action().equals("replace")).count();
	}

	public long destroy() {
		return changes.stream().filter(c -> c.action().equals("destroy")).count();
	}
}