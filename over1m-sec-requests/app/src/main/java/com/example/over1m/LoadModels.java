package com.example.over1m;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Immutable records behind the 1M-RPS lab — the <em>measured</em> numbers from
 * the video "Let's Handle a Million Requests per Second, It's Scarier Than You
 * Think!" (2024). Every record is one station's measurement on the 12-core box;
 * a console (or a test) reads them, never computes them.
 */
public final class LoadModels {

	private LoadModels() {}

	/** One framework on station 1: express vs fastify vs the C++ one. */
	public record Framework(String name, long rps, String note, String dockerBuild) {}

	/** One autocannon load-step: connections × pipeline × workers → rps. */
	public record LoadStep(String name, int c, int p, int w, long rps, String note) {}

	/** One pm2 cluster-mode step: instances of the same app.js on the same box. */
	public record Pm2Step(int instances, long rps, String note) {}

	/** One NIC-bound step: payload bytes are what the 6 GB/s NIC counts. */
	public record NicBound(String payload, long bytes, long rps, String note) {}

	/** One Postgres write step: IOPS knob → writes/s → the monthly bill. */
	public record DbWrite(String setting, long iops, long writesPerSec, String monthlyCost, String note) {}

	/** One Redis cache jump: the path cold→warm, rps, what it did to DB load. */
	public record CacheJump(String path, long rps, long dbLoadPct, String note) {}

	/** One async-write station: 202 first, worker drains later, DB off the path. */
	public record AsyncWrite(String step, String mechanism, String returns, long rps, String note) {}

	/** One Redis-cluster arc point: nodes, combined rps, and the crossed-1M note. */
	public record ClusterArc(int nodes, long rps, String note) {}

	/** One UUID birthday-paradox station: uuids/s, collisions/s, the safe-note. */
	public record UuidArc(long uuidsPerSec, BigDecimal collisions, String note) {}
}
