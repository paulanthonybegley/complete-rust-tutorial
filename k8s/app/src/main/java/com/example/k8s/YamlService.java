package com.example.k8s;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.k8s.K8sModels.ConfigMap;
import com.example.k8s.K8sModels.Container;
import com.example.k8s.K8sModels.Deployment;
import com.example.k8s.K8sModels.NetPolicy;
import com.example.k8s.K8sModels.Pod;
import com.example.k8s.K8sModels.Pvc;
import com.example.k8s.K8sModels.ReplicaSet;
import com.example.k8s.K8sModels.Secret;
import com.example.k8s.K8sModels.Svc;

/**
 * The Manifest lab (0:24:52 YAML, 0:21:02 Manifests, 0:38:58 API versioning).
 * Validates the student's YAML the way the API server would — schema check
 * (indentation, apiVersion, kind, required fields) then "apply" mutates the
 * simulated cluster exactly like kubectl apply -f.
 */
@Service
public class YamlService {

	private static final Set<String> KNOWN_KINDS = Set.of(
			"Pod", "Deployment", "ReplicaSet", "Service", "ConfigMap", "Secret",
			"PersistentVolumeClaim", "PersistentVolume", "NetworkPolicy", "Namespace");

	private static final Set<String> KNOWN_API = Set.of(
			"v1", "apps/v1", "networking.k8s.io/v1", "storage.k8s.io/v1", "rbac.authorization.k8s.io/v1");

	private static final Map<String, String> DEPRECATED = Map.of(
			"apps/v1beta1", "apps/v1beta1 was removed in Kubernetes 1.16 — use apps/v1",
			"apps/v1beta2", "apps/v1beta2 was removed in Kubernetes 1.16 — use apps/v1",
			"extensions/v1beta1", "extensions/v1beta1 was removed in Kubernetes 1.16 — use apps/v1",
			"v1beta1", "v1beta1 was removed — use the stable group (e.g. networking.k8s.io/v1)");

	private final SimCluster cluster;

	public YamlService(SimCluster cluster) {
		this.cluster = cluster;
	}

	// ------------------------------------------------------------------
	// presets — these mirror the real files shipped in the k8s/lab folder

	public static final String POD_NGINX = "pod-nginx.yaml";
	public static final String DEPLOY_WEB = "deploy-web.yaml";
	public static final String SVC_CLUSTERIP = "svc-clusterip.yaml";
	public static final String CM_APP = "cm-app.yaml";
	public static final String SECRET_APP = "secret-app.yaml";
	public static final String PVC_WEB = "pvc-web.yaml";
	public static final String NETPOL_ALLOW = "netpol-allow-api.yaml";
	public static final String NS_ANALYTICS = "ns-analytics.yaml";

	public List<String> presetNames() {
		return List.of(POD_NGINX, DEPLOY_WEB, SVC_CLUSTERIP, CM_APP, SECRET_APP, PVC_WEB, NETPOL_ALLOW, NS_ANALYTICS);
	}

	public String preset(String name) {
		String yaml = switch (name == null ? "" : name) {
			case POD_NGINX -> POD_NGINX_YAML;
			case DEPLOY_WEB -> DEPLOY_WEB_YAML;
			case SVC_CLUSTERIP -> SVC_CLUSTERIP_YAML;
			case CM_APP -> CM_APP_YAML;
			case SECRET_APP -> SECRET_APP_YAML;
			case PVC_WEB -> PVC_WEB_YAML;
			case NETPOL_ALLOW -> NETPOL_ALLOW_YAML;
			case NS_ANALYTICS -> NS_ANALYTICS_YAML;
			default -> POD_NGINX_YAML;
		};
		return yaml;
	}

	// ------------------------------------------------------------------
	// validation

	/** Returns a list of problems; empty list = "validated, ready to apply". */
	public List<String> validate(String content) {
		List<String> errors = new ArrayList<>();
		if (content == null || content.isBlank()) {
			errors.add("Manifest is empty — nothing to validate.");
			return errors;
		}
		String[] lines = content.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.contains("\t")) {
				errors.add("line " + (i + 1) + ": tabs are invalid in YAML — indent with two spaces");
			}
			int lead = 0;
			while (lead < line.length() && line.charAt(lead) == ' ') {
				lead++;
			}
			if (line.isBlank()) {
				continue;
			}
			if (lead % 2 != 0) {
				errors.add("line " + (i + 1) + ": indentation must be a multiple of 2 spaces" + paste(line));
			}
		}
		String api = valueOf(lines, "apiVersion");
		String kind = valueOf(lines, "kind");
		if (api == null) {
			errors.add("missing required field: apiVersion");
		} else if (DEPRECATED.containsKey(api.trim())) {
			errors.add("apiVersion \"" + api.trim() + "\" is deprecated — " + DEPRECATED.get(api.trim()));
		} else if (!KNOWN_API.contains(api.trim())) {
			errors.add("unknown apiVersion \"" + api.trim() + "\" — use one of " + String.join(", ", KNOWN_API));
		}
		if (kind == null) {
			errors.add("missing required field: kind");
		} else if (!KNOWN_KINDS.contains(kind.trim())) {
			errors.add("unknown kind \"" + kind.trim() + "\" — known kinds: " + String.join(", ", KNOWN_KINDS));
		}
		String name = valueOf(lines, "name");
		if (name == null) {
			errors.add("metadata.name is required");
		} else if (!name.trim().matches("[a-z0-9.-]+")) {
			errors.add("metadata.name must be lowercase DNS-1123 (letters, digits, '-', '.')");
		}
		if (kind != null && (kind.trim().equals("Pod") || kind.trim().equals("Deployment"))) {
			String image = valueOf(lines, "image");
			if (image == null) {
				errors.add("spec.containers[0].image is required");
			} else if (!image.trim().contains("/") && !image.trim().matches("[a-z0-9.:-]+")) {
				errors.add("image \"" + image.trim() + "\" does not look like a registry image reference");
			}
		}
		return errors;
	}

	private String paste(String line) {
		String t = line.trim();
		if (t.length() > 40) {
			t = t.substring(0, 40) + "…";
		}
		return "  (got: \"" + t + "\")";
	}

	private String valueOf(String[] lines, String key) {
		for (String line : lines) {
			String t = line.trim();
			if (t.startsWith(key + ":") && !t.startsWith(key + ":" + " ") || t.startsWith(key + ": ")) {
				int idx = t.indexOf(':');
				if (idx > 0) {
					String rest = t.substring(idx + 1).trim();
					if (!rest.startsWith("[") && !rest.isBlank() && !rest.startsWith("|")
							&& !rest.startsWith(">") && !rest.startsWith("-")) {
						return rest;
					}
				}
			}
		}
		return null;
	}

	// ------------------------------------------------------------------
	// apply

	/** Applies a validated manifest to the simulated cluster. Returns the created-object banner. */
	public String apply(String content) {
		List<String> fixed = validate(content);
		if (!fixed.isEmpty()) {
			return "error: manifest invalid — cannot apply:\n  - " + String.join("\n  - ", fixed);
		}
		String[] lines = content.split("\n");
		String kind = valueOf(lines, "kind").trim();
		String name = valueOf(lines, "name").trim();
		String ns = valueOf(lines, "namespace");
		String api = valueOf(lines, "apiVersion").trim();
		String namespace = ns == null ? SimCluster.NS_DEFAULT : ns.trim();

		return switch (kind) {
			case "Pod" -> applyPod(namespace, name, lines);
			case "Deployment" -> applyDeployment(namespace, name, lines);
			case "Service" -> applyService(namespace, name, lines, api);
			case "ConfigMap" -> applyConfigMap(namespace, name, lines);
			case "Secret" -> applySecret(namespace, name, lines);
			case "PersistentVolumeClaim" -> applyPvc(namespace, name, lines);
			case "NetworkPolicy" -> applyNetPol(namespace, name, rows(lines, "podSelector", 4), lines);
			case "Namespace" -> applyNamespace(name);
			default -> "error: cannot apply kind " + kind;
		};
	}

	private String applyPod(String ns, String name, String[] lines) {
		String image = valueOf(lines, "image");
		cluster.putPod(new Pod(ns, name, image, "Running", 0, true,
				100, 200, 64, 128, 50, 60, cluster.now(),
				List.of(new Container(name, image)), List.of("app=" + name), null));
		return "pod/" + name + " created";
	}

	private String applyDeployment(String ns, String name, String[] lines) {
		String image = valueOf(lines, "image");
		int replicas = intValue(lines, "replicas", 1);
		String rs = name + SimCluster.RS_TOKEN + "rev-1";
		cluster.putPod(new Pod(ns, rs + "-x1", image, "Running", 0, true,
				250, 500, 128, 256, 140, 160, cluster.now(),
				List.of(new Container(name, image)), List.of("app=" + name), null));
		// write straight into cluster stores via public mutators
		cluster.putReplicaSet(new ReplicaSet(ns, rs, name, image, replicas, replicas));
		cluster.putDeployment(new Deployment(ns, name, image, replicas, replicas, replicas, replicas,
				"RollingUpdate", 1, 1, image, List.of("app=" + name)));
		return "deployment.apps/" + name + " created";
	}

	private String applyService(String ns, String name, String[] lines, String api) {
		String type = valueOf(lines, "type");
		if (type == null) {
			type = "ClusterIP";
		}
		int port = intValue(lines, "port", 80);
		int target = intValue(lines, "targetPort", port);
		String selector = linesValueOf(lines, "selector");
		Map<String, String> sel = cluster.parseSelector(selector);
		List<String> endpoints = cluster.podsMatchingSelector(sel).stream()
				.map(p -> "10.0.0." + (Math.abs(p.name().hashCode()) % 200 + 20) + ":" + target).toList();
		String nodePort = type.equals("NodePort") || type.equals("LoadBalancer") ? "30080" : "0";
		String ext = type.equals("LoadBalancer") ? "192.168.1.10" : "<none>";
		cluster.putSvc(new Svc(ns, name, type, "10.96.0." + (cluster.services().size() + 10), ext,
				port, target, Integer.parseInt(nodePort), selector, endpoints, List.of()));
		return "service/" + name + " created";
	}

	private String applyConfigMap(String ns, String name, String[] lines) {
		Map<String, String> data = dataBlock(lines, "data");
		cluster.putConfigMap(new ConfigMap(ns, name, data, cluster.now()));
		return "configmap/" + name + " created";
	}

	private String applySecret(String ns, String name, String[] lines) {
		Map<String, String> data = dataBlock(lines, "data");
		cluster.putSecret(new Secret(ns, name, data, cluster.now()));
		return "secret/" + name + " created";
	}

	private String applyPvc(String ns, String name, String[] lines) {
		int gi = intValue(lines, "storage", 1);
		String cls = valueOf(lines, "storageClassName");
		String mode = valueOf(lines, "accessModes");
		if (mode == null) {
			mode = "RWO";
		}
		String clsName = cls == null ? "standard" : cls.trim();
		final String modeF = mode;
		final int giF = gi;
		// dynamic provisioning: bind to the first matching Available PV
		String bound = cluster.pvs().stream()
				.filter(p -> p.storageClass().equals(clsName) && p.accessMode().equals(modeF)
						&& p.phase().equals("Available") && p.capacityGi() >= giF)
				.map(K8sModels.Pv::name).findFirst().orElse("");
		String phase = bound.isEmpty() ? "Pending" : "Bound";
		cluster.putPvc(new Pvc(ns, name, gi, clsName, mode, phase, bound));
		return "persistentvolumeclaim/" + name + " created";
	}

	private String applyNetPol(String ns, String name, List<String> podSel, String[] lines) {
		List<String> types = new ArrayList<>();
		for (String line : lines) {
			String t = line.trim();
			if (t.equals("- Ingress")) {
				types.add("Ingress");
			}
			if (t.equals("- Egress")) {
				types.add("Egress");
			}
		}
		List<String> ingressFrom = new ArrayList<>();
		for (String line : lines) {
			String t = line.trim();
			if (t.startsWith("app: ")) {
				ingressFrom.add(t);
			}
		}
		int port = intValue(lines, "port", 8080);
		List<String> podSelector = podSel.isEmpty() ? List.of("<all pods>") : podSel;
		cluster.putNetPol(new NetPolicy(ns, name, podSelector, types, ingressFrom, List.of(), port));
		return "networkpolicy.networking.k8s.io/" + name + " created";
	}

	private String applyNamespace(String name) {
		cluster.createNamespace(name);
		return "namespace/" + name + " created";
	}

	private List<String> rows(String[] lines, String key, int indent) {
		List<String> out = new ArrayList<>();
		boolean in = false;
		for (String line : lines) {
			String t = line.trim();
			if (t.equals(key + ":")) {
				in = true;
				continue;
			}
			if (in) {
				if (!t.startsWith("-") && !line.startsWith("    ") && !line.isBlank()) {
					break;
				}
				if (t.startsWith("app: ")) {
					out.add(t);
				}
			}
		}
		return out;
	}

	private int intValue(String[] lines, String key, int def) {
		String v = valueOf(lines, key);
		if (v == null) {
			return def;
		}
		String num = v.replaceAll("[^0-9]", "");
		if (num.isEmpty()) {
			return def;
		}
		return Integer.parseInt(num);
	}

	private String linesValueOf(String[] lines, String key) {
		// "selector:" followed by indented "app: web" lines -> "app=web"
		StringBuilder sb = new StringBuilder();
		boolean in = false;
		for (String line : lines) {
			if (line.trim().equals(key + ":")) {
				in = true;
				continue;
			}
			if (in) {
				if (line.isBlank()) {
					continue;
				}
				if (!line.startsWith(" ") || line.trim().contains(":")) {
					String t = line.trim();
					if (t.startsWith("app: ")) {
						sb.append("app=").append(t.substring(5)).append(",");
						continue;
					}
					if (t.startsWith("tier: ")) {
						sb.append("tier=").append(t.substring(6)).append(",");
						continue;
					}
					break;
				}
			}
		}
		String s = sb.toString();
		return s.endsWith(",") ? s.substring(0, s.length() - 1) : s;
	}

	private Map<String, String> dataBlock(String[] lines, String block) {
		Map<String, String> out = new LinkedHashMap<>();
		boolean in = false;
		for (int i = 0; i < lines.length; i++) {
			String t = lines[i].trim();
			if (t.equals(block + ":")) {
				in = true;
				continue;
			}
			if (in) {
				if (t.startsWith("    ") || (t.contains(":") && !t.startsWith("-") && !t.startsWith("apiVersion"))) {
					int idx = t.indexOf(':');
					if (idx > 0) {
						String k = t.substring(0, idx).trim();
						String v = t.substring(idx + 1).trim();
						if (!k.isEmpty() && !k.contains(" ")) {
							out.put(k, v);
						}
					}
				}
			}
		}
		return out;
	}

	// ------------------------------------------------------------------
	// yaml -> json for the transcript preview

	public String toJson(String content) {
		String[] lines = (content == null ? "" : content).split("\n");
		StringBuilder out = new StringBuilder("{\n");
		boolean first = true;
		for (String line : lines) {
			String t = line.trim();
			if (t.startsWith("#") || t.isBlank() || t.startsWith("- ")) {
				continue;
			}
			int idx = t.indexOf(':');
			if (idx <= 0) {
				continue;
			}
			String k = t.substring(0, idx).trim();
			String v = t.substring(idx + 1).trim();
			if (!first) {
				out.append(",\n");
			}
			first = false;
			if (v.isEmpty()) {
				out.append("  \"").append(k).append("\": { ... }");
			} else {
				out.append("  \"").append(k).append("\": \"").append(escape(v)).append("\"");
			}
		}
		out.append("\n}");
		return out.toString();
	}

	private String escape(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	// ------------------------------------------------------------------
	// preset manifest texts (mirrors of k8s/lab/*.yaml)

	public static final String POD_NGINX_YAML = """
			apiVersion: v1
			kind: Pod
			metadata:
			  name: nginx-static
			  labels:
			    app: nginx-static
			spec:
			  containers:
			    - name: nginx
			      image: nginx:1.24
			""".indent(0).stripTrailing();

	public static final String DEPLOY_WEB_YAML = """
			apiVersion: apps/v1
			kind: Deployment
			metadata:
			  name: web
			  labels:
			    app: web
			spec:
			  replicas: 2
			  selector:
			    matchLabels:
			      app: web
			  template:
			    metadata:
			      labels:
			        app: web
			    spec:
			      containers:
			        - name: nginx
			          image: nginx:1.24
			""".stripTrailing();

	public static final String SVC_CLUSTERIP_YAML = """
			apiVersion: v1
			kind: Service
			metadata:
			  name: web-svc
			spec:
			  type: ClusterIP
			  selector:
			    app: web
			  ports:
			    - port: 80
			      targetPort: 8080
			""".stripTrailing();

	public static final String CM_APP_YAML = """
			apiVersion: v1
			kind: ConfigMap
			metadata:
			  name: app-config
			data:
			  APP_COLOR: green
			  LOG_LEVEL: debug
			  GREETING: configured via manifest
			""".stripTrailing();

	public static final String SECRET_APP_YAML = """
			apiVersion: v1
			kind: Secret
			metadata:
			  name: app-secret
			type: Opaque
			stringData:
			  DB_PASSWORD: hunter2
			  API_KEY: xyz-987
			""".stripTrailing();

	public static final String PVC_WEB_YAML = """
			apiVersion: v1
			kind: PersistentVolumeClaim
			metadata:
			  name: web-pvc2
			spec:
			  accessModes:
			    - ReadWriteOnce
			  storageClassName: local-ssd
			  resources:
			    requests:
			      storage: 4Gi
			""".stripTrailing();

	public static final String NETPOL_ALLOW_YAML = """
			apiVersion: networking.k8s.io/v1
			kind: NetworkPolicy
			metadata:
			  name: allow-api
			spec:
			  podSelector:
			    matchLabels:
			      app: api
			  policyTypes:
			    - Ingress
			  ingress:
			    - from:
			        - podSelector:
			            matchLabels:
			              app: web
			      ports:
			        - port: 8080
			""".stripTrailing();

	public static final String NS_ANALYTICS_YAML = """
			apiVersion: v1
			kind: Namespace
			metadata:
			  name: analytics
			""".stripTrailing();
}