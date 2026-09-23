package com.example.over1m;

import java.util.Locale;
import java.util.Map;
import java.util.List;

/**
 * The reference data for the whole 1M-RPS lab — the console equivalent of
 * "the numbers Cododev actually measured" (his video "Let's Handle 1
 * Million Requests per Second, It's Scarier Than You Think!", 2024). This
 * module is the immutable "product underneath": a Spring-less library any
 * console (or test) can import.
 *
 * <p>Every {@code static} here is exactly one benchmark from the video map:
 * <ul>
 *   <li>{@link #FRAMEWORKS} — Express vs Fastify vs Cpeak, one thread each.</li>
 *   <li>{@link #PM2_ARC} — the same app under pm2 cluster mode, 1 → 12 processes.</li>
 *   <li>{@link #NIC_ARC} — the 6 GB/s NIC ceiling for 3 payload sizes.</li>
 *   <li>{@link #DB_ARC} — Postgres writes vs IOPS vs monthly cost.</li>
 *   <li>{@link #CACHE_ARC} — Redis: reads off the DB path, writes stay async.</li>
 *   <li>{@link #ASYNC_ARC} — Kafka worker: 202-first, drain later.</li>
 *   <li>{@link #CLUSTER_ARC} — Redis 1 → 30 nodes: the 1M RPS wall falls.</li>
 *   <li>{@link #UUID_ARC} — birthday-paradox maths that makes big UUIDs safe.</li>
 * </ul>
 */
public final class SimLoad {

	private SimLoad() {}

	// ---- station 1 (“which framework?” — one thread can’t cross the wall) ----

	/** Express — the baseline everyone starts with. */
	public static final LoadModels.Framework EXPRESS = new LoadModels.Framework(
		"Express",
		20_000,
		"one Node thread, no cluster — the video measured ~14-20k RPS",
		"docker run -p 3000:3000 over1m-express"
	);

	/** Fastify — the same one-Node-thread setup, ≈3.6× Express on this box. */
	public static final LoadModels.Framework FASTIFY = new LoadModels.Framework(
		"Fastify",
		66_000,
		"claimed 3.6× Express; measured here it's ~66k RPS in one thread",
		"docker run -p 3001:3001 over1m-fastify"
	);

	/** Cpeak — the no-framework C++ server from the video. */
	public static final LoadModels.Framework CPEAK = new LoadModels.Framework(
		"Cpeak",
		73_000,
		"a hand-written C++ server — framework overhead is mostly gone",
		"docker run -p 3002:3002 over1m-cpeak"
	);

	/** Console keys for the lab's `start` command. */
	public static final Map<String, LoadModels.Framework> FRAMEWORKS = new java.util.LinkedHashMap<>();
	static {
		FRAMEWORKS.put("express", EXPRESS);
		FRAMEWORKS.put("fastify", FASTIFY);
		FRAMEWORKS.put("cpeak", CPEAK);
	}

	// ---- station 2 (PM2 — more processes, same code, same box) --------------

	/** pm2 cluster-mode arc — the app.js does not change; the process count does. */
	public static final List<LoadModels.Pm2Step> PM2_ARC = List.of(
		new LoadModels.Pm2Step(1, 8_000, "one instance — a single thread behind the socket"),
		new LoadModels.Pm2Step(4, 24_000, "four processes now share the listen socket"),
		new LoadModels.Pm2Step(12, 42_000, "twelve processes ≈ 12× the single-instance RPS on this box")
	);

	// ---- station 3 (the NIC — 6 GB/s is the real, scary ceiling) ------------

	/** NIC-bound arc: the payload bytes are what the NIC counts, not requests. */
	public static final List<LoadModels.NicBound> NIC_ARC = List.of(
		new LoadModels.NicBound("empty body (204/keep-alive)", 0L, 99_000L, "the ~100k RPS ceiling with the NIC mostly idle — CPU not the wall"),
		new LoadModels.NicBound("30 KB JSON payload", 30_000L, 200_000L, "payload bytes are what the NIC counts, not requests"),
		new LoadModels.NicBound("64 KB payload", 64_000L, 93_000L, "bigger every time — requests fit less often")
	);

	// ---- station 4 (Postgres — IOPS buy you writes/s, and money) -------------

	/** Postgres write-throughput arc: IOPS knob vs writes/s vs the monthly bill. */
	public static final List<LoadModels.DbWrite> DB_ARC = List.of(
		new LoadModels.DbWrite("gp3 · 3000 IOPS", 3_000L, 35_000L, "$110 /mo", "base EBS — the write ceiling as measured"),
		new LoadModels.DbWrite("gp3 · 6000 IOPS", 6_000L, 66_000L, "$185 /mo", "double the IOPS ≈ double the writes/s"),
		new LoadModels.DbWrite("io2 · 32 000 IOPS", 32_000L, 168_000L, "$1 200 /mo", "barely below the video's NIC-bound 200k write idea — and money is now the wall")
	);

	// ---- station 5 (Redis cache — reads move off the DB path) ----------------

	/** Redis cache arc: "request → Redis" replaces "request → Postgres". */
	public static final List<LoadModels.CacheJump> CACHE_ARC = List.of(
		new LoadModels.CacheJump("request → Redis GET", 300_000L, 0L, "hot reads never touch Postgres"),
		new LoadModels.CacheJump("request → Redis SET", 100_000L, 35_000L, "writes still async — 202 first, drain later"),
		new LoadModels.CacheJump("request → DB (miss)", 55_000L, 100L, "only the miss path still pays Postgres write IOPS")
	);

	// ---- station 6 (Kafka — the request stops waiting on the write) ----------

	/** Async-write arc: 202 Accepted is returned before the DB ever sees nothing. */
	public static final List<LoadModels.AsyncWrite> ASYNC_ARC = List.of(
		new LoadModels.AsyncWrite("POST /todos", "@RequestMapping", "202 Accepted", 35_000L, "the response is sent before the write lands"),
		new LoadModels.AsyncWrite("POST /todos (queued)", "Kafka + worker", "202 Accepted", 35_000L, "worker batch-inserts; the request never blocks"),
		new LoadModels.AsyncWrite("GET /todos (cached)", "Redis GET", "200 OK", 300_000L, "reads stay off Postgres completely")
	);

	// ---- station 7 (Redis cluster — 1M RPS finally clears) -------------------

	/** Redis cluster arc 1 → 2 → 4 → 15 → 30 nodes; 1M RPS is crossed inside. */
	public static final List<LoadModels.ClusterArc> CLUSTER_ARC = List.of(
		new LoadModels.ClusterArc(1, 371_000L, "single Redis node — ~371k RPS reads"),
		new LoadModels.ClusterArc(2, 735_000L, "two nodes — keys sharded across both"),
		new LoadModels.ClusterArc(4, 1_412_000L, "four nodes already cross 1M reads/s"),
		new LoadModels.ClusterArc(15, 3_140_000L, "fifteen nodes — linear up to the NIC of each"),
		new LoadModels.ClusterArc(30, 29_800_000L, "thirty nodes: the video's actual final cluster")
	);

	// ---- station 8 (UUID birthday paradox — why 122 bits is enough) ----------

	/** The birthday-paradox maths that makes a sharded key safe at 1M RPS. */
	public static final List<LoadModels.UuidArc> UUID_ARC = List.of(
		new LoadModels.UuidArc(1_000_000L, java.math.BigDecimal.valueOf(6L), "1M UUIDs/s — 122 bits of entropy, collision odds are the birthday paradox"),
		new LoadModels.UuidArc(1_000_000L, java.math.BigDecimal.ONE, "same rate, 122 bits: one collision is expected only every ~10^16 of them")
	);
}
