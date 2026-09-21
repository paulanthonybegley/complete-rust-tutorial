package com.example.devops.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.devops.model.PipelineResult;
import com.example.devops.model.PipelineStage;

/**
 * CI/CD pipeline simulator. Eight stages mirror the build → deploy arc of the
 * course; a stage can be sabotaged so learners see the pipeline stop the
 * moment a job fails.
 */
@Service
public class PipelineService {

	private static final String NONE = "none";

	public PipelineResult run(String failAt) {
		String[] names = { "Checkout", "Compile & package", "Unit & integration tests", "Security scan",
				"Build image", "Push to registry", "Deploy to cluster", "Verify & smoke" };
		String[] notes = { "clone + resolve refs",
				"mvn -B package → api-rules-0.0.1-SNAPSHOT.jar",
				"19 tests guard the API contracts",
				"SCA + SAST — dependency ranges clean",
				"docker build multi-stage (builder + runtime)",
				"docker push registry/api-rules:sha-<commit>",
				"kubectl apply -f k8s/api-rules.yaml",
				"curl /health 200 · P95 latency under threshold" };
		String sabotage = failAt == null || failAt.isBlank() ? NONE : failAt.toLowerCase();

		List<PipelineStage> stages = new ArrayList<>();
		boolean broken = false;
		for (int i = 0; i < names.length; i++) {
			boolean failing = sabotage.equals(names[i].toLowerCase()) || sabotage.equals(String.valueOf(i))
					|| sabotage.equals(stageKey(i));
			if (broken) {
				stages.add(new PipelineStage(names[i], false, true, "never reached"));
			} else if (failing) {
				stages.add(new PipelineStage(names[i], false, false,
						"FAIL — " + failureNote(i)));
				broken = true;
			} else {
				stages.add(new PipelineStage(names[i], true, false, notes[i]));
			}
		}
		PipelineResult result = new PipelineResult(stages, !broken, verdict(!broken, sabotage));
		return result;
	}

	private String stageKey(int i) {
		return List.of("checkout", "compile", "tests", "scan", "image", "push", "deploy", "verify").get(i);
	}

	private String failureNote(int i) {
		return switch (i) {
			case 0 -> "no matching refs on the runner";
			case 1 -> "compilation error in ProductController.java:73";
			case 2 -> "order test red — expected 201, got 422";
			case 3 -> "known CVE in a transitive dependency range";
			case 4 -> "context too large — forgot .dockerignore";
			case 5 -> "registry auth rejected the push";
			case 6 -> "manifest invalid — readiness probe missing";
			case 7 -> "P95 latency 2100ms — pagination regression";
			default -> "something exploded";
		};
	}

	private String verdict(boolean green, String sabotage) {
		if (green) {
			return "Green run: every job passed and the artifact shipped. Nothing reached production that didn't "
					+ "earn its way here.";
		}
		return ("Red run: the pipeline stopped at the first failure (“%s”), so nothing deployable came out of this "
				+ "change. Catch it here, not at 3 a.m.")
				.formatted(sabotage.equals(NONE) ? "?" : sabotage);
	}
}