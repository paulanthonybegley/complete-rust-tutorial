package com.example.k8s;

import java.util.List;
import java.util.Map;

/**
 * Immutable records describing the simulated cluster. The cluster is an
 * in-memory teaching model: every record maps one-to-one to a resource you
 * would manipulate with real kubectl, but nothing is actually running anywhere.
 */
public final class K8sModels {

	private K8sModels() {
	}

	public record Container(String name, String image) {
	}

	/** phase, ready, restarts all mirror kubectl's STATUS/READY/RESTARTS columns. */
	public record Pod(String namespace, String name, String image, String phase, int restarts,
			boolean ready, int cpuReq, int cpuLim, int memReq, int memLim, int cpuUse, int memUse,
			long ageEpoch, List<Container> containers, List<String> labels, String volumeFile) {

		public String age(long now) {
			return ageText(now - ageEpoch);
		}

		public Pod withContainers(List<Container> other) {
			return new Pod(namespace, name, image, phase, restarts, ready, cpuReq, cpuLim,
					memReq, memLim, cpuUse, memUse, ageEpoch, other, labels, volumeFile);
		}
	}

	public record ReplicaSet(String namespace, String name, String owner, String image,
			int wanted, int ready) {
	}

	public record Deployment(String namespace, String name, String image, int replicas, int ready,
			int upToDate, int available, String strategy, int generation, int revision,
			String imagePrev, List<String> templateLabels) {
	}

	public record RolloutEntry(int revision, String changeCause, int replicas, int ready, String image) {
	}

	public record Svc(String namespace, String name, String type, String clusterIp,
			String externalIp, int port, int targetPort, int nodePort, String selector,
			List<String> endpoints, List<String> labels) {
	}

	public record ConfigMap(String namespace, String name, Map<String, String> data, long ageEpoch) {
		public String age(long now) {
			return ageText(now - ageEpoch);
		}
	}

	public record Secret(String namespace, String name, Map<String, String> data, long ageEpoch) {
		public String age(long now) {
			return ageText(now - ageEpoch);
		}
	}

	public record Pv(String name, int capacityGi, String storageClass, String accessMode,
			String phase, String claim) {
	}

	public record Pvc(String namespace, String name, int requestGi, String storageClass,
			String accessMode, String phase, String boundTo) {
	}

	public record NetPolicy(String namespace, String name, List<String> podSelector,
			List<String> policyTypes, List<String> ingressFrom, List<String> egressTo, int port) {
	}

	public record Node(String name, int cpuMilli, int memMi, int cpuUse, int memUse, String status) {
	}

	public record LogLine(int seq, String time, String container, String text) {
	}

	public record FsFile(String path, String size, String content) {
	}

	public record ProbeState(String endpoint, boolean startupDone, boolean ready, int restarts,
			String phase, String reason) {
	}

	// ------------------------------------------------------------------
	// helpers

	static String ageText(long seconds) {
		if (seconds < 60) {
			return seconds + "s";
		}
		if (seconds < 3600) {
			return seconds / 60 + "m";
		}
		if (seconds < 86400) {
			return seconds / 3600 + "h";
		}
		return seconds / 86400 + "d";
	}
}