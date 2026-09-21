package com.example.devops.service;

import org.springframework.stereotype.Service;

import com.example.devops.model.MonitorResult;

/**
 * Observability simulator: flip a pod unhealthy and watch uptime, latency and
 * error rate react — and the 3 a.m. page fire if nothing auto-heals it.
 */
@Service
public class MonitorService {

	public MonitorResult sample(boolean unhealthy, boolean autoHeal) {
		if (unhealthy && !autoHeal) {
			return new MonitorResult(99.4, 2100, 4.2, true, false, true,
					"PAGE: API_RULES · P95 latency 2100ms · error rate 4.2% · pod unready — unacked",
					"No auto-heal: the sick pod keeps taking traffic. This is your 3 a.m. page — metrics turned a "
							+ "threshold crossing into a human wake-up.");
		}
		if (unhealthy) {
			return new MonitorResult(99.9, 245, 0.3, true, true, false,
					"auto-heal: Deployment replaced pod 2 — P95 back to 245ms, no page fired",
					"Auto-heal enabled: the orchestrator restarted the sick pod before the alert threshold tripped — "
							+ "you slept through the night.");
		}
		return new MonitorResult(99.95, 142, 0.1, false, autoHeal, false,
				"steady state — no alert in the last 24h; 99.95% uptime",
				"Steady state: latency 142ms, error rate 0.1%. Monitoring gives you the number someone will page "
						+ "you about — before it reaches that number.");
	}
}