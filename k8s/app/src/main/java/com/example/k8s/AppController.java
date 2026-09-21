package com.example.k8s;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.k8s.K8sModels.Pod;
import com.example.k8s.ResourceService.Fit;
import com.example.k8s.ResourceService.Qos;

import jakarta.servlet.http.HttpSession;

/**
 * One controller per station, mirroring the video's chapters. Sim endpoints
 * return either full pages or htmx fragments; every mutating handler leaves a
 * short "current event" line on the page so the learner can read what just
 * happened.
 */
@Controller
public class AppController {

	private final SimCluster cluster;
	private final KubectlService kubectl;
	private final YamlService yaml;
	private final ResourceService resources;
	private final NetpolService netpol;

	public AppController(SimCluster cluster, KubectlService kubectl, YamlService yaml,
			ResourceService resources, NetpolService netpol) {
		this.cluster = cluster;
		this.kubectl = kubectl;
		this.yaml = yaml;
		this.resources = resources;
		this.netpol = netpol;
	}

	@ModelAttribute
	public void currentYear(Model model) {
		model.addAttribute("currentYear", Year.now().getValue());
	}

	// ------------------------------------------------------------------
	// shared helpers

	private void mark(HttpSession session, String slug) {
		if (session == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		LinkedHashSet<String> done = (LinkedHashSet<String>) session.getAttribute("done");
		if (done == null) {
			done = new LinkedHashSet<>();
			session.setAttribute("done", done);
		}
		done.add(slug);
	}

	private List<String> marks(HttpSession session) {
		if (session == null) {
			return List.of();
		}
		@SuppressWarnings("unchecked")
		LinkedHashSet<String> done = (LinkedHashSet<String>) session.getAttribute("done");
		return done == null ? List.of() : List.copyOf(done);
	}

	private int meter(HttpSession session) {
		return marks(session).size();
	}

	private static final List<String> STATIONS = List.of("arch", "manifest", "pods", "resources",
			"probes", "exec", "logs", "config", "deploys", "storage", "services", "netpol");

	// ------------------------------------------------------------------
	// home

	@GetMapping("/")
	public String home(HttpSession session, Model model) {
		model.addAttribute("pageTitle", "Home");
		model.addAttribute("meter", meter(session));
		model.addAttribute("meterMax", STATIONS.size());
		model.addAttribute("done", marks(session));
		model.addAttribute("totalPods", countRunningPods());
		model.addAttribute("deployReady", allDeploymentsReady());
		model.addAttribute("namespaces", cluster.namespaces());
		model.addAttribute("getPods", kubectl.run("get pods"));
		return "index";
	}

	private int countRunningPods() {
		return (int) cluster.pods().stream().filter(p -> p.phase().equals("Running")).count();
	}

	private boolean allDeploymentsReady() {
		return cluster.deployments().stream().noneMatch(d -> d.ready() < d.replicas());
	}

	// ------------------------------------------------------------------
	// Architecture & Purpose (0:01:42)

	public record TraceStep(int n, String sender, String receiver, String detail) {
	}

	private static final List<TraceStep> TRACE = List.of(
			new TraceStep(1, "you / kubectl", "API server", "kubectl apply -f web.yaml serialises the manifest and authenticates your kubeconfig"),
			new TraceStep(2, "API server", "etcd", "the request passes validation + authorization, then the desired state is committed to etcd"),
			new TraceStep(3, "API server", "scheduler", "a watch fires: 'desired pod, no node yet'. The scheduler picks a node that can fit the request"),
			new TraceStep(4, "scheduler", "API server", "the bind decision is written back (nodeName set) — the API server is the only writer to etcd"),
			new TraceStep(5, "controller manager", "ReplicaSet", "controllers reconcile continuously: the ReplicaSet controller creates the missing Pod objects"),
			new TraceStep(6, "API server", "kubelet", "the kubelet on the chosen node watches its assigned pods and starts the containers"),
			new TraceStep(7, "kubelet", "container runtime", "the runtime (containerd) pulls the image and starts the container processes"),
			new TraceStep(8, "kube-proxy", "iptables/ipvs", "service endpoints are wired so traffic to the ClusterIP reaches the pod — the pod is now live"));

	@GetMapping("/arch")
	public String arch(@RequestParam(defaultValue = "1") int step, HttpSession session, Model model) {
		mark(session, "arch");
		model.addAttribute("pageTitle", "Architecture");
		model.addAttribute("trace", TRACE);
		model.addAttribute("active", Math.max(1, Math.min(step, TRACE.size())));
		model.addAttribute("activeStep", TRACE.get(Math.max(1, Math.min(step, TRACE.size())) - 1));
		return "arch";
	}

	// ------------------------------------------------------------------
	// Manifest & YAML lab (0:21:02 YAML · 0:24:52 Manifests · 0:38:58 API versioning)

	@GetMapping("/manifest")
	public String manifest(@RequestParam(defaultValue = "deploy-web.yaml") String p,
			@RequestParam(defaultValue = "false") boolean json, HttpSession session, Model model) {
		mark(session, "manifest");
		String content = yaml.preset(p);
		String preset = p;
		if (!yaml.presetNames().contains(p)) {
			preset = YamlService.POD_NGINX;
			content = yaml.preset(preset);
		}
		model.addAttribute("pageTitle", "Manifests & YAML");
		model.addAttribute("presets", yaml.presetNames());
		model.addAttribute("preset", preset);
		model.addAttribute("manifest", content);
		model.addAttribute("json", json ? yaml.toJson(content) : null);
		model.addAttribute("apiVersions", List.of("v1", "apps/v1", "networking.k8s.io/v1",
				"storage.k8s.io/v1", "apps/v1beta1 (deprecated)"));
		return "manifest";
	}

	@PostMapping(value = "/manifest", params = "action=validate")
	public String manifestValidate(@RequestParam("manifest") String content, HttpSession session, Model model) {
		model.addAttribute("validation", yaml.validate(content));
		model.addAttribute("manifest", content);
		return "fragments :: validationResult";
	}

	@PostMapping(value = "/manifest", params = "action=apply")
	public String manifestApply(@RequestParam("manifest") String content, HttpSession session, Model model) {
		model.addAttribute("applyResult", yaml.apply(content));
		model.addAttribute("manifest", content);
		return "fragments :: applyResult";
	}

	// ------------------------------------------------------------------
	// kubectl console (shared by Pods / Exec / Logs / Deploys stations)

	@PostMapping("/kubectl")
	public String kubectl(@RequestParam String cmd, Model model) {
		model.addAttribute("cmd", cmd);
		model.addAttribute("output", kubectl.run(cmd));
		return "fragments :: kubectl";
	}

	// ------------------------------------------------------------------
	// Making Pods + Namespaces (0:28:55 · 0:34:00)

	@GetMapping("/pods")
	public String pods(HttpSession session, Model model) {
		mark(session, "pods");
		model.addAttribute("pageTitle", "Pods & Namespaces");
		model.addAttribute("namespaces", cluster.namespaces());
		model.addAttribute("podsByNs", podsByNamespace());
		model.addAttribute("getPods", kubectl.run("get pods"));
		model.addAttribute("getAll", kubectl.run("get all"));
		return "pods";
	}

	private Map<String, List<Pod>> podsByNamespace() {
		Map<String, List<Pod>> map = new java.util.LinkedHashMap<>();
		for (String ns : cluster.namespaces()) {
			map.put(ns, cluster.pods(ns));
		}
		return map;
	}

	// ------------------------------------------------------------------
	// Monitoring + Requests & Limits (0:41:43 · 0:44:56)

	private Pod resourceTarget() {
		return cluster.pods().stream().filter(p -> p.name().startsWith("web-rs-")).findFirst().orElseThrow();
	}

	@GetMapping("/resources")
	public String resources(HttpSession session, Model model) {
		mark(session, "resources");
		Pod p = resourceTarget();
		Qos qos = resources.qos(p);
		Fit fit = resources.fit();
		model.addAttribute("pageTitle", "Requests & Limits");
		model.addAttribute("pod", p);
		model.addAttribute("qos", qos);
		model.addAttribute("fit", fit);
		model.addAttribute("topNode", kubectl.run("top node"));
		model.addAttribute("topPod", kubectl.run("top pod"));
		return "resources";
	}

	@PostMapping("/resources")
	public String resourcesApply(@RequestParam int cpuReq, @RequestParam int cpuLim,
			@RequestParam int memReq, @RequestParam int memLim, @RequestParam int cpuUse,
			@RequestParam int memUse, Model model) {
		Pod old = resourceTarget();
		Pod next = new Pod(old.namespace(), old.name(), old.image(), old.phase(), old.restarts(), old.ready(),
				Math.max(0, cpuReq), Math.max(0, cpuLim), Math.max(0, memReq), Math.max(0, memLim),
				Math.max(0, cpuUse), Math.max(0, memUse), old.ageEpoch(), old.containers(), old.labels(), null);
		cluster.putPod(next);
		return "redirect:/resources#result";
	}

	// ------------------------------------------------------------------
	// Probes (0:51:01)

	@GetMapping("/probes")
	public String probes(HttpSession session, Model model) {
		mark(session, "probes");
		model.addAttribute("pageTitle", "Probes");
		model.addAttribute("webPods", webPods());
		model.addAttribute("states", probeStates());
		model.addAttribute("scenarios", List.of("healthy", "liveness-fail", "readiness-fail", "flap", "no-startup", "no-probe"));
		return "probes";
	}

	private List<Pod> webPods() {
		return cluster.pods().stream().filter(p -> p.name().startsWith("web-rs-")).toList();
	}

	private Map<String, K8sModels.ProbeState> probeStates() {
		Map<String, K8sModels.ProbeState> out = new java.util.LinkedHashMap<>();
		for (Pod p : webPods()) {
			out.put(p.name(), cluster.probeState(p));
		}
		return out;
	}

	@PostMapping("/probes")
	public String probesApply(@RequestParam String pod, @RequestParam String scenario,
			@RequestParam(defaultValue = "5") int startup, @RequestParam(defaultValue = "/health") String path,
			@RequestParam(defaultValue = "true") boolean readiness, Model model) {
		cluster.setProbe(SimCluster.key(SimCluster.NS_DEFAULT, pod), scenario, startup, path, readiness);
		model.addAttribute("states", probeStates());
		model.addAttribute("webPods", webPods());
		model.addAttribute("lastEvent", "kubectl set probe on pod/" + pod + " · scenario \"" + scenario + "\"");
		return "fragments :: probeCards";
	}

	// ------------------------------------------------------------------
	// kubectl exec & cp (0:58:20)

	private static final Map<String, String> WORKSPACE = new java.util.LinkedHashMap<>() {
		{
			put("flag.txt", "CTF{kubectl_cp_works}");
			put("notes.txt", "practice cp with kubectl");
		}
	};

	@GetMapping("/exec")
	public String exec(HttpSession session, Model model) {
		mark(session, "exec");
		model.addAttribute("pageTitle", "exec & cp");
		model.addAttribute("apiPod", apiPod());
		model.addAttribute("fs", cluster.files(SimCluster.NS_DEFAULT, "api-rs-a1"));
		model.addAttribute("workspace", WORKSPACE);
		model.addAttribute("samples", List.of(
				"ls /", "ls /app", "ls /app/config", "cat /app/config/app.properties",
				"env | nice",
				"hostname", "printenv GREETING"));
		return "exec";
	}

	private Pod apiPod() {
		return cluster.pods().stream().filter(p -> p.name().equals("api-rs-a1")).findFirst()
				.orElseGet(() -> {
					cluster.putPod(new Pod(SimCluster.NS_DEFAULT, "api-rs-a1", "api:1.0", "Running", 0, true,
							100, 200, 64, 128, 60, 70, cluster.now(),
							List.of(new K8sModels.Container("api", "api:1.0"),
									new K8sModels.Container("sidecar", "sidecar:1.0")),
							List.of("app=api"), null));
					return cluster.pods().stream().filter(p -> p.name().equals("api-rs-a1")).findFirst().orElseThrow();
				});
	}

	@PostMapping("/exec")
	public String execAction(@RequestParam(defaultValue = "ls /app") String cmd,
			@RequestParam(defaultValue = "flag.txt") String src,
			@RequestParam(defaultValue = "/data/flag.txt") String dst,
			@RequestParam(defaultValue = "cp") String action,
			HttpSession session, Model model) {
		mark(session, "exec");
		if (action.equals("cp")) {
			String content = WORKSPACE.getOrDefault(src, "content copied from workspace");
			cluster.writeFile(SimCluster.NS_DEFAULT, "api-rs-a1", dst, content);
			model.addAttribute("lastEvent", "kubectl cp " + src + " api-rs-a1:" + dst + "  →  " + content.length() + " bytes");
		} else if (action.equals("cat")) {
			model.addAttribute("lastEvent", kubectl.run("exec api-rs-a1 -- cat " + src));
		} else {
			model.addAttribute("lastEvent", kubectl.run("exec api-rs-a1 -- " + cmd
					+ "\n\n# every exec here actually runs against the simulator's virtual filesystem"));
		}
		model.addAttribute("apiPod", apiPod());
		model.addAttribute("fs", cluster.files(SimCluster.NS_DEFAULT, "api-rs-a1"));
		model.addAttribute("workspace", WORKSPACE);
		model.addAttribute("samples", List.of(
				"ls /", "ls /app", "ls /app/config", "cat /app/config/app.properties",
				"env | nice",
				"hostname", "printenv GREETING"));
		return "exec";
	}

	// ------------------------------------------------------------------
	// Viewing Logs (1:21:16)

	@GetMapping("/logs")
	public String logs(@RequestParam(defaultValue = "api") String container, HttpSession session, Model model) {
		mark(session, "logs");
		model.addAttribute("pageTitle", "Viewing Logs");
		model.addAttribute("apiPod", apiPod());
		model.addAttribute("lines", cluster.logs(SimCluster.NS_DEFAULT, "api-rs-a1"));
		model.addAttribute("container", container);
		return "logs";
	}

	@PostMapping("/logs")
	public String logsEmit(@RequestParam(defaultValue = "api") String container,
			@RequestParam(defaultValue = "launch") String action, Model model) {
		if (action.equals("healthcheck")) {
			cluster.appendLog(SimCluster.NS_DEFAULT, "api-rs-a1", "api", "GET /healthz 200 1ms");
			cluster.appendLog(SimCluster.NS_DEFAULT, "api-rs-a1", "api", "GET /orders 200 3ms");
		} else if (action.equals("crash")) {
			cluster.appendLog(SimCluster.NS_DEFAULT, "api-rs-a1", "api", "panic: runtime error: index out of range");
			cluster.appendLog(SimCluster.NS_DEFAULT, "api-rs-a1", "api", "goroutine 1 [running]: main.main()");
		} else {
			cluster.appendLog(SimCluster.NS_DEFAULT, "api-rs-a1", "api", "sidecar: rotated log at 12:00:00Z");
		}
		model.addAttribute("apiPod", apiPod());
		model.addAttribute("lines", cluster.logs(SimCluster.NS_DEFAULT, "api-rs-a1"));
		model.addAttribute("container", container);
		return "fragments :: logLines";
	}

	// ------------------------------------------------------------------
	// ConfigMaps · Volume Mounting · Secrets (1:05:34 · 1:09:49 · 1:18:05)

	@GetMapping("/config")
	public String config(@RequestParam(defaultValue = "env") String mount, HttpSession session, Model model) {
		mark(session, "config");
		model.addAttribute("pageTitle", "ConfigMaps & Secrets");
		model.addAttribute("configMaps", cluster.configMaps());
		model.addAttribute("secrets", cluster.secrets());
		model.addAttribute("b64", b64Map(cluster.secrets()));
		model.addAttribute("mount", mount);
		model.addAttribute("envManifest", ENV_MANIFEST);
		model.addAttribute("volumeManifest", VOLUME_MANIFEST);
		model.addAttribute("dataSnapshot", envSnapshotText());
		model.addAttribute("fileSnapshot", fileSnapshotText());
		return "config";
	}

	private static final String ENV_MANIFEST = """
			env:
			  - name: APP_COLOR
			    valueFrom:
			      configMapKeyRef:
			        name: web-config
			        key: APP_COLOR
			  - name: DB_PASSWORD
			    valueFrom:
			      secretKeyRef:
			        name: web-secret
			        key: DB_PASSWORD

			# Env vars are baked in at pod creation. Rotate the Secret
			# and existing pods keep the OLD value until they restart.
			$ kubectl exec api-rs-a1 -- printenv APP_COLOR DB_PASSWORD
			blue | s3cr3t!""";

	private static final String VOLUME_MANIFEST = """
			volumes:
			  - name: web-config
			    configMap:
			      name: web-config
			  - name: web-secrets
			    secret:
			      secretName: web-secret
			      items:
			        - key: DB_PASSWORD
			          path: db-password
			containers:
			  - name: api
			    volumeMounts:
			      - name: web-config
			        mountPath: /app/config
			      - name: web-secrets
			        mountPath: /run/secrets

			$ kubectl exec api-rs-a1 -- ls /app/config
			app.properties""";

	private String envSnapshotText() {
		StringBuilder b = new StringBuilder("\n");
		for (K8sModels.ConfigMap cm : cluster.configMaps()) {
			for (var e : new java.util.TreeMap<>(cm.data()).entrySet()) {
				b.append(e.getKey()).append('=').append(e.getValue()).append('\n');
			}
		}
		for (K8sModels.Secret s : cluster.secrets()) {
			for (var e : new java.util.TreeMap<>(s.data()).entrySet()) {
				b.append(e.getKey()).append('=').append(e.getValue()).append('\n');
			}
		}
		return b.toString();
	}

	private String fileSnapshotText() {
		return "\n/app/config/app.properties\n/app/config/. ..\n/run/secrets/db-password\n/run/secrets/. ..";
	}

	private Map<String, String> b64Map(List<K8sModels.Secret> secrets) {
		Map<String, String> out = new java.util.LinkedHashMap<>();
		for (K8sModels.Secret s : secrets) {
			for (var e : s.data().entrySet()) {
				out.put(s.name() + "·" + e.getKey(),
						java.util.Base64.getEncoder().encodeToString(e.getValue().getBytes()));
			}
		}
		return out;
	}

	@PostMapping("/config")
	public String configMount(@RequestParam String mount, Model model) {
		model.addAttribute("mount", mount);
		model.addAttribute("configMaps", cluster.configMaps());
		model.addAttribute("secrets", cluster.secrets());
		model.addAttribute("b64", b64Map(cluster.secrets()));
		model.addAttribute("envManifest", ENV_MANIFEST);
		model.addAttribute("volumeManifest", VOLUME_MANIFEST);
		model.addAttribute("dataSnapshot", envSnapshotText());
		model.addAttribute("fileSnapshot", fileSnapshotText());
		return "fragments :: configMount";
	}

	// ------------------------------------------------------------------
	// Labels + Deployments (1:28:11 · 1:36:01)

	@GetMapping("/deploys")
	public String deploys(@RequestParam(defaultValue = "") String selector, HttpSession session, Model model) {
		mark(session, "deploys");
		model.addAttribute("pageTitle", "Deployments & Self-Healing");
		model.addAttribute("deployment", cluster.deployment("web"));
		model.addAttribute("rsets", cluster.allRsFor(requireWeb()));
		model.addAttribute("revisions", List.of("1 · nginx:1.24", "2 · nginx:1.25"));
		model.addAttribute("images", List.of("nginx:1.23", "nginx:1.24", "nginx:1.25", "nginx:alpine"));
		model.addAttribute("selector", selector);
		Map<String, String> sel = cluster.parseSelector(selector);
		model.addAttribute("selectResult", selector.isBlank() ? List.of()
				: cluster.pods().stream().filter(p -> SimCluster.matches(p.labels(), sel)).toList());
		model.addAttribute("rolloutHistory", "REVISION  CHANGE-CAUSE\n1         nginx:1.24\n2         nginx:1.25");
		model.addAttribute("pods", cluster.pods());
		return "deploys";
	}

	private K8sModels.Deployment requireWeb() {
		return cluster.deployment("web");
	}

	@PostMapping("/deploys")
	public String deploysAction(@RequestParam String action, @RequestParam(defaultValue = "1") int n,
			@RequestParam(defaultValue = "nginx:1.25") String image, Model model) {
		switch (action) {
			case "scale-up" -> cluster.scaleDeployment("web", requireWeb().replicas() + 1);
			case "scale-down" -> cluster.scaleDeployment("web", Math.max(0, requireWeb().replicas() - 1));
			case "update" -> cluster.updateImage("web", image);
			case "undo" -> cluster.rollback("web");
			case "kill" -> killOneWebPod();
			default -> {
			}
		}
		return "redirect:/deploys#result";
	}

	private void killOneWebPod() {
		K8sModels.ReplicaSet rs = cluster.rsFor(requireWeb());
		if (rs == null) {
			return;
		}
		cluster.podsForReplicaSet(rs).stream().findFirst().ifPresent(p -> cluster.killPod(p.namespace(), p.name()));
	}

	// ------------------------------------------------------------------
	// Storage (1:52:34)

	@GetMapping("/storage")
	public String storage(HttpSession session, Model model) {
		mark(session, "storage");
		model.addAttribute("pageTitle", "Storage");
		model.addAttribute("pvs", cluster.pvs());
		model.addAttribute("pvcs", cluster.pvcs());
		model.addAttribute("classes", List.of("standard", "local-ssd", "nfs-storage"));
		model.addAttribute("modes", List.of("RWO", "RWX", "ROX"));
		model.addAttribute("persistentFiles", cluster.persistentFiles());
		model.addAttribute("podFiles", cluster.files(SimCluster.NS_DEFAULT, "api-rs-a1"));
		return "storage";
	}

	@PostMapping("/storage")
	public String storageAction(@RequestParam String action, @RequestParam(defaultValue = "claim") String name,
			@RequestParam(defaultValue = "2") int sizeGi, @RequestParam(defaultValue = "standard") String cls,
			@RequestParam(defaultValue = "RWO") String mode, @RequestParam(defaultValue = "data") String content,
			Model model) {
		if (action.equals("claim")) {
			String bound = cluster.pvs().stream()
					.filter(p -> p.storageClass().equals(cls) && p.accessMode().equals(mode)
							&& p.phase().equals("Available") && p.capacityGi() >= sizeGi)
					.map(K8sModels.Pv::name).findFirst().orElse("");
			cluster.putPvc(new K8sModels.Pvc(SimCluster.NS_DEFAULT, name, sizeGi, cls, mode,
					bound.isEmpty() ? "Pending" : "Bound", bound));
		} else if (action.equals("write-persistent")) {
			cluster.writeFile(SimCluster.NS_DEFAULT, "api-rs-a1", SimCluster.PERSISTENT_PREFIX + name, content);
		} else if (action.equals("write-ephemeral")) {
			cluster.writeFile(SimCluster.NS_DEFAULT, "api-rs-a1", "/data/" + name, content);
		} else if (action.equals("bounce")) {
			// the pods die; only PERSISTENT_PREFIX data survives
			cluster.killPod(SimCluster.NS_DEFAULT, "api-rs-a1");
		}
		return "redirect:/storage#result";
	}

	// ------------------------------------------------------------------
	// Service Basics · port vs targetPort · ClusterIP · NodePort · LoadBalancer

	@GetMapping("/services")
	public String services(@RequestParam(defaultValue = "ClusterIP") String type, HttpSession session, Model model) {
		mark(session, "services");
		model.addAttribute("pageTitle", "Services & Networking");
		model.addAttribute("services", cluster.services());
		model.addAttribute("types", List.of("ClusterIP", "NodePort", "LoadBalancer"));
		model.addAttribute("type", type);
		model.addAttribute("diagram", portChain(type));
		model.addAttribute("curl", "curl -s " + fqdnOf(type));
		model.addAttribute("fqdn", fqdnOf(type));
		return "services";
	}

	private String portChain(String type) {
		String node = (type.equals("NodePort") || type.equals("LoadBalancer")) ? "node:30080 -> " : "";
		return "web-svc " + node + "ClusterIP:80 -> targetPort:8080 -> container/nginx";
	}

	private String fqdnOf(String type) {
		return switch (type) {
			case "NodePort" -> "http://192.168.1.20:30080";
			case "LoadBalancer" -> "http://192.168.1.10";
			default -> "http://web-svc.default.svc.cluster.local";
		};
	}

	@PostMapping("/services")
	public String servicesType(@RequestParam String type, Model model) {
		model.addAttribute("type", type);
		model.addAttribute("services", cluster.services());
		model.addAttribute("types", List.of("ClusterIP", "NodePort", "LoadBalancer"));
		model.addAttribute("diagram", portChain(type));
		model.addAttribute("curl", "curl -s " + fqdnOf(type));
		model.addAttribute("curlOut", "HTTP/1.1 200 OK\nWelcome to nginx!\n(served by pod web-rs-77b7c6d8f9-x" + (1 + Math.abs(type.hashCode() % 3)) + ")");
		return "fragments :: curlOut";
	}

	// ------------------------------------------------------------------
	// Network Policies (2:15:08)

	@GetMapping("/netpol")
	public String netpol(@RequestParam(defaultValue = "web") String target, HttpSession session, Model model) {
		mark(session, "netpol");
		model.addAttribute("pageTitle", "Network Policies");
		model.addAttribute("target", target);
		model.addAttribute("policies", cluster.netPolicies());
		model.addAttribute("labels", distinctLabels());
		if (target.isBlank()) {
			target = "web";
		}
		model.addAttribute("targetApps", List.of("web", "api", "db"));
		return "netpol";
	}

	private List<String> distinctLabels() {
		LinkedHashSet<String> set = new LinkedHashSet<>();
		for (Pod p : cluster.pods()) {
			set.addAll(p.labels());
		}
		return List.copyOf(set);
	}

	@PostMapping("/netpol")
	public String netpolApply(@RequestParam String target, @RequestParam(defaultValue = "Ingress,Egress") String types,
			@RequestParam(defaultValue = "app=db") String from,
			@RequestParam(defaultValue = "dns") String to,
			@RequestParam(defaultValue = "8080") int port, Model model) {
		List<String> typesList = List.of(types.split(","));
		List<String> fromList = from.isBlank() ? List.of() : List.of(from.split(","));
		List<String> toList = to.isBlank() ? List.of() : List.of(to.split(","));
		NetpolService.Result r = netpol.apply(target, typesList, fromList, toList, port);
		model.addAttribute("matrix", r);
		model.addAttribute("target", target);
		model.addAttribute("policies", cluster.netPolicies());
		model.addAttribute("labels", distinctLabels());
		return "fragments :: netpolResult";
	}

	// ------------------------------------------------------------------
	// Ship it (2:41:00 NodePort demo · 2:45:14 Continued Training)

	@GetMapping("/ship")
	public String ship(HttpSession session, Model model) {
		mark(session, "ship");
		model.addAttribute("pageTitle", "Ship it");
		model.addAttribute("getAll", kubectl.run("get all"));
		model.addAttribute("demo", List.of(
				"1. kubectl get nodes                       # cluster is up",
				"2. kubectl apply -f deploy-web.yaml       # zero-to-hero app to the cluster",
				"3. kubectl get pods -w                     # watch it become ready",
				"4. kubectl expose deployment web --type=NodePort --port=80 --target-port=8080",
				"5. kubectl get svc web-nodeport            # find nodePort 30080",
				"6. open http://<node-ip>:30080             # the browser hits the app"));
		model.addAttribute("nextSteps", List.of(
				"Certifications: CKA / CKAD — the video's 'Continued Training' endpoint",
				"kubectl cheat sheet — print it, tape it, use it",
				"HELM — package the manifests so a chart deploys the whole app",
				"Operators & CRDs — extend the API, not the tooling",
				"Service Mesh (Istio/Linkerd) — the story past plain network policies"));
		model.addAttribute("concepts", List.of(
				"Pods", "ReplicaSets", "Deployments", "Services (ClusterIP/NodePort/LB)",
				"ConfigMaps", "Secrets", "PersistentVolumes", "NetworkPolicies", "Namespaces", "Probes"));
		return "ship";
	}

	// ------------------------------------------------------------------
	// Quiz (self-grading)

	public record QuizItem(String q, List<String> options, int answer, String why) {
	}

	private static final List<QuizItem> QUIZ = List.of(
			new QuizItem("What is the only component that ever writes to etcd?",
					List.of("The scheduler", "The API server", "The kubelet", "kubectl itself"), 1,
					"Every write — even a kubelet's pod status — goes through the API server, which is the single writer to etcd."),
			new QuizItem("kubectl apply sends the cluster a…",
					List.of("Series of imperative steps", "Declared desired state (a manifest)", "Pulled container image", "Script of API calls"), 1,
					"Manifests declare the desired state; controllers reconcile reality toward it. That is declarative, not imperative."),
			new QuizItem("Which group+version hosts the Deployment kind?",
					List.of("v1", "apps/v1", "batch/v1", "autoscaling/v1"), 1,
					"Deployments moved to apps/v1 (beta versions apps/v1beta1 were removed in 1.16)."),
			new QuizItem("You kubectl delete a pod that a Deployment owns. What happens?",
					List.of("Nothing — pods cannot be deleted", "The ReplicaSet creates a replacement pod", "The whole Deployment is deleted", "The Service drops it"), 1,
					"The ReplicaSet controller sees fewer than desired replicas and makes a new pod — self-healing."),
			new QuizItem("A standalone pod (no controller) is deleted. What happens?",
					List.of("A replacement is created automatically", "It stays deleted", "The node restarts it", "It becomes a Job"), 1,
					"No controller, no reconciliation — the pod is gone. That is why you run workloads through Deployments."),
			new QuizItem("'Guaranteed' QoS class requires…",
					List.of("requests = limits on CPU and memory for every container", "a startup probe", "limits only, no requests", "requests only, no limits"), 0,
					"Requests equal to limits on both CPU and memory => Guaranteed. Otherwise BestEffort or Burstable."),
			new QuizItem("A liveness probe is for…",
					List.of("deciding which pods get traffic", "restarting unhealthy containers", "checking cluster health", "scaling the deployment"), 1,
					"Liveness restarts a deadlocked/stuck container; readiness decides whether it receives traffic."),
			new QuizItem("What does a readiness probe do?",
					List.of("Kills a slow container", "Removes a pod from Service endpoints until ready", "Pulls the image again", "Reboots the node"), 1,
					"A pod that fails readiness stays Running but receives no traffic."),
			new QuizItem("kubectl exec allows you to…",
					List.of("run a command inside a running container", "execute scripts on the node", "change pod images at runtime", "copy files into etcd"), 0,
					"exec runs a process in a container — great for debugging, never for production schema fixes."),
			new QuizItem("What does kubectl cp do?",
					List.of("Copies kubeconfig between clusters", "Copies files between your machine and a pod", "Copies pods between nodes", "Duplicates a ReplicaSet"), 1,
					"kubectl cp <file> <pod>:<path> moves files in, and out the reverse."),
			new QuizItem("ConfigMaps store…",
					List.of("secrets", "plain, non-sensitive configuration", "container images", "RBAC roles"), 1,
					"ConfigMaps hold non-sensitive config (env vars, config files). Secrets hold sensitive data."),
			new QuizItem("Are Secret values encrypted inside etcd by default?",
					List.of("Yes, always", "No — they are base64-encoded, which is encoding not encryption", "Only with Opaque type", "Only at rest on the node"), 1,
					"base64 is obfuscation, easily decoded. Real protection needs encryption at rest and strict RBAC."),
			new QuizItem("Mounting a Secret as a volume vs injecting it as an env var — which allows live updates?",
					List.of("Env vars update live", "Volume: the mounted files update and its projection can be made immutable after", "Neither", "Both automatically"), 1,
					"Env vars are fixed at pod creation; a mounted volume-like Secret is updated by kubelet (with delay)."),
			new QuizItem("emptyDir data lives…",
					List.of("until the pod is deleted", "forever, it is a persistent volume", "until the node reboots only", "in etcd"), 0,
					"emptyDir is a scratch volume with the pod's lifetime — data is gone when the pod leaves the node."),
			new QuizItem("Which of these maps a Service to its pods?",
					List.of("The namespace rule", "The label selector (spec.selector)", "The ClusterIP", "The nodePort"), 1,
					"The selector picks endpoint pods; Services use the selector to build their endpoint list."),
			new QuizItem("A Service with type ClusterIP is reachable…",
					List.of("from outside the cluster", "only inside the cluster via a stable virtual IP + DNS name", "only on a single node", "via its nodePort"), 1,
					"ClusterIP is stable, internal; NodePort exposes nodeIP:port outside; LoadBalancer adds a cloud LB."),
			new QuizItem("port vs targetPort: port is what the Service listens on; targetPort is…",
					List.of("the node's listening port", "the port the pod/container listens on", "the random port range 30000-32767", "the HTTPS port"), 1,
					"targetPort is the container port the traffic is forwarded to; they may differ."),
			new QuizItem("What is a nodePort restricted to?",
					List.of("Any 1-65535 port", "30000-32767", "443 only", "8080"), 1,
					"NodePort is cluster-wide and must be within 30000-32767 unless you over-ride the range."),
			new QuizItem("A NetworkPolicy that selects a pod and lists Ingress but no ingress rule — the result?",
					List.of("It changes nothing", "It denies all ingress to that pod", "It allows everything", "Pods get removed"), 1,
					"Empty ingress list on a selected pod = deny all ingress. Kubernetes is default-open until a policy opts in."),
			new QuizItem("The most important mindset shift of the whole video?",
					List.of("kubectl is the only tool you need", "You declare the desired state; the control plane converges on it", "YAML indentation is a suggestion", "Containers are VMs"), 1,
					"Everything else follows: manifests define desired state, controllers reconcile, clusters heal."));

	@GetMapping("/quiz")
	public String quiz(HttpSession session, Model model) {
		mark(session, "quiz");
		model.addAttribute("pageTitle", "Quiz");
		model.addAttribute("quiz", QUIZ);
		return "quiz";
	}

	@PostMapping("/quiz")
	public String quizGrade(@RequestParam Map<String, String> answers, Model model) {
		int score = 0;
		List<Boolean> correct = new ArrayList<>();
		for (int i = 0; i < QUIZ.size(); i++) {
			String given = answers.get("q" + i);
			boolean ok = given != null && !given.isEmpty() && Integer.parseInt(given) == QUIZ.get(i).answer();
			correct.add(ok);
			if (ok) {
				score++;
			}
		}
		model.addAttribute("quiz", QUIZ);
		model.addAttribute("correct", correct);
		model.addAttribute("score", score);
		model.addAttribute("total", QUIZ.size());
		return "quiz";
	}

	// ------------------------------------------------------------------
	// Flashcards

	public record Flashcard(String front, String back) {
	}

	private static final List<Flashcard> DECK = List.of(
			new Flashcard("Control plane", "The 'brain' of the cluster: API server, scheduler, controller-manager, etcd, cloud-controller."),
			new Flashcard("API server", "Front door + single writer to etcd. Validates, authorizes, stores desired state."),
			new Flashcard("Scheduler", "Decides which node runs a new pod based on requests, affinity, taints."),
			new Flashcard("Kubelet", "Node agent: watches assigned pods and drives the container runtime."),
			new Flashcard("Manifest / desired state", "A YAML file describing what you *want*; controllers converge reality toward it."),
			new Flashcard("Pod", "The smallest runnable unit — one or more containers sharing a network + storage."),
			new Flashcard("Namespace", "A virtual cluster partition isolating resources and names (default, kube-system…)."),
			new Flashcard("kubectl apply -f", "Declarative apply: sends a manifest; the cluster reconciles toward the desired state."),
			new Flashcard("ReplicaSet", "Keeps a stable set of identical pods for a Deployment."),
			new Flashcard("Deployment", "Declarative rollout/rollback manager for ReplicaSets."),
			new Flashcard("Replicas", "How many identical pods 'desired' — the controller makes/removes pods to match."),
			new Flashcard("Self-healing", "Kill a pod owned by a Deployment and a fresh one appears automatically."),
			new Flashcard("Rollout", "A controlled image update. rollingUpdate keeps old pods alive while new ones become ready."),
			new Flashcard("Rollout undo", "Roll back to the previous revision — the history is kept, so reversion is one command away."),
			new Flashcard("Requests vs Limits", "Requests guarantee scheduling room; limits cap usage. Over limit = throttled (CPU) or OOM kill (memory)."),
			new Flashcard("QoS classes", "Guaranteed (req==lim), Burstable (req<lim/some), BestEffort (none) — eviction order lowest to highest priority."),
			new Flashcard("Liveness probe", "Tells the kubelet when a container is stuck and must be restarted."),
			new Flashcard("Readiness probe", "Tells the service which pods may receive traffic; failing readiness pauses traffic."),
			new Flashcard("Startup probe", "Pauses liveness/readiness while a container boots slowly — protects slow app startup."),
			new Flashcard("kubectl exec", "Run a command inside a running container (debug: ls, cat, env)."),
			new Flashcard("kubectl cp", "Copy files between your machine and a pod."),
			new Flashcard("kubectl logs -f", "Stream stdout/stderr of a container; add --container, --timestamps, --previous."),
			new Flashcard("ConfigMap", "Non-sensitive configuration injected as env or file into pods."),
			new Flashcard("Secret", "Sensitive data, base64-encoded at rest (NOT encryption), mounted as env or file."),
			new Flashcard("emptyDir", "Scratch volume with the pod's lifetime — deleted with the pod."),
			new Flashcard("PersistentVolume + PVC", "PV = storage; PVC = its request. A bound PVC survives pod restarts."),
			new Flashcard("StorageClass", "Template for dynamic provisioning (standard, local-ssd, etc.)."),
			new Flashcard("Service", "Stable virtual IP + DNS for a set of pods selected by label. Types: ClusterIP, NodePort, LoadBalancer."),
			new Flashcard("nodePort 30000-32767", "Cluster-wide port that forwards node traffic to the Service, then to pods."),
			new Flashcard("port vs targetPort", "port = the Service listens on; targetPort = the container port it forwards to."),
			new Flashcard("NetworkPolicy", "Whitelists pod-to-pod traffic. A selected pod with no allow rule = denied (default-deny)."),
			new Flashcard("default-deny", "Deny-all is the safe baseline; each allow rule re-opens just the traffic you need."));

	@GetMapping("/flashcards")
	public String flashcards(HttpSession session, Model model) {
		mark(session, "flashcards");
		model.addAttribute("pageTitle", "Flashcards");
		model.addAttribute("deck", DECK);
		return "flashcards";
	}

	// ------------------------------------------------------------------
	// Sources

	public record Source(int order, String chapter, String at, String videoMin, String sim) {
	}

	private static final List<Source> SOURCES = List.of(
			new Source(1, "K8s Architecture & Purpose", "0:01:42", "041", "arch"),
			new Source(2, "YAML", "0:21:02", "1:20", "manifest"),
			new Source(3, "Manifests", "0:24:52", "1:24", "manifest"),
			new Source(4, "Making Pods", "0:28:55", "1:28", "pods"),
			new Source(5, "Namespaces", "0:34:00", "1:34", "pods"),
			new Source(6, "API Versioning", "0:38:58", "1:38", "manifest"),
			new Source(7, "Resource Monitoring", "0:41:43", "1:41", "resources"),
			new Source(8, "Requests & Limits", "0:44:56", "1:44", "resources"),
			new Source(9, "Probes", "0:51:01", "1:51", "probes"),
			new Source(10, "kubectl exec & cp", "0:58:20", "1:58", "exec"),
			new Source(11, "ConfigMaps", "1:05:34", "2:05", "config"),
			new Source(12, "Volume Mounting", "1:09:49", "2:09", "config"),
			new Source(13, "Secrets", "1:18:05", "2:18", "config"),
			new Source(14, "Viewing Logs", "1:21:16", "2:21", "logs"),
			new Source(15, "Labels", "1:28:11", "2:28", "deploys"),
			new Source(16, "Deployments", "1:36:01", "2:36", "deploys"),
			new Source(17, "Storage", "1:52:34", "2:52", "storage"),
			new Source(18, "Service Basics", "2:08:12", "3:08", "services"),
			new Source(19, "Network Policies", "2:15:08", "3:15", "netpol"),
			new Source(20, "Service Port vs TargetPort", "2:26:29", "3:26", "services"),
			new Source(21, "ClusterIP Services", "2:29:19", "3:29", "services"),
			new Source(22, "NodePort Services", "2:36:48", "3:36", "services"),
			new Source(23, "LoadBalancer Services", "2:39:15", "3:39", "services"),
			new Source(24, "NodePort Service Demonstration", "2:41:00", "3:41", "ship"),
			new Source(25, "Continued Training!", "2:45:14", "3:45", "ship"));

	@GetMapping("/sources")
	public String sources(HttpSession session, Model model) {
		mark(session, "sources");
		model.addAttribute("pageTitle", "Sources");
		model.addAttribute("sources", SOURCES);
		model.addAttribute("videoUrl", "https://www.youtube.com/watch?v=MTHGoGUFpvE");
		model.addAttribute("videoTitle", "Kubernetes Zero to Hero: The Complete Beginner's Guide (2025 Edition)");
		return "sources";
	}
}