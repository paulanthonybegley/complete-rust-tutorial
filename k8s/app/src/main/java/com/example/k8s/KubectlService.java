package com.example.k8s;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.example.k8s.K8sModels.ConfigMap;
import com.example.k8s.K8sModels.Container;
import com.example.k8s.K8sModels.Deployment;
import com.example.k8s.K8sModels.FsFile;
import com.example.k8s.K8sModels.NetPolicy;
import com.example.k8s.K8sModels.Node;
import com.example.k8s.K8sModels.Pod;
import com.example.k8s.K8sModels.Pv;
import com.example.k8s.K8sModels.Pvc;
import com.example.k8s.K8sModels.ReplicaSet;
import com.example.k8s.K8sModels.Secret;
import com.example.k8s.K8sModels.Svc;

/**
 * A read-and-write kubectl for the simulated cluster. Every command a learner
 * types is answered the way a real kubectl would answer — including the
 * Not Found and unknown-command errors — because that friction is where
 * learning happens. Nothing escapes the in-memory cluster.
 */
@Service
public class KubectlService {

	private static final String BANNER = "Server Version: v1.30.0 (k8s-zero-hero simulator)\n"
			+ "Client Version: v1.30.0\n"
			+ "Kubeconfig:     /workspace/.kube/config (simulated)";

	// a tiny "host workspace" so kubectl cp has files to move around
	private final Map<String, FsFile> workspace = new TreeMap<>();

	private final SimCluster cluster;
	private final YamlService yaml;

	public KubectlService(SimCluster cluster, YamlService yaml) {
		this.cluster = cluster;
		this.yaml = yaml;
		workspace.put("flag.txt", new FsFile("flag.txt", "21 B", "CTF{kubectl_cp_works}"));
		workspace.put("notes.txt", new FsFile("notes.txt", "38 B", "practice cp with kubectl"));
	}

	public String run(String command) {
		List<String> t = tokenize(command);
		if (t.isEmpty()) {
			return usage();
		}
		if (t.get(0).equalsIgnoreCase("kubectl")) {
			t.remove(0);
		}
		String verb = t.get(0).toLowerCase();
		try {
			return switch (verb) {
				case "get" -> get(t);
				case "describe" -> describe(t);
				case "run" -> run(t);
				case "create" -> create(t);
				case "delete" -> delete(t);
				case "scale" -> scale(t);
				case "logs" -> logs(t);
				case "exec" -> exec(t);
				case "cp" -> cp(t);
				case "top" -> top(t);
				case "rollout" -> rollout(t);
				case "apply" -> apply(t);
				case "api-versions" -> apiVersions();
				case "explain" -> explain(t);
				case "label" -> label(t);
				case "version" -> BANNER;
				case "help" -> usage();
				default -> "Error: unknown command \"" + verb + "\" for \"kubectl\"\nRun 'kubectl --help' for usage.";
			};
		} catch (CommandError e) {
			return e.getMessage();
		}
	}

	private static final class CommandError extends RuntimeException {
		CommandError(String msg) {
			super(msg);
		}
	}

	// ------------------------------------------------------------------
	// helpers

	private List<String> tokenize(String command) {
		List<String> out = new ArrayList<>();
		for (String tok : command.trim().split("\\s+")) {
			if (!tok.isEmpty()) {
				out.add(tok);
			}
		}
		return out;
	}

	private String usage() {
		return """
				kubectl controls the Kubernetes cluster manager.

				Basic Commands (Beginner):
				  create         Create a resource from a file or from stdin
				  run            Run a particular image on the cluster
				  get            Display one or many resources
				  scale          Set a new size for a Deployment
				  delete         Delete resources by filenames, stdin, resources and names

				Discover and Resolve Resources:
				  explain        Documentation of resources
				  api-versions   The supported API groups

				Interactive Console:
				  logs           Print the logs for a container in a pod
				  exec           Execute a command in a container
				  cp             Copy files/directories between an fs and a pod
				  top            Display resource (CPU/memory/disk) usage

				Deploy:
				  apply          Apply a configuration to a resource by filename
				  rollout        Manage the rollout of a resource
				  label          Update the labels on a resource

				Some examples:
				  kubectl get pods -A
				  kubectl get deployments
				  kubectl run nginx-one --image=nginx:1.24 --restart=Never
				  kubectl logs api-rs-a1 -f
				  kubectl exec api-rs-a1 -- ls /app
				  kubectl delete pod flaky
				  kubectl rollout undo deployment/web""";
	}

	private String pad(String s, int w) {
		StringBuilder b = new StringBuilder(s);
		while (b.length() < w) {
			b.append(' ');
		}
		return b.toString();
	}

	private String txt(String s) {
		return s == null ? "" : s;
	}

	private String nsOf(List<String> t) {
		for (int i = 1; i < t.size(); i++) {
			if (t.get(i).equals("-n")) {
				return i + 1 < t.size() ? t.get(i + 1) : SimCluster.NS_DEFAULT;
			}
			if (t.get(i).startsWith("--namespace=")) {
				return t.get(i).substring(12);
			}
		}
		return SimCluster.NS_DEFAULT;
	}

	private boolean allNamespaces(List<String> t) {
		return t.contains("-A") || t.contains("--all-namespaces");
	}

	private boolean hasFlag(List<String> t, String flag) {
		return t.contains(flag);
	}

	private String flagValue(List<String> t, String flag, String def) {
		for (String tok : t) {
			if (tok.startsWith(flag + "=")) {
				return tok.substring(flag.length() + 1);
			}
		}
		return def;
	}

	private String selectorOf(List<String> t) {
		for (int i = 1; i < t.size(); i++) {
			if (t.get(i).equals("-l")) {
				return i + 1 < t.size() ? t.get(i + 1) : "";
			}
		}
		return "";
	}

	private String find(List<String> t, String type) {
		for (int i = 1; i < t.size(); i++) {
			String tok = t.get(i);
			if (tok.equals(type)) {
				return i + 1 < t.size() ? t.get(i + 1) : null;
			}
		}
		return null;
	}

	private long now() {
		return cluster.now();
	}

	// ------------------------------------------------------------------
	// get

	private String get(List<String> t) {
		String type = t.size() > 1 ? t.get(1).toLowerCase() : "";
		boolean wide = hasFlag(t, "-o") && t.contains("wide") || t.indexOf("-o") >= 0 && flagValue(t, "-o", "").equals("wide");
		Map<String, String> sel = cluster.parseSelector(selectorOf(t));

		return switch (type) {
			case "nodes", "node", "no" -> getNodes();
			case "namespaces", "namespace", "ns" -> getNs();
			case "pods", "pod", "po" -> getPods(wide, sel);
			case "deployments", "deployment", "deploy" -> getDeployments();
			case "replicasets", "replicaset", "rs" -> getRs();
			case "services", "service", "svc" -> getSvcs();
			case "configmaps", "configmap", "cm" -> getCms();
			case "secrets", "secret" -> getSecrets();
			case "pvc", "persistentvolumeclaims" -> getPvcs();
			case "pv", "persistentvolumes" -> getPvs();
			case "networkpolicies", "networkpolicy", "netpol" -> getNetPols();
			case "endpoints", "endpoint", "ep" -> getEndpoints();
			case "all" -> getAll();
			case "pods-all" -> getPods(true, sel);
			default -> "error: the server doesn't have a resource type \"" + type + "\"";
		};
	}

	private String getNodes() {
		StringBuilder b = new StringBuilder(pad("NAME", 16) + pad("STATUS", 10) + pad("ROLES", 20) + pad("AGE", 6) + "VERSION\n");
		for (Node n : cluster.nodes()) {
			String roles = n.name().startsWith("minikube") ? "control-plane" : "worker";
			b.append(pad(n.name(), 16)).append(pad(n.status(), 10)).append(pad(roles, 20))
					.append(pad("1h", 6)).append("v1.30.0").append('\n');
		}
		return b.toString();
	}

	private String getNs() {
		StringBuilder b = new StringBuilder(pad("NAME", 24) + pad("STATUS", 10) + "AGE\n");
		for (String ns : cluster.namespaces()) {
			b.append(pad(ns, 24)).append(pad("Active", 10)).append("1h\n");
		}
		return b.toString();
	}

	private String getPods(boolean wide, Map<String, String> sel) {
		boolean a = allNamespaces(List.of());
		return renderPods(true, wide, sel);
	}

	private String renderPods(boolean ignoreNspaces, boolean wide, Map<String, String> sel) {
		List<Pod> pods = cluster.pods();
		if (!sel.isEmpty()) {
			pods = pods.stream().filter(p -> SimCluster.matches(p.labels(), sel)).toList();
		}
		StringBuilder b = new StringBuilder(pad("NAME", 40)).append(pad("READY", 6)).append(pad("STATUS", 20))
				.append(pad("RESTARTS", 8)).append("AGE");
		if (wide) {
			b.append(pad("IP", 14)).append("NODE");
		}
		b.append('\n');
		for (Pod p : pods) {
			boolean showNs = !p.namespace().equals(SimCluster.NS_DEFAULT);
			String display = showNs ? p.namespace() + "/" + p.name() : p.name();
			b.append(pad(display, 40)).append(pad(readyOf(p), 6)).append(pad(p.phase(), 20))
					.append(pad(String.valueOf(p.restarts()), 8)).append(p.age(now()));
			if (wide) {
				b.append(pad(ipOf(p), 14)).append("worker-1");
			}
			b.append('\n');
		}
		return b.toString();
	}

	private String readyOf(Pod p) {
		return p.ready() ? p.containers().size() + "/" + p.containers().size() : "0/" + p.containers().size();
	}

	private String ipOf(Pod p) {
		return "10.0.0." + (Math.abs(p.name().hashCode()) % 200 + 20);
	}

	private String getDeployments() {
		StringBuilder b = new StringBuilder(pad("NAME", 24) + pad("READY", 8) + pad("UP-TO-DATE", 10)
				+ pad("AVAILABLE", 10) + pad("AGE", 6) + "CONTAINERS   IMAGES\n");
		for (Deployment d : cluster.deployments()) {
			b.append(pad(d.name(), 24)).append(pad(d.ready() + "/" + d.replicas(), 8))
					.append(pad(String.valueOf(d.upToDate()), 10)).append(pad(String.valueOf(d.available()), 10))
					.append(pad("20m", 6)).append(d.name()).append("   ").append(d.image()).append('\n');
		}
		return b.toString();
	}

	private String getRs() {
		StringBuilder b = new StringBuilder(pad("NAME", 30) + pad("DESIRED", 8) + pad("CURRENT", 8) + pad("READY", 6) + "AGE\n");
		for (ReplicaSet r : cluster.replicaSets()) {
			b.append(pad(r.name(), 30)).append(pad(String.valueOf(r.wanted()), 8))
					.append(pad(String.valueOf(r.wanted()), 8)).append(String.valueOf(r.ready())).append('\n');
		}
		return b.toString();
	}

	private String getSvcs() {
		StringBuilder b = new StringBuilder(pad("NAME", 16) + pad("TYPE", 13) + pad("CLUSTER-IP", 14)
				+ pad("EXTERNAL-IP", 16) + pad("PORT(S)", 14) + "AGE\n");
		for (Svc s : cluster.services()) {
			b.append(pad(s.name(), 16)).append(pad(s.type(), 13)).append(pad(s.clusterIp(), 14))
					.append(pad(s.externalIp(), 16)).append(pad(portsOf(s), 14)).append("20m\n");
		}
		return b.toString();
	}

	private String portsOf(Svc s) {
		String base = s.targetPort() == s.port() ? s.port() + "/TCP" : s.port() + ":" + s.targetPort() + "/TCP";
		if (s.type().equals("NodePort") || s.type().equals("LoadBalancer")) {
			return base + " -> " + s.nodePort() + "/TCP";
		}
		return base;
	}

	private String getCms() {
		StringBuilder b = new StringBuilder(pad("NAME", 24) + pad("DATA", 6) + "AGE\n");
		for (ConfigMap c : cluster.configMaps()) {
			b.append(pad(c.name(), 24)).append(pad(String.valueOf(c.data().size()), 6)).append(c.age(now())).append('\n');
		}
		return b.toString();
	}

	private String getSecrets() {
		StringBuilder b = new StringBuilder(pad("NAME", 24) + pad("TYPE", 12) + pad("DATA", 6) + "AGE\n");
		for (Secret s : cluster.secrets()) {
			b.append(pad(s.name(), 24)).append(pad("Opaque", 12)).append(pad(String.valueOf(s.data().size()), 6))
					.append(s.age(now())).append('\n');
		}
		return b.toString();
	}

	private String getPvcs() {
		StringBuilder b = new StringBuilder(pad("NAME", 12) + pad("STATUS", 10) + pad("VOLUME", 10)
				+ pad("CAPACITY", 10) + pad("ACCESS MODES", 14) + pad("STORAGECLASS", 14) + "AGE\n");
		for (Pvc p : cluster.pvcs()) {
			b.append(pad(p.name(), 12)).append(pad(p.phase(), 10)).append(pad(p.boundTo(), 10))
					.append(pad(p.requestGi() + "Gi", 10)).append(pad(p.accessMode(), 14))
					.append(pad(p.storageClass(), 14)).append("20m\n");
		}
		return b.toString();
	}

	private String getPvs() {
		StringBuilder b = new StringBuilder(pad("NAME", 14) + pad("CAPACITY", 10) + pad("ACCESS MODES", 14)
				+ pad("RECLAIM POLICY", 16) + pad("STATUS", 10) + "CLAIM\n");
		for (Pv p : cluster.pvs()) {
			b.append(pad(p.name(), 14)).append(pad(p.capacityGi() + "Gi", 10)).append(pad(p.accessMode(), 14))
					.append(pad("Retain", 16)).append(pad(p.phase(), 10)).append(pad(p.claim(), 24)).append('\n');
		}
		return b.toString();
	}

	private String getNetPols() {
		StringBuilder b = new StringBuilder(pad("NAME", 24) + pad("POD-SELECTOR", 24) + "AGE\n");
		for (NetPolicy p : cluster.netPolicies()) {
			b.append(pad(p.name(), 24)).append(pad(String.join(",", p.podSelector()), 24)).append("20m\n");
		}
		return b.toString();
	}

	private String getEndpoints() {
		StringBuilder b = new StringBuilder(pad("NAME", 16) + "ENDPOINTS\n");
		for (Svc s : cluster.services()) {
			String eps = s.endpoints().isEmpty() ? "<none>" : String.join(",", s.endpoints());
			b.append(pad(s.name(), 16)).append(eps).append('\n');
		}
		return b.toString();
	}

	private String getAll() {
		StringBuilder b = new StringBuilder();
		b.append("NAME                                           READY   STATUS    RESTARTS   AGE\n");
		for (Pod p : cluster.pods()) {
			b.append(pad(p.namespace() + "/" + p.name(), 48)).append(pad(readyOf(p), 8))
					.append(pad(p.phase(), 12)).append(pad(String.valueOf(p.restarts()), 8))
					.append(p.age(now())).append('\n');
		}
		return b.toString();
	}

	// ------------------------------------------------------------------
	// describe

	private String describe(List<String> t) {
		if (t.size() < 3) {
			return "error: resource name required — e.g. kubectl describe pod web-rs-77b7c6d8f9-x1";
		}
		String type = t.get(1).toLowerCase();
		String name = t.get(2);
		return switch (type) {
			case "pod", "po", "pods" -> describePod(name);
			case "deployment", "deploy" -> describeDeployment(name);
			case "svc", "service", "services" -> describeSvc(name);
			default -> "describe not implemented for " + type + " (try pod / deployment / service)";
		};
	}

	private String describePod(String name) {
		Pod p = cluster.pods().stream().filter(x -> x.name().equals(name)).findFirst()
				.orElseThrow(() -> new CommandError("Error from server (NotFound): pods \"" + name + "\" not found"));
		StringBuilder b = new StringBuilder();
		b.append("Name:             ").append(p.name()).append('\n');
		b.append("Namespace:        ").append(p.namespace()).append('\n');
		b.append("Status:           ").append(p.phase()).append('\n');
		b.append("IP:               ").append(ipOf(p)).append('\n');
		b.append("Labels:           ").append(String.join(",", p.labels())).append('\n');
		b.append("Containers:\n");
		for (Container c : p.containers()) {
			b.append("  ").append(c.name()).append(":\n");
			b.append("    Image:          ").append(c.image()).append('\n');
			b.append("    State:          Running\n");
			b.append("    Ready:          ").append(p.ready()).append('\n');
		}
		b.append("Restart Count:    ").append(p.restarts()).append('\n');
		b.append("Events:           <none>");
		return b.toString();
	}

	private String describeDeployment(String name) {
		Deployment d = cluster.deployment(name);
		if (d == null) {
			throw new CommandError("Error from server (NotFound): deployments.apps \"" + name + "\" not found");
		}
		StringBuilder b = new StringBuilder();
		b.append("Name:                   ").append(d.name()).append('\n');
		b.append("Namespace:              ").append(d.namespace()).append('\n');
		b.append("Replicas:               ").append(d.replicas()).append(" desired | ").append(d.available())
				.append(" available | ").append(d.ready()).append(" ready\n");
		ReplicaSet rs = cluster.rsFor(d);
		if (rs != null) {
			b.append("NewReplicaSet:          ").append(rs.name()).append(" (").append(rs.ready()).append("/")
					.append(rs.wanted()).append(" replicas created)\n");
		}
		b.append("StrategyType:           ").append(d.strategy()).append('\n');
		b.append("RollingUpdateStrategy:  25% max unavailable, 25% max surge\n");
		return b.toString();
	}

	private String describeSvc(String name) {
		Svc s = cluster.service(name);
		if (s == null) {
			throw new CommandError("Error from server (NotFound): services \"" + name + "\" not found");
		}
		StringBuilder b = new StringBuilder();
		b.append("Name:              ").append(s.name()).append('\n');
		b.append("Namespace:         ").append(s.namespace()).append('\n');
		b.append("Type:              ").append(s.type()).append('\n');
		b.append("Cluster-IP:        ").append(s.clusterIp()).append('\n');
		b.append("External-IP:       ").append(s.externalIp()).append('\n');
		b.append("Port:              <unset> ").append(s.port()).append("/TCP")
				.append(" -> targetPort ").append(s.targetPort()).append("/TCP\n");
		b.append("Selector:          ").append(s.selector()).append('\n');
		b.append("Endpoints:         ").append(s.endpoints().isEmpty() ? "<none>" : String.join(",", s.endpoints())).append('\n');
		return b.toString();
	}

	// ------------------------------------------------------------------
	// run / create / delete / scale

	private String run(List<String> t) {
		String name = t.size() > 1 && !t.get(1).startsWith("-") ? t.get(1) : null;
		if (name == null) {
			return "error: requires pod name — kubectl run NAME --image=IMAGE";
		}
		String image = flagValue(t, "--image", null);
		if (image == null) {
			return "error: --image is required — kubectl run " + name + " --image=nginx:1.24";
		}
		String restart = flagValue(t, "--restart", "Always");
		String ns = nsOf(t);
		if (restart.equals("Never")) {
			cluster.putPod(new Pod(ns, name, image, "Running", 0, true,
					50, 100, 32, 64, 30, 40, now(),
					List.of(new Container(name, image)), List.of("app=" + name), null));
			cluster.appendLog(ns, name, name, "Started container " + name);
			return "pod/" + name + " created";
		}
		String rs = name + SimCluster.RS_TOKEN + "rev-1";
		cluster.putPod(new Pod(ns, rs + "-x1", image, "Running", 0, true,
				250, 500, 128, 256, 140, 160, now(),
				List.of(new Container(name, image)), List.of("app=" + name), null));
		cluster.putReplicaSet(new ReplicaSet(ns, rs, name, image, 1, 1));
		cluster.putDeployment(new Deployment(ns, name, image, 1, 1, 1, 1, "RollingUpdate", 1, 1, image,
				List.of("app=" + name)));
		return "deployment.apps/" + name + " created";
	}

	private String create(List<String> t) {
		String kind = t.size() > 1 ? t.get(1).toLowerCase() : "";
		String name = t.size() > 2 ? t.get(2) : null;
		return switch (kind) {
			case "namespace", "ns" -> {
				if (name == null) {
					yield "error: namespace name required — kubectl create namespace NAME";
				}
				cluster.createNamespace(name);
				yield "namespace/" + name + " created";
			}
			case "configmap", "cm" -> {
				if (name == null) {
					yield "error: configmap name required — kubectl create configmap NAME --from-literal=k=v";
				}
				yield createFromLiterals(t, name, false);
			}
			case "secret" -> {
				if (name == null) {
					yield "error: secret name required — kubectl create secret generic NAME --from-literal=k=v";
				}
				yield createFromLiterals(t, name, true);
			}
			default -> "error: cannot create resource type \"" + kind + "\" (namespace / configmap / secret supported)";
		};
	}

	private String createFromLiterals(List<String> t, String name, boolean secret) {
		Map<String, String> data = new LinkedHashMap<>();
		for (String tok : t) {
			if (tok.startsWith("--from-literal=")) {
				String pair = tok.substring(15);
				int eq = pair.indexOf('=');
				if (eq > 0) {
					data.put(pair.substring(0, eq), pair.substring(eq + 1));
				}
			}
		}
		if (data.isEmpty()) {
			data.put("KEY", "value-placeholder");
		}
		if (secret) {
			cluster.putSecret(new Secret(SimCluster.NS_DEFAULT, name, data, now()));
			return "secret/" + name + " created";
		}
		cluster.putConfigMap(new ConfigMap(SimCluster.NS_DEFAULT, name, data, now()));
		return "configmap/" + name + " created";
	}

	private String delete(List<String> t) {
		String kind = t.size() > 1 ? t.get(1).toLowerCase() : "";
		String name = t.size() > 2 ? t.get(2) : null;
		if (name == null) {
			return "error: resource name required — kubectl delete " + kind + " NAME";
		}
		String ns = nsOf(t);
		return switch (kind) {
			case "pod", "po", "pods" -> {
				if (cluster.pods(ns).stream().noneMatch(p -> p.name().equals(name))) {
					yield "Error from server (NotFound): pods \"" + name + "\" not found";
				}
				cluster.killPod(ns, name);
				yield "pod/" + name + " deleted";
			}
			case "deployment", "deploy" -> {
				if (cluster.deployment(name) == null) {
					yield "Error from server (NotFound): deployments.apps \"" + name + "\" not found";
				}
				cluster.removeDeployment(name);
				yield "deployment.apps/" + name + " deleted";
			}
			case "service", "svc" -> {
				if (cluster.service(name) == null) {
					yield "Error from server (NotFound): services \"" + name + "\" not found";
				}
				cluster.removeSvc(name);
				yield "service/" + name + " deleted";
			}
			default -> "error: cannot delete resource type \"" + kind + "\" (pod / deployment / service supported)";
		};
	}

	private String scale(List<String> t) {
		if (t.size() < 3 || !t.get(1).equalsIgnoreCase("deployment")) {
			return "error: kubectl scale deployment NAME --replicas=N";
		}
		String name = t.get(2);
		String replicas = flagValue(t, "--replicas", null);
		if (replicas == null) {
			return "error: --replicas is required — kubectl scale deployment " + name + " --replicas=5";
		}
		int n = Integer.parseInt(replicas.replaceAll("[^0-9-]", ""));
		if (cluster.deployment(name) == null) {
			return "Error from server (NotFound): deployments.apps \"" + name + "\" not found";
		}
		cluster.scaleDeployment(name, n);
		return "deployment.apps/" + name + " scaled to " + n + " replicas";
	}

	// ------------------------------------------------------------------
	// logs / exec / cp / top

	private String logs(List<String> t) {
		String name = find(t, "pod");
		if (name == null) {
			for (String tok : t) {
				if (tok.startsWith("api-") || tok.startsWith("web-") || tok.startsWith("flaky") || tok.startsWith("pod-")) {
					name = tok;
					break;
				}
			}
		}
		if (name == null) {
			return "error: pod name required — kubectl logs POD [container]";
		}
		String ns = nsOf(t);
		String container = flagValue(t, "-c", null);
		if (container == null) {
			container = flagValue(t, "--container", null);
		}
		StringBuilder b = new StringBuilder();
		if (hasFlag(t, "-f") || hasFlag(t, "--follow")) {
			b.append("== streaming (follow) — Ctrl-C to detach ==\n");
		}
		final String containerFilter = container;
		var lines = cluster.logs(ns, name).stream()
				.filter(l -> containerFilter == null || l.container().equals(containerFilter)).toList();
		if (lines.isEmpty()) {
			return "Error from server (BadRequest): pod " + ns + "/" + name + " has no logs currently";
		}
		boolean stamps = hasFlag(t, "--timestamps");
		for (var l : lines) {
			b.append(stamps ? "[" + l.time() + "] " : "").append(l.container()).append(' ').append(l.text()).append('\n');
		}
		if (hasFlag(t, "--previous")) {
			b.append("== --previous: showing only the previous (crashed) container instance ==\n");
		}
		return b.toString().stripTrailing();
	}

	private String exec(List<String> t) {
		String name = null;
		int at = -1;
		for (int i = 1; i < t.size(); i++) {
			if (t.get(i).equals("--")) {
				at = i;
				break;
			}
			if (i == 1 || name == null && !t.get(i).startsWith("-")) {
				name = t.get(i);
			}
		}
		if (name == null) {
			return "error: [POD] required — kubectl exec POD -- COMMAND";
		}
		if (at < 0 || at + 1 >= t.size()) {
			return "error: you must specify a command — kubectl exec " + name + " -- ls /app";
		}
		List<String> args = t.subList(at + 1, t.size());
		return shell(name, args);
	}

	private String shell(String pod, List<String> args) {
		String cmd = args.get(0);
		return switch (cmd) {
			case "ls" -> ls(pod, args.size() > 1 ? args.get(1) : "/");
			case "pwd" -> "/ (container root)\n";
			case "cat" -> cat(pod, args.size() > 1 ? args.get(1) : null);
			case "hostname" -> pod + "\n";
			case "whoami" -> "root\n";
			case "env" -> env(pod);
			case "printenv" -> args.size() > 1 ? envVar(pod, args.get(1)) : env(pod);
			case "echo" -> String.join(" ", args.subList(1, args.size())) + "\n";
			default -> "sh: 1: " + args.get(0) + ": not found\n";
		};
	}

	private String ls(String pod, String dir) {
		String d = dir.endsWith("/") ? dir : dir + "/";
		Pod p = byName(pod);
		if (p == null) {
			throw new CommandError("Error from server (NotFound): pods \"" + pod + "\" not found");
		}
		Map<String, FsFile> fs = cluster.files(p.namespace(), pod);
		List<String> out = new ArrayList<>();
		int depth = d.equals("/") ? 0 : d.split("/").length;
		for (FsFile f : fs.values()) {
			String path = f.path();
			if (path.startsWith(d) && !path.equals(d) && path.split("/").length == depth + 2) {
				int cut = d.equals("/") ? 1 : d.length();
				out.add(path.substring(cut));
			}
		}
		if (out.isEmpty()) {
			return "ls: " + dir + ": No such file or directory\n";
		}
		return String.join("   ", out) + "\n";
	}

	private String cat(String pod, String path) {
		if (path == null) {
			return "cat: missing file operand\n";
		}
		Pod p = byName(pod);
		if (p == null) {
			throw new CommandError("Error from server (NotFound): pods \"" + pod + "\" not found");
		}
		Map<String, FsFile> fs = cluster.files(p.namespace(), pod);
		FsFile f = fs.get(path);
		if (f == null || f.content() == null) {
			return "cat: " + path + ": No such file or directory\n";
		}
		return f.content() + "\n";
	}

	private String env(String pod) {
		byName(pod);
		StringBuilder b = new StringBuilder();
		Map<String, String> envs = new TreeMap<>(Map.of(
				"HOSTNAME", pod,
				"KUBERNETES_PORT", "tcp://10.96.0.1:443",
				"NS", SimCluster.NS_DEFAULT));
		for (ConfigMap cm : cluster.configMaps()) {
			envs.putAll(cm.data());
		}
		for (Secret s : cluster.secrets()) {
			envs.putAll(s.data());
		}
		envs.forEach((k, v) -> b.append(k).append('=').append(v).append('\n'));
		return b.toString();
	}

	private String envVar(String pod, String var) {
		String v = env(pod);
		for (String line : v.split("\n")) {
			if (line.startsWith(var + "=")) {
				return line.substring(var.length() + 1) + "\n";
			}
		}
		return var + " is not set\n";
	}

	private Pod byName(String name) {
		return cluster.pods().stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
	}

	private String cp(List<String> t) {
		if (t.size() < 3) {
			return cpUsage();
		}
		String a = t.get(1);
		String b = t.get(2);
		int hero = a.indexOf(':');
		if (hero > 0) {
			// pull: pod:path -> local path
			String pod = a.substring(0, hero);
			String src = a.substring(hero + 1);
			FsFile f = cluster.files(SimCluster.NS_DEFAULT, pod).get(src);
			if (f == null) {
				return "error: ENOENT: " + src + " does not exist in pod " + pod;
			}
			workspace.put(b, new FsFile(b, f.size(), f.content()));
			return "Successfully copied " + a + " to " + b;
		}
		hero = b.indexOf(':');
		if (hero > 0) {
			String pod = b.substring(0, hero);
			String dst = b.substring(hero + 1);
			FsFile f = workspace.get(a);
			if (f == null) {
				return "error: ENOENT: " + a + " not found in the local workspace";
			}
			if (wsIsPodMissing(pod)) {
				return "Error from server (NotFound): pods \"" + pod + "\" not found";
			}
			cluster.writeFile(SimCluster.NS_DEFAULT, pod, dst, f.content());
			return "Successfully copied " + a + " to " + b;
		}
		return cpUsage();
	}

	private boolean wsIsPodMissing(String pod) {
		try {
			Pod p = byName(pod);
			return p == null;
		} catch (Exception e) {
			return true;
		}
	}

	private String cpUsage() {
		return "error: file/path required — kubectl cp <file> <pod>:<path>  or  kubectl cp <pod>:<path> <file>";
	}

	private String top(List<String> t) {
		String kind = t.size() > 1 ? t.get(1).toLowerCase() : "";
		return switch (kind) {
			case "pod", "pods" -> {
				StringBuilder b = new StringBuilder(pad("NAME", 36) + pad("CPU(cores)", 12) + "MEMORY(bytes)\n");
				for (Pod p : cluster.pods()) {
					b.append(pad(p.name(), 36)).append(pad(cluster.cpuUse(p) + "m", 12))
							.append(cluster.memUse(p) + "Mi\n");
				}
				yield b.toString();
			}
			case "node", "nodes" -> {
				StringBuilder b = new StringBuilder(pad("NAME", 16) + pad("CPU(cores)", 12)
						+ pad("CPU%", 8) + pad("MEMORY(bytes)", 14) + "MEMORY%\n");
				for (Node n : cluster.nodes()) {
					b.append(pad(n.name(), 16)).append(pad(String.valueOf(n.cpuUse()), 12))
							.append(pad((int) (100.0 * n.cpuUse() / n.cpuMilli()) + "%", 8))
							.append(pad(String.valueOf(n.memUse() * 1048576L), 14))
							.append((int) (100.0 * n.memUse() / n.memMi()) + "%\n");
				}
				yield b.toString();
			}
			default -> "error: kubectl top {node|pod} — component \"top \" not implemented (use node or pod)";
		};
	}

	// ------------------------------------------------------------------
	// rollout / apply / api-versions / explain / label

	private String rollout(List<String> t) {
		String action = t.size() > 1 ? t.get(1).toLowerCase() : "";
		String name = t.size() > 2 ? t.get(2) : null;
		if (name == null) {
			return "error: resource required — kubectl rollout " + action + " deployment/web";
		}
		String depName = name.contains("/") ? name.substring(name.indexOf('/') + 1) : name;
		Deployment d = cluster.deployment(depName);
		if (d == null) {
			return "Error from server (NotFound): deployments.apps \"" + depName + "\" not found";
		}
		return switch (action) {
			case "status" -> "deployment \"" + depName + "\" successfully rolled out";
			case "history" -> {
				StringBuilder b = new StringBuilder("REVISION  CHANGE-CAUSE\n");
				b.append("1         ").append(d.imagePrev().isEmpty() ? d.image() : d.imagePrev()).append('\n');
				b.append("2         ").append(d.image()).append('\n');
				yield b.toString();
			}
			case "undo" -> {
				cluster.rollback(depName);
				yield "deployment.apps/" + depName + " rolled back";
			}
			case "restart" -> "deployment.apps/" + depName + " restarted";
			default -> "error: unknown rollout action \"" + action + "\" (status / history / undo / restart)";
		};
	}

	private String apply(List<String> t) {
		String file = flagValue(t, "-f", null);
		if (file == null) {
			return "error: -f is required — kubectl apply -f manifest.yaml";
		}
		if (!yaml.presetNames().contains(file)) {
			if (file.endsWith(".yaml") || file.endsWith(".yml")) {
				return "Error from the simulator: preset \"" + file + "\" is not bundled.\nBundled manifests: "
						+ String.join(", ", yaml.presetNames());
			}
			return "error: the path \"" + file + "\" does not exist";
		}
		return yaml.apply(yaml.preset(file));
	}

	private String apiVersions() {
		return """
				core/v1
				apps/v1
				batch/v1
				networking.k8s.io/v1
				storage.k8s.io/v1
				rbac.authorization.k8s.io/v1
				autoscaling/v1
				policy/v1""";
	}

	private String explain(List<String> t) {
		String type = t.size() > 1 ? t.get(1).toLowerCase() : "";
		return switch (type) {
			case "pod" -> explainBlock("Pod", Map.of(
					"apiVersion", "string — v1 (core)",
					"kind", "string — Pod",
					"metadata.name", "string — DNS-1123 subdomain",
					"metadata.namespace", "string — default is 'default'",
					"metadata.labels", "map — key/value pairs the selectors match",
					"spec.containers[]", "list — one or more containers",
					"spec.containers[].image", "string — image reference (registry/repo:tag)",
					"spec.restartPolicy", "string — Always | OnFailure | Never"));
			case "deployment", "deploy" -> explainBlock("Deployment", Map.of(
					"apiVersion", "string — apps/v1",
					"kind", "string — Deployment",
					"metadata.name", "string",
					"spec.replicas", "integer — desired pod count",
					"spec.selector.matchLabels", "map — must match template labels",
					"spec.template.metadata.labels", "map — labels stamped on every pod",
					"spec.template.spec.containers[].image", "string",
					"spec.strategy.type", "string — RollinUpdate | Recreate",
					"spec.strategy.rollingUpdate.maxSurge", "list/integer — how many above desired",
					"spec.strategy.rollingUpdate.maxUnavailable", "list/integer — how many below desired"));
			case "service", "svc" -> explainBlock("Service", Map.of(
					"apiVersion", "string — v1 (core)",
					"kind", "string — Service",
					"spec.type", "string — ClusterIP | NodePort | LoadBalancer | ExternalName",
					"spec.selector", "map — which pods receive traffic",
					"spec.ports[].port", "integer — port the service listens on",
					"spec.ports[].targetPort", "integer — port the pod listens on",
					"spec.ports[].nodePort", "integer — 30000-32767, NodePort & LoadBalancer only"));
			case "configmap", "cm" -> explainBlock("ConfigMap", Map.of(
					"apiVersion", "string — v1",
					"metadata.name", "string",
					"data", "map — plain key/value configuration"));
			case "secret" -> explainBlock("Secret", Map.of(
					"apiVersion", "string — v1",
					"metadata.name", "string",
					"type", "string — Opaque is the default",
					"stringData", "map — plaintext, encoded as base64 at rest",
					"data", "map — base64 already-encoded values",
					"immutable", "boolean — blocks mutation"));
			case "pvc", "persistentvolumeclaim" -> explainBlock("PersistentVolumeClaim", Map.of(
					"apiVersion", "string — v1",
					"spec.accessModes", "list — ReadWriteOnce | ReadOnlyMany | ReadWriteMany",
					"spec.storageClassName", "string — StorageClass for dynamic provisioning",
					"spec.resources.requests.storage", "quantity — e.g. 4Gi"));
			case "networkpolicy", "netpol" -> explainBlock("NetworkPolicy", Map.of(
					"apiVersion", "string — networking.k8s.io/v1",
					"spec.podSelector.matchLabels", "map — pods this policy applies to",
					"spec.policyTypes", "list — Ingress and/or Egress",
					"spec.ingress[].from", "list — allowed sources",
					"spec.egress[].to", "list — allowed destinations",
					"spec.policyTypes", "— a policy with no ingress rule = deny all ingress"));
			case "node" -> explainBlock("Node", Map.of(
					"apiVersion", "string — v1",
					"kind", "string — Node",
					"spec.taints", "list — repel pods without matching tolerations",
					"status.allocatable.cpu", "quantity — schedulable capacity",
					"status.allocatable.memory", "quantity",
					"metadata.labels 'kubernetes.io/role'", "string — control-plane | worker"));
			default -> "error: 'kubectl explain' for resource \"" + type + "\" not bundled (try pod, deployment, service, configmap, secret, pvc, networkpolicy, node)";
		};
	}

	private String explainBlock(String name, Map<String, String> fields) {
		StringBuilder b = new StringBuilder("KIND:     " + name + "\nVERSION:  \n\nFIELD      TYPE        DESCRIPTION\n");
		for (Map.Entry<String, String> e : fields.entrySet()) {
			b.append(pad(e.getKey(), 28)).append(pad(fieldType(e.getKey()), 12)).append(e.getValue()).append('\n');
		}
		return b.toString();
	}

	private String fieldType(String key) {
		if (key.contains("[]")) {
			return "[]Tuple";
		}
		if (key.contains("labels") || key.contains("data") || key.contains("values")) {
			return "map";
		}
		if (key.endsWith("name") || key.equals("image")) {
			return "string";
		}
		return "string";
	}

	private String label(List<String> t) {
		String kind = t.size() > 1 ? t.get(1).toLowerCase() : "";
		String name = t.size() > 2 ? t.get(2) : null;
		String pair = t.size() > 3 ? t.get(3) : null;
		if (name == null || pair == null || !pair.contains("=")) {
			return "error: kubectl label " + kind + " NAME KEY=VALUE";
		}
		String key = pair.substring(0, pair.indexOf('='));
		String value = pair.substring(pair.indexOf('=') + 1);
		return switch (kind) {
			case "pod", "po", "pods" -> {
				Pod p = byName(name);
				if (p == null) {
					yield "Error from server (NotFound): pods \"" + name + "\" not found";
				}
				List<String> labels = new ArrayList<>();
				boolean replaced = false;
				for (String l : p.labels()) {
					if (l.startsWith(key + "=")) {
						labels.add(key + "=" + value);
						replaced = true;
					} else {
						labels.add(l);
					}
				}
				if (!replaced) {
					labels.add(key + "=" + value);
				}
				cluster.putPod(new Pod(p.namespace(), p.name(), p.image(), p.phase(), p.restarts(), p.ready(),
						p.cpuReq(), p.cpuLim(), p.memReq(), p.memLim(), p.cpuUse(), p.memUse(),
						p.ageEpoch(), p.containers(), labels, p.volumeFile()));
				yield "pod/" + name + " labeled";
			}
			case "deployment", "deploy" -> {
				Deployment d = cluster.deployment(name);
				if (d == null) {
					yield "Error from server (NotFound): deployments.apps \"" + name + "\" not found";
				}
				List<String> labels = new ArrayList<>(d.templateLabels());
				labels.add(key + "=" + value);
				cluster.putDeployment(new Deployment(d.namespace(), d.name(), d.image(), d.replicas(), d.ready(),
						d.upToDate(), d.available(), d.strategy(), d.generation(), d.revision(),
						d.imagePrev(), labels));
				yield "deployment.apps/" + name + " labeled";
			}
			default -> "error: cannot label resource type \"" + kind + "\" (pod / deployment supported)";
		};
	}

}