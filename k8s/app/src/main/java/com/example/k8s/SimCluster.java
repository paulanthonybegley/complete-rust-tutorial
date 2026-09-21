package com.example.k8s;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.example.k8s.K8sModels.ConfigMap;
import com.example.k8s.K8sModels.Container;
import com.example.k8s.K8sModels.Deployment;
import com.example.k8s.K8sModels.FsFile;
import com.example.k8s.K8sModels.LogLine;
import com.example.k8s.K8sModels.NetPolicy;
import com.example.k8s.K8sModels.Node;
import com.example.k8s.K8sModels.Pod;
import com.example.k8s.K8sModels.ProbeState;
import com.example.k8s.K8sModels.Pv;
import com.example.k8s.K8sModels.Pvc;
import com.example.k8s.K8sModels.ReplicaSet;
import com.example.k8s.K8sModels.Secret;
import com.example.k8s.K8sModels.Svc;

/**
 * The simulated cluster. One instance for the whole app (a classroom tool),
 * seeded with the baseline the exercises build on. Nothing here talks to a
 * real cluster, node, registry or cloud — every object exists only in memory
 * so a learner can safely type kubectl commands that would be destructive on
 * a real cluster.
 */
@Component
public class SimCluster {

	public static final String NS_DEFAULT = "default";
	public static final String NS_SYSTEM = "kube-system";
	public static final String NS_PUBLIC = "kube-public";

	/** Files written under this path survive pod deletion — the PVC lesson. */
	public static final String PERSISTENT_PREFIX = "/data/persistent/";

	private final Map<String, FsFile> persistent = new TreeMap<>();

	public Map<String, FsFile> persistentFiles() {
		return Map.copyOf(persistent);
	}

	/** ReplicaSet names walk the Deployment->ReplicaSet->Pod tree: <deploy>-rs-<hash>. */
	public static final String RS_TOKEN = "-rs-";

	private final java.util.Set<String> namespaces = new java.util.LinkedHashSet<>();

	public void createNamespace(String name) {
		namespaces.add(name);
	}

	private final AtomicInteger podSeq = new AtomicInteger(1000);
	private final AtomicInteger logSeq = new AtomicInteger(1);

	private final Map<String, Pod> pods = new LinkedHashMap<>();
	private final Map<String, ReplicaSet> rsets = new LinkedHashMap<>();
	private final Map<String, Deployment> deployments = new LinkedHashMap<>();
	private final Map<String, Svc> services = new LinkedHashMap<>();
	private final Map<String, ConfigMap> configMaps = new LinkedHashMap<>();
	private final Map<String, Secret> secrets = new LinkedHashMap<>();
	private final Map<String, Pvc> pvcs = new LinkedHashMap<>();
	private final Map<String, Pv> pvs = new LinkedHashMap<>();
	private final Map<String, NetPolicy> netPolicies = new LinkedHashMap<>();
	private final Map<String, Node> nodes = new LinkedHashMap<>();
	private final Map<String, List<LogLine>> logs = new LinkedHashMap<>();
	private final Map<String, Map<String, FsFile>> files = new LinkedHashMap<>();
	private final Map<String, String> probeScenario = new LinkedHashMap<>();
	private final Map<String, Integer> probeStartup = new LinkedHashMap<>();
	private final Map<String, String> probeLivenessPath = new LinkedHashMap<>();
	private final Map<String, Boolean> probeReadinessOn = new LinkedHashMap<>();
	private final Map<String, Integer> podCpuUse = new LinkedHashMap<>();
	private final Map<String, Integer> podMemUse = new LinkedHashMap<>();

	public SimCluster() {
		namespaces.add(NS_PUBLIC);
		namespaces.add(NS_SYSTEM);
		namespaces.add(NS_DEFAULT);

		nodes.put("minikube", new Node("minikube", 2000, 2048, 700, 900, "Ready"));
		nodes.put("worker-1", new Node("worker-1", 4000, 4096, 1600, 2100, "Ready"));
		nodes.put("worker-2", new Node("worker-2", 4000, 4096, 1200, 1500, "Ready"));

		systemPod("kube-apiserver", "registry.k8s.io/kube-apiserver:v1.30");
		systemPod("kube-scheduler", "registry.k8s.io/kube-scheduler:v1.30");
		systemPod("kube-controller-manager", "registry.k8s.io/kube-controller-manager:v1.30");
		systemPod("etcd", "registry.k8s.io/etcd:3.5.12");

		String currentRs = "web-rs-77b7c6d8f9";
		String prevRs = "web-rs-5b89c2a1d4";
		rsets.put(currentRs, new ReplicaSet(NS_DEFAULT, currentRs, "web", "nginx:1.25", 3, 3));
		rsets.put(prevRs, new ReplicaSet(NS_DEFAULT, prevRs, "web", "nginx:1.24", 0, 0));
		deployments.put("web", new Deployment(NS_DEFAULT, "web", "nginx:1.25", 3, 3, 3, 3,
				"RollingUpdate", 2, 2, "nginx:1.24", List.of("app=web", "tier=frontend")));
		for (int i = 1; i <= 3; i++) {
			putPod(runningPod(currentRs + "-x" + i, "nginx:1.25",
					List.of("app=web", "tier=frontend", "pod-template-hash=" + currentRs), 140, 160));
		}

		putPod(runningPod("api-rs-a1", "api:1.0",
				List.of("app=api", "pod-template-hash=api-rs-m1"), 60, 70)
				.withContainers(List.of(new Container("api", "api:1.0"), new Container("sidecar", "sidecar:1.0"))));
		seedFiles("api-rs-a1");
		appendLogs("api-rs-a1", "api", List.of(
				"GET /healthz 200 1ms",
				"GET /orders 200 4ms",
				"POST /orders 201 12ms",
				"GET /healthz 200 1ms",
				"WARN slow query took 310ms"));
		appendLogs("api-rs-a1", "sidecar", List.of(
				"sidecar: tailing /var/log/api.log",
				"sidecar: rotated log at 2026-09-21T09:30:00Z"));

		putPod(runningPod("flaky", "busybox:1.36", List.of("app=flaky"), 30, 40));

		services.put("web-clusterip", new Svc(NS_DEFAULT, "web-clusterip", "ClusterIP", "10.96.0.10", "<none>",
				80, 8080, 0, "app=web",
				List.of("10.0.0.21:8080", "10.0.0.22:8080", "10.0.0.23:8080"), List.of("app=web")));
		services.put("web-nodeport", new Svc(NS_DEFAULT, "web-nodeport", "NodePort", "10.96.0.11", "<none>",
				80, 8080, 30080, "app=web",
				List.of("10.0.0.21:8080", "10.0.0.22:8080", "10.0.0.23:8080"), List.of("app=web")));
		services.put("web-lb", new Svc(NS_DEFAULT, "web-lb", "LoadBalancer", "10.96.0.12", "192.168.1.10",
				80, 8080, 30080, "app=web",
				List.of("10.0.0.21:8080", "10.0.0.22:8080", "10.0.0.23:8080"), List.of("app=web")));

		configMaps.put("web-config", new ConfigMap(NS_DEFAULT, "web-config",
				Map.of("APP_COLOR", "blue", "LOG_LEVEL", "info", "GREETING", "Hello from ConfigMap"), now()));
		secrets.put("web-secret", new Secret(NS_DEFAULT, "web-secret",
				Map.of("DB_PASSWORD", "s3cr3t!", "API_KEY", "k8s-abc123"), now()));

		pvs.put("pv-web", new Pv("pv-web", 5, "standard", "RWO", "Bound", "default/web-pvc"));
		pvs.put("pv-mysql", new Pv("pv-mysql", 10, "local-ssd", "RWO", "Available", ""));
		pvs.put("pv-shared", new Pv("pv-shared", 1024, "nfs-storage", "RWX", "Available", ""));
		pvcs.put("web-pvc", new Pvc(NS_DEFAULT, "web-pvc", 5, "standard", "RWO", "Bound", "pv-web"));

		netPolicies.put("default-deny", new NetPolicy(NS_DEFAULT, "default-deny", List.of("<all pods>"),
				List.of("Ingress", "Egress"), List.of("<none>"), List.of("<none>"), 0));
		netPolicies.put("allow-db-to-web", new NetPolicy(NS_DEFAULT, "allow-db-to-web", List.of("app=web"),
				List.of("Ingress"), List.of("app=db"), List.of("<none>"), 8080));

		String[] webKeys = { key(NS_DEFAULT, currentRs + "-x1"), key(NS_DEFAULT, currentRs + "-x2"),
				key(NS_DEFAULT, currentRs + "-x3") };
		for (String k : webKeys) {
			probeScenario.put(k, "healthy");
			probeStartup.put(k, 5);
			probeLivenessPath.put(k, "/health");
			probeReadinessOn.put(k, true);
		}
	}

	// ------------------------------------------------------------------
	// seeding helpers

	private void systemPod(String name, String image) {
		String[] bits = { NS_SYSTEM, name };
		pods.put(key(NS_SYSTEM, name), new Pod(NS_SYSTEM, name, image, "Running", 0, true,
				50, 100, 32, 64, 30, 40, now(), List.of(new Container(name, image)), List.of(), null));
	}

	private Pod runningPod(String name, String image, List<String> labels, int cpuUse, int memUse) {
		return new Pod(NS_DEFAULT, name, image, "Running", 0, true,
				250, 500, 128, 256, cpuUse, memUse, now(),
				List.of(new Container("nginx", image)), labels, null);
	}

	private void seedFiles(String pod) {
		Map<String, FsFile> map = new LinkedHashMap<>();
		map.put("/app/app.jar", new FsFile("/app/app.jar", "18.2 MB", null));
		map.put("/app/config/app.properties", new FsFile("/app/config/app.properties", "96 B",
				"APP_COLOR=blue\nLOG_LEVEL=info\nGREETING=Hello from ConfigMap"));
		map.put("/data/.keep", new FsFile("/data/.keep", "0 B", ""));
		files.put(key(NS_DEFAULT, pod), map);
	}

	public String stamp(int seq) {
		return "2026-09-21T12:00:" + String.format("%02d", seq % 60);
	}

	// ------------------------------------------------------------------
	// low-level

	public static String key(String ns, String name) {
		return ns + "/" + name;
	}

	/** Returns ns/name of the stored pod. */
	public String putPod(Pod pod) {
		pods.put(key(pod.namespace(), pod.name()), pod);
		return key(pod.namespace(), pod.name());
	}

	public void appendLogs(String podName, String container, List<String> lines) {
		appendLogs(NS_DEFAULT, podName, container, lines);
	}

	public void appendLogs(String ns, String podName, String container, List<String> lines) {
		List<LogLine> buf = logs.computeIfAbsent(key(ns, podName), k -> new ArrayList<>());
		for (String line : lines) {
			buf.add(new LogLine(logSeq.getAndIncrement(), stamp(logSeq.get()), container, line));
		}
	}

	public void appendLog(String ns, String podName, String container, String text) {
		appendLogs(ns, podName, container, List.of(text));
	}

	public long now() {
		return System.currentTimeMillis() / 1000;
	}

	// ------------------------------------------------------------------
	// views (fresh, sorted — safe for templates and tests)

	public List<String> namespaces() {
		return namespaces.stream().sorted().toList();
	}

	// public mutators used by the kubectl console and the Manifest lab

	public void putDeployment(Deployment d) {
		deployments.put(d.name(), d);
	}

	public void putReplicaSet(ReplicaSet r) {
		rsets.put(r.name(), r);
	}

	public void putSvc(Svc s) {
		services.put(s.name(), s);
	}

	public void putConfigMap(ConfigMap c) {
		configMaps.put(c.name(), c);
	}

	public void putSecret(Secret c) {
		secrets.put(c.name(), c);
	}

	public void putPvc(Pvc c) {
		pvcs.put(c.name(), c);
	}

	public void putNetPol(NetPolicy p) {
		netPolicies.put(p.name(), p);
	}

	public void removeDeployment(String name) {
		Deployment d = deployments.remove(name);
		if (d == null) {
			return;
		}
		for (ReplicaSet rs : allRsFor(d)) {
			for (Pod p : podsForReplicaSet(rs)) {
				removePod(p.namespace(), p.name());
			}
			rsets.remove(rs.name());
		}
	}

	public void removeSvc(String name) {
		services.remove(name);
	}

	public List<Pod> pods() {
		return pods.values().stream().sorted(Comparator.comparing(Pod::namespace).thenComparing(Pod::name)).toList();
	}

	public List<Pod> pods(String ns) {
		return pods.values().stream().filter(p -> p.namespace().equals(ns))
				.sorted(Comparator.comparing(Pod::name)).toList();
	}

	public List<Pod> podsMatchingSelector(Map<String, String> selector) {
		return pods().stream().filter(p -> matches(p.labels(), selector)).toList();
	}

	public static boolean matches(List<String> labels, Map<String, String> selector) {
		for (Map.Entry<String, String> e : selector.entrySet()) {
			String want = e.getKey() + "=" + e.getValue();
			if (labels.stream().noneMatch(l -> l.equals(want))) {
				return false;
			}
		}
		return true;
	}

	public Map<String, String> parseSelector(String raw) {
		Map<String, String> out = new LinkedHashMap<>();
		if (raw == null || raw.isBlank()) {
			return out;
		}
		for (String pair : raw.split(",")) {
			int eq = pair.indexOf('=');
			if (eq > 0) {
				out.put(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
			}
		}
		return out;
	}

	public List<ReplicaSet> replicaSets() {
		return rsets.values().stream().sorted(Comparator.comparing(ReplicaSet::name)).toList();
	}

	public List<Deployment> deployments() {
		return deployments.values().stream().sorted(Comparator.comparing(Deployment::name)).toList();
	}

	public Deployment deployment(String name) {
		return deployments.get(name);
	}

	public List<Svc> services() {
		return services.values().stream().sorted(Comparator.comparing(Svc::name)).toList();
	}

	public Svc service(String name) {
		return services.get(name);
	}

	public List<ConfigMap> configMaps() {
		return configMaps.values().stream().sorted(Comparator.comparing(ConfigMap::name)).toList();
	}

	public List<Secret> secrets() {
		return secrets.values().stream().sorted(Comparator.comparing(Secret::name)).toList();
	}

	public List<Pvc> pvcs() {
		return pvcs.values().stream().sorted(Comparator.comparing(Pvc::name)).toList();
	}

	public List<Pv> pvs() {
		return pvs.values().stream().sorted(Comparator.comparing(Pv::name)).toList();
	}

	public List<NetPolicy> netPolicies() {
		return netPolicies.values().stream().sorted(Comparator.comparing(NetPolicy::name)).toList();
	}

	public List<Node> nodes() {
		return nodes.values().stream().sorted(Comparator.comparing(Node::name)).toList();
	}

	public List<LogLine> logs(String ns, String pod) {
		return List.copyOf(logs.getOrDefault(key(ns, pod), List.of()));
	}

	public Map<String, FsFile> files(String ns, String pod) {
		Map<String, FsFile> merged = new java.util.TreeMap<>(persistent);
		merged.putAll(files.getOrDefault(key(ns, pod), Map.of()));
		return merged;
	}

	public void writeFile(String ns, String pod, String path, String content) {
		String size = content == null ? "0 B" : content.getBytes().length + " B";
		if (path.startsWith(PERSISTENT_PREFIX)) {
			persistent.put(path, new FsFile(path, size, content));
			return;
		}
		Map<String, FsFile> map = files.getOrDefault(key(ns, pod), Map.of());
		Map<String, FsFile> copy = new LinkedHashMap<>(map);
		copy.put(path, new FsFile(path, size, content));
		files.put(key(ns, pod), copy);
	}

	public List<String> containerNames(Pod pod) {
		return pod.containers().stream().map(Container::name).toList();
	}

	// ------------------------------------------------------------------
	// probes

	public void setProbe(String podKey, String scenario, int startupSec, String path, boolean readiness) {
		probeScenario.put(podKey, scenario);
		probeStartup.put(podKey, startupSec);
		probeLivenessPath.put(podKey, path);
		probeReadinessOn.put(podKey, readiness);
	}

	public ProbeState probeState(Pod pod) {
		String k = key(pod.namespace(), pod.name());
		String scenario = probeScenario.getOrDefault(k, "healthy");
		int startup = probeStartup.getOrDefault(k, 0);
		boolean readiness = probeReadinessOn.getOrDefault(k, false);
		String path = probeLivenessPath.getOrDefault(k, "/health");

		String phase = pod.phase();
		int restarts = pod.restarts();
		boolean ready = pod.ready();
		String reason;

		switch (scenario) {
			case "liveness-fail" -> {
				phase = "CrashLoopBackOff";
				restarts = pod.restarts() + 1;
				ready = false;
				reason = "Liveness probe failed: GET " + path + " returned 500 — kubelet killed the container and tries again (restart #" + restarts + ")";
			}
			case "readiness-fail" -> {
				phase = "Running";
				ready = false;
				reason = "Liveness probe (GET " + path + ") OK, but readiness probe returns 503 — the pod is alive yet receives NO traffic";
			}
			case "flap" -> {
				phase = "Running";
				ready = false;
				reason = "Readiness probe flapping 200→503→200→503… clients see intermittent errors and the service endpoints keep churn";
			}
			case "no-startup" -> {
				phase = "Running";
				ready = readiness;
				reason = "No startup probe — the readiness check starts counting immediately, so a slowly-booting app fails readiness until it finishes booting";
			}
			case "no-probe" -> {
				phase = "Running";
				ready = true;
				reason = "No probe configured at all — the pod counts as Ready the moment processes start, even if they would return errors";
			}
			default -> {
				phase = "Running";
				ready = readiness;
				reason = "Startup probe OK (" + startup + "s) → liveness GET " + path + " OK → readiness passed — pod Ready and receiving traffic";
			}
		}
		return new ProbeState(path, startup > 0, ready, restarts, phase, reason);
	}

	// ------------------------------------------------------------------
	// resource accounting (requests/limits + live usage)

	public void setUsage(String podKey, int cpuUse, int memUse) {
		podCpuUse.put(podKey, cpuUse);
		podMemUse.put(podKey, memUse);
	}

	public int cpuUse(Pod pod) {
		return podCpuUse.getOrDefault(key(pod.namespace(), pod.name()), pod.cpuUse());
	}

	public int memUse(Pod pod) {
		return podMemUse.getOrDefault(key(pod.namespace(), pod.name()), pod.memUse());
	}

	public int totalCpuRequests() {
		return pods().stream().mapToInt(Pod::cpuReq).sum();
	}

	public int totalMemRequests() {
		return pods().stream().mapToInt(Pod::memReq).sum();
	}

	// ------------------------------------------------------------------
	// deployments lifecycle: scale / update / rollback / self-heal

	public ReplicaSet rsFor(Deployment dep) {
		return rsets.values().stream()
				.filter(r -> r.namespace().equals(dep.namespace()) && r.owner().equals(dep.name()) && r.wanted() > 0)
				.findFirst().orElse(null);
	}

	public List<ReplicaSet> allRsFor(Deployment dep) {
		return rsets.values().stream()
				.filter(r -> r.namespace().equals(dep.namespace()) && r.owner().equals(dep.name()))
				.sorted(Comparator.comparing(ReplicaSet::name).reversed()).toList();
	}

	public List<Pod> podsForReplicaSet(ReplicaSet rs) {
		return pods(rs.namespace()).stream().filter(p -> p.name().startsWith(rs.name())).toList();
	}

	public void scaleDeployment(String name, int replicas) {
		Deployment dep = deployments.get(name);
		if (dep == null || replicas < 0) {
			return;
		}
		ReplicaSet rs = rsFor(dep);
		int ready = rs == null ? 0 : Math.min(replicas, rs.ready());
		if (rs != null) {
			rsets.put(rs.name(), new ReplicaSet(rs.namespace(), rs.name(), rs.owner(), rs.image(), replicas, ready));
			List<Pod> existing = podsForReplicaSet(rs);
			if (existing.size() < replicas) {
				for (int i = existing.size(); i < replicas; i++) {
					putPod(runningPod(rs.name() + "-x" + (i + 1), rs.image(), wl(rs), 140, 160));
				}
			} else if (existing.size() > replicas) {
				for (int i = existing.size() - 1; i >= replicas; i--) {
					removePod(rs.namespace(), existing.get(i).name());
				}
			}
		}
		deployments.put(name, new Deployment(dep.namespace(), dep.name(), dep.image(),
				replicas, ready, ready, ready, dep.strategy(), dep.generation() + 1,
				dep.revision(), dep.imagePrev(), dep.templateLabels()));
	}

	private List<String> wl(ReplicaSet rs) {
		return List.of("app=" + rs.owner(), "tier=frontend", "pod-template-hash=" + rs.name());
	}

	public void updateImage(String name, String image) {
		updateImage(name, image, rev -> rev + 1);
	}

	private void updateImage(String name, String image, java.util.function.UnaryOperator<Integer> revFn) {
		Deployment dep = deployments.get(name);
		if (dep == null || image == null || image.isBlank()) {
			return;
		}
		ReplicaSet old = rsFor(dep);
		int rev = revFn.apply(dep.revision());
		String rsName = name + RS_TOKEN + "rev-" + rev;
		if (old != null) {
			rsets.put(old.name(), new ReplicaSet(old.namespace(), old.name(), old.owner(), old.image(), 0, 0));
		}
		rsets.put(rsName, new ReplicaSet(NS_DEFAULT, rsName, name, image, dep.replicas(), dep.replicas()));
		for (int i = 1; i <= dep.replicas(); i++) {
			putPod(runningPod(rsName + "-x" + i, image, wl(rsets.get(rsName)), 140, 160));
		}
		String prev = old != null ? old.image() : dep.imagePrev();
		deployments.put(name, new Deployment(dep.namespace(), dep.name(), image,
				dep.replicas(), dep.replicas(), dep.replicas(), dep.replicas(), dep.strategy(),
				dep.generation() + 1, rev, prev, dep.templateLabels()));
	}

	public void rollback(String name) {
		Deployment dep = deployments.get(name);
		if (dep == null || dep.revision() <= 1) {
			return;
		}
		updateImage(name, dep.imagePrev(), r -> dep.revision() + 1);
	}

	public void killPod(String ns, String podName) {
		String k = key(ns, podName);
		Pod pod = pods.get(k);
		if (pod == null) {
			return;
		}
		removePod(ns, podName);
		String ownerRs = rsets.values().stream()
				.filter(r -> r.namespace().equals(ns) && podName.startsWith(r.name())).findFirst()
				.map(ReplicaSet::name).orElse(null);
		if (ownerRs != null) {
			ReplicaSet rs = rsets.get(ownerRs);
			putPod(runningPod(rs.name() + "-x" + (extractSeq(podName) + 1), rs.image(), wl(rs), 140, 160));
			appendLog(ns, "api-rs-a1", "kubelet", "pod /" + podName + " terminated — ReplicaSet " + ownerRs + " recreated it instantly");
		} else {
			appendLog(NS_DEFAULT, "flaky", "kubelet", "pod " + podName + " terminated — no ReplicaSet to bring it back: it is gone");
		}
	}

	private int extractSeq(String podName) {
		int idx = podName.lastIndexOf("-x");
		if (idx < 0) {
			return 0;
		}
		try {
			return Integer.parseInt(podName.substring(idx + 2));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public void removePod(String ns, String podName) {
		pods.remove(key(ns, podName));
	}

	public String nextPodNamePrefix() {
		return "pod-" + podSeq.incrementAndGet() + "-";
	}
}