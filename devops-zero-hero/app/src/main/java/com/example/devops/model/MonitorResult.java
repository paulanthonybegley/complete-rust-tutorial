package com.example.devops.model;

/**
 * Health snapshot for the observability sim.
 */
public record MonitorResult(double uptimePct, int latencyMs, double errorsPct, boolean unhealthy, boolean autoHeal,
		boolean paged, String lastAlert, String verdict) {
}