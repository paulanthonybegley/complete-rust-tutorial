package com.example.k8s;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.k8s.K8sModels.NetPolicy;

/**
 * NetworkPolicy station (2:15:08). A policy builder: the learner picks the
 * target pods, the policy types and the allowed sources/destinations, and this
 * service writes the policy into the simulated cluster and computes the
 * traffic matrix those rules produce.
 *
 * The rule engine is deliberately simple and honest:
 *   - a network is open by default;
 *   - once a NetworkPolicy selects a pod and includes Ingress/Egress,
 *     everything not explicitly allowed by that policy (or another one that
 *     also selects the pod) is denied.
 */
@Service
public class NetpolService {

	public record MatrixCell(String source, String verdict, String rule) {
	}

	public record Result(List<MatrixCell> ingressCells, List<MatrixCell> egressCells, String yaml) {
	}

	private final SimCluster cluster;

	public NetpolService(SimCluster cluster) {
		this.cluster = cluster;
	}

	private static final List<String> SOURCES = List.of("app=db", "app=api", "app=web", "internet");

	public Result apply(String targetApp, List<String> policyTypes, List<String> ingressFrom,
			List<String> egressTo, int port) {
		List<String> types = policyTypes == null || policyTypes.isEmpty()
				? List.of("Ingress", "Egress") : policyTypes;
		List<String> from = ingressFrom == null ? List.of() : ingressFrom;
		List<String> to = egressTo == null ? List.of() : egressTo;
		List<String> targetSel = targetApp == null || targetApp.isBlank()
				? List.of("<all pods>") : List.of("app=" + targetApp);
		String safe = targetApp == null || targetApp.isBlank() ? "all" : targetApp.replaceAll("[^a-z0-9-]", "");

		cluster.putNetPol(new NetPolicy(SimCluster.NS_DEFAULT, "allow-" + safe, targetSel, types, from, to, port));
		NetPolicy applied = cluster.netPolicies().stream()
				.filter(p -> p.name().equals("allow-" + safe)).findFirst().orElse(null);

		List<MatrixCell> ingress = new ArrayList<>();
		boolean ingressOn = applied.policyTypes().contains("Ingress");
		for (String src : SOURCES) {
			String verdict;
			String rule;
			if (!ingressOn) {
				verdict = "ALLOWED";
				rule = "policy does not manage Ingress — pods keep the open default";
			} else if (from.contains("any")) {
				verdict = "ALLOWED";
				rule = "ingress allows everything";
			} else if (src.equals("internet")) {
				verdict = from.contains("internet") ? "ALLOWED" : "DENIED";
				rule = from.contains("internet") ? "ingress allows internet traffic" : "no ipBlock/internet rule (node-level traffic \"external\")";
			} else if (from.contains(src)) {
				verdict = "ALLOWED";
				rule = "ingress allows " + src;
			} else {
				verdict = "DENIED";
				rule = "not listed in ingress.from";
			}
			ingress.add(new MatrixCell(src, verdict, rule));
		}

		List<MatrixCell> egress = new ArrayList<>();
		boolean egressOn = applied.policyTypes().contains("Egress");
		for (String dst : SOURCES) {
			String verdict;
			String rule;
			if (!egressOn) {
				verdict = "ALLOWED";
				rule = "policy does not manage Egress";
			} else if (to.contains("any")) {
				verdict = "ALLOWED";
				rule = "egress allows everything";
			} else if (dst.equals("internet")) {
				verdict = to.contains("internet") ? "ALLOWED" : "DENIED";
				rule = to.contains("internet") ? "egress allows DNS/AWS metdata reachout" : "egress to the internet blocked";
			} else if (to.contains("dns")) {
				verdict = "ALLOWED";
				rule = "egress allows kube-dns (" + dst + ")";
			} else if (to.contains(dst)) {
				verdict = "ALLOWED";
				rule = "egress allows " + dst;
			} else {
				verdict = "DENIED";
				rule = "not listed in egress.to";
			}
			egress.add(new MatrixCell(dst, verdict, rule));
		}

		return new Result(ingress, egress, yamlOf(targetApp, types, from, to, port, targetSel));
	}

	public String yamlOf(String targetApp, List<String> types, List<String> from, List<String> to,
			int port, List<String> targetSel) {
		StringBuilder b = new StringBuilder();
		b.append("apiVersion: networking.k8s.io/v1\n");
		b.append("kind: NetworkPolicy\n");
		b.append("metadata:\n");
		b.append("  name: allow-").append(targetApp == null ? "all" : targetApp).append("\n");
		b.append("  namespace: default\n");
		b.append("spec:\n");
		b.append("  podSelector:\n");
		b.append("    matchLabels:\n");
		b.append("      app: ").append(targetApp == null ? "*" : targetApp).append("\n");
		b.append("  policyTypes:\n");
		for (String t : types) {
			b.append("    - ").append(t).append("\n");
		}
		if (types.contains("Ingress")) {
			b.append("  ingress:\n");
			if (from.isEmpty() || from.contains("none")) {
				b.append("    [] # empty list => ALL ingress denied\n");
			} else if (from.contains("any")) {
				b.append("    - {} # allow all ingress\n");
			} else {
				for (String f : from) {
					if (f.equals("internet")) {
						continue;
					}
					b.append("    - from:\n");
					b.append("        - podSelector:\n");
					b.append("            matchLabels:\n");
					b.append("              ").append(f).append("\n");
				}
			}
		}
		if (types.contains("Egress")) {
			b.append("  egress:\n");
			if (to.isEmpty() || to.contains("none")) {
				b.append("    [] # empty list => ALL egress denied\n");
			} else if (to.contains("any")) {
				b.append("    - {} # allow all egress\n");
			} else {
				for (String d : to) {
					if (d.equals("internet")) {
						continue;
					}
					b.append("    - to:\n");
					b.append("        - podSelector:\n");
					b.append("            matchLabels:\n");
					b.append("              ").append(d).append("\n");
				}
			}
		}
		if (port > 0) {
			b.append("    ## ports are restricted: only ").append(port).append("/TCP\n");
		}
		return b.toString();
	}
}