package com.example.over1m;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Renders the whole "1M RPS, it's scarier than you think" console runbook —
 * every station in the same order the video measured it, printing only the
 * numbers from {@link SimLoad} (no math of its own). A bench() on this box
 * prints the exact measured arc: 1 thread → framework → pm2 → NIC → Postgres
 * IOPS → Redis reads → async 202s → the 30-node Redis cluster that finally
 * crosses 1,000,000 requests/sec.
 */
@Service
public class AutocannonService {

	// ---- station 1: which framework — the browser can't cross the wall -------

	/** The three measured frameworks, one thread each, same box. */
	public List<String> bench() {
		List<String> out = new ArrayList<>();
		out.add("$ autocannon -c 20 -d 20 -p 2 -m get http://localhost:3000/bench");
		out.add("");
		out.add("   framework     rps        factor vs express    note");
		for (var f : SimLoad.FRAMEWORKS.values()) {
			var factor = (f == SimLoad.FRAMEWORKS.get("express")
				? "1.0×" : String.format(Locale.ROOT, "%.1f×",
					f.rps() / (double) SimLoad.FRAMEWORKS.get("express").rps()));
			out.add(String.format(Locale.ROOT, "   %-13s  %,9d    %-20s  %s",
				f.name(), f.rps(), factor, f.dockerBuild()));
		}
		return out;
	}

	// ---- station 2: pm2 — more processes, same code, same box ---------------

	/** The pm2 arc: 1 → 4 → 12 instances of the same app on the same box. */
	public List<String> pm2() {
		List<String> out = new ArrayList<>();
		out.add("$ pm2 start app.js -i 1 && autocannon -c 20 -d 20 -p 2");
		out.add("");
		out.add("   instances      rps       factor vs 1        note");
		for (int i = 0; i < SimLoad.PM2_ARC.size(); i++) {
			var s = SimLoad.PM2_ARC.get(i);
			var factor = (i == 0 ? "1.0×" : String.format(Locale.ROOT, "%.1f×",
				s.rps() / (double) SimLoad.PM2_ARC.get(0).rps()));
			out.add(String.format(Locale.ROOT, "   %-6d     %,9d    %-17s  %s",
				s.instances(), s.rps(), factor, s.note()));
		}
		return out;
	}

	// ---- station 3: the NIC — 6 GB/s is the real, scary ceiling --------------

	/** The NIC-bound arc: payload size is the unit the NIC counts, not requests. */
	public List<String> nic() {
		List<String> out = new ArrayList<>();
		out.add("$ sar -n DEV 1 20   # eth0 rxkB/s is the number that caps you");
		out.add("");
		out.add("   payload        bytes        rps        note");
		for (var n : SimLoad.NIC_ARC) {
			out.add(String.format(Locale.ROOT, "   %-14s  %,9d   %,10d   %s",
				n.payload(), n.bytes(), n.rps(), n.note()));
		}
		return out;
	}

	// ---- station 4: Postgres writes — IOPS is the bill you pay monthly --------

	/** The DB-write arc: gp3 IOPS → writes/s → dollars every month. */
	public List<String> db() {
		List<String> out = new ArrayList<>();
		out.add("$ aws ec2 describe-volumes --filters Name=size,Values=2400");
		out.add("");
		out.add("   setting        iops       writes/s    monthly     note");
		for (var d : SimLoad.DB_ARC) {
			out.add(String.format(Locale.ROOT, "   %-14s  %,8d    %,8d   %-10s  %s",
				d.setting(), d.iops(), d.writesPerSec(), d.monthlyCost(), d.note()));
		}
		return out;
	}

	// ---- station 5: Redis cache — the read stops being a DB query -----------

	/** The cache arc: Redis GET/SET vs a DB miss — same path, different wall. */
	public List<String> cache() {
		List<String> out = new ArrayList<>();
		out.add("$ redis-cli --latency -h cache-arc.local");
		out.add("");
		out.add("   path               rps        db load    note");
		for (var c : SimLoad.CACHE_ARC) {
			out.add(String.format(Locale.ROOT, "   %-18s  %,10d    %3d%%      %s",
				c.path(), c.rps(), c.dbLoadPct(), c.note()));
		}
		return out;
	}

	// ---- station 6: async writes — 202 first, the drain comes later ----------

	/** The async-write arc: the request stops waiting on the write. */
	public List<String> async() {
		List<String> out = new ArrayList<>();
		out.add("$ curl -i -X POST -d '{}' http://localhost:3000/todos");
		out.add("");
		out.add("   step                  mechanism        returns      rps       note");
		for (var a : SimLoad.ASYNC_ARC) {
			out.add(String.format(Locale.ROOT, "   %-20s  %-16s  %-8s  %,9d    %s",
				a.step(), a.mechanism(), a.returns(), a.rps(), a.note()));
		}
		return out;
	}

	// ---- station 7: Redis cluster — the 1M wall finally falls ----------------

	/** The cluster arc: 1 → 2 → 4 → 15 → 30 nodes; 1M RPS is crossed inside. */
	public List<String> cluster() {
		List<String> out = new ArrayList<>();
		out.add("$ redis-cli --cluster create node1:7000 … node30:7000 --cluster-replicas 猛地0");
		out.add("");
		out.add("   nodes      rps        note");
		for (var c : SimLoad.CLUSTER_ARC) {
			out.add(String.format(Locale.ROOT, "   %-6d   %,10d   %s",
				c.nodes(), c.rps(), c.note()));
		}
		return out;
	}

	// ---- station 8: UUID birthday paradox — why 122 bits is safe -------------

	/** The UUID arc: 122 bits makes collision odds vanish — even at 1M/s. */
	public List<String> uuids() {
		List<String> out = new ArrayList<>();
		out.add("$ python3 - <<'EOF'  # birthday paradox at 1M UUIDs/s");
		out.add("");
		out.add("   uuids/s        collisions/s     note");
		for (var u : SimLoad.UUID_ARC) {
			out.add(String.format(Locale.ROOT, "   %,10d      %-18s  %s",
				u.uuidsPerSec(), u.collisions(), u.note()));
		}
		return out;
	}

	// ---- the whole lab, one make-style sweep over every station --------------

	/** Every station back to back — the console version of "the whole arc". */
	public List<String> runbook() {
		List<String> out = new ArrayList<>();
		out.add("1M RPS — the measured arc (every number is a SimLoad constant)");
		out.add("");
		out.add("station 1   which framework     " + bench().get(3));
		out.add("station 2   pm2 cluster mode    " + pm2().get(3));
		out.add("station 3   NIC ceiling         " + nic().get(3));
		out.add("station 4   Postgres writes     " + db().get(3));
		out.add("station 5   Redis reads         " + cache().get(3));
		out.add("station 6   async 202s          " + async().get(3));
		out.add("station 7   Redis cluster 1M    " + cluster().get(3));
		out.add("station 8   UUID birthday math  " + uuids().get(3));
		return out;
	}
}
