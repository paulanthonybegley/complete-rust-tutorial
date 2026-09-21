package com.example.k8s;

import org.springframework.stereotype.Service;

import com.example.k8s.K8sModels.Pod;

/**
 * Resource accounting for the Requests & Limits station (0:44:56) and the
 * Monitoring station (0:41:43): QoS class assignment, node fit, throttling
 * and OOM warnings — computed exactly the way the scheduler and kubelet do,
 * but from the in-memory numbers.
 */
@Service
public class ResourceService {

	public record Fit(String nodeName, int nodeCpu, int nodeMem, int reqCpu, int reqMem,
			boolean fits, String note) {
	}

	public record Qos(int cpuReq, int cpuLim, int memReq, int memLim, int cpuUse, int memUse,
			String className, String why, String runtimeRisk) {
	}

	public Qos qos(Pod p) {
		boolean cpuGuaranteed = p.cpuReq() > 0 && p.cpuReq() == p.cpuLim();
		boolean memGuaranteed = p.memReq() > 0 && p.memReq() == p.memLim();
		String className;
		String why;
		if (cpuGuaranteed && memGuaranteed) {
			className = "Guaranteed";
			why = "every container sets requests that equal limits for both CPU and memory — the kubelet may evict nobody for this pod first only if it must";
		} else if (p.cpuReq() == 0 && p.memReq() == 0) {
			className = "BestEffort";
			why = "no requests or limits at all — the pod can use any spare capacity and is the first candidate for eviction under pressure";
		} else {
			className = "Burstable";
			why = "requests < limits (or only one dimension set) — the pod is guaranteed its requests and may burst up to its limits";
		}

		String risk = "normal";
		if (p.cpuUse() > p.cpuLim() && p.cpuLim() > 0) {
			risk = "CPU THROTTLED — usage " + p.cpuUse() + "m exceeds the " + p.cpuLim()
					+ "m limit, so the CFS quota throttles the container";
		} else if (p.memUse() >= p.memLim() && p.memLim() > 0) {
			risk = "OOM RISK — memory at " + p.memUse() + "Mi (~" + (int) (100.0 * p.memUse() / p.memLim())
					+ "% of the " + p.memLim() + "Mi limit); the kernel may OOMKill this container";
		}
		return new Qos(p.cpuReq(), p.cpuLim(), p.memReq(), p.memLim(), p.cpuUse(), p.memUse(),
				className, why, risk);
	}

	/** Does every pod's total request still fit the largest worker node? */
	public Fit fit() {
		int reqCpu = cluster.totalCpuRequests();
		int reqMem = cluster.totalMemRequests();
		var biggest = cluster.nodes().stream().max((a, b) -> Integer.compare(a.cpuMilli(), b.cpuMilli()))
				.orElse(null);
		if (biggest == null) {
			return new Fit("", 0, 0, reqCpu, reqMem, true, "no nodes to schedule onto");
		}
		boolean fits = reqCpu <= biggest.cpuMilli() && reqMem <= biggest.memMi();
		String note = fits
				? "The scheduler can pack all " + reqCpu + "m CPU / " + reqMem + "Mi memory of requests onto "
						+ biggest.name() + " (" + biggest.cpuMilli() + "m / " + biggest.memMi() + "Mi allocatable)"
				: "TOTAL requests overcommit " + biggest.name() + " — pods would stay Pending until nodes have room or requests shrink";
		return new Fit(biggest.name(), biggest.cpuMilli(), biggest.memMi(), reqCpu, reqMem, fits, note);
	}

	private final SimCluster cluster;

	public ResourceService(SimCluster cluster) {
		this.cluster = cluster;
	}
}