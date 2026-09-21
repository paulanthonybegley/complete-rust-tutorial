package com.example.devops.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.devops.model.K8sResult;
import com.example.devops.model.PodState;

/**
 * Kubernetes reconciler simulator: declared replicas become running pods,
 * "kill a pod" trips the self-heal counter, and a rolling update switches the
 * generation without a service blip.
 */
@Service
public class K8sService {

	public K8sResult reconcile(int desired, Integer killPod, boolean rolling) {
		int reps = Math.max(1, Math.min(5, desired));
		boolean kill = killPod != null && killPod >= 1 && killPod <= reps;

		int respawned = kill ? respawns.count() : respawns.total();
		boolean newest = rolling || kill;
		String gen = newest ? "aaaaa" : "7d9b5";

		List<PodState> pods = new ArrayList<>();
		int ready = reps;
		if (kill) {
			for (int i = 1; i <= reps; i++) {
				if (i == killPod) {
					pods.add(new PodState("api-rules-" + gen + "-" + i, "Replaced"));
				} else {
					pods.add(new PodState("api-rules-" + gen + "-" + i, "Running"));
				}
			}
			pods.add(new PodState("api-rules-aaaaa-" + (reps + 1), "Starting"));
			ready = reps - 1;
		} else if (rolling) {
			for (int i = 1; i <= reps; i++) {
				pods.add(new PodState("api-rules-" + gen + "-" + i, "Running"));
			}
		} else {
			for (int i = 1; i <= reps; i++) {
				pods.add(new PodState("api-rules-" + gen + "-" + i, "Running"));
			}
		}

		String verdict;
		if (kill) {
			verdict = ("Pod %d died — the Deployment controller noticed %d/%d ready and scheduled a replacement "
					+ "(pod “Starting”). Self-heal counter: %d. Desired state stays at %d replicas.")
					.formatted(killPod, ready, reps, respawned, reps);
		} else if (rolling) {
			verdict = ("Rolling update: generation %s is draining in one pod at a time while the Service keeps "
					+ "routing — no downtime, %d/%d ready.").formatted(gen, ready, reps);
		} else {
			verdict = "Steady state: %d/%d replicas running (pod generation %s). Desired state matches reality."
					.formatted(ready, reps, gen);
		}
		return new K8sResult(pods, reps, ready, respawned, rolling, verdict);
	}

	private final RespawnCounter respawns;

	public K8sService(RespawnCounter respawns) {
		this.respawns = respawns;
	}
}