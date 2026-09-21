package com.example.devops.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.devops.model.Category;
import com.example.devops.model.Flashcard;

/**
 * Flashcard decks — term on the front, definition on the back, filterable by
 * theme.
 */
@Service
public class FlashcardService {

	private final List<Flashcard> cards = List.of(
			f(Category.CULTURE, "DevOps", "A culture of collaboration, practices and automation that unites "
					+ "development and operations to deliver faster and safer."),
			f(Category.CULTURE, "Infinity loop", "Plan → Code → Build → Test → Release → Deploy → Operate → "
					+ "Monitor: how an idea becomes software in users' hands."),
			f(Category.VERSION_CONTROL, "Commit", "A tracked snapshot of changes in git's distributed history."),
			f(Category.VERSION_CONTROL, "Branch", "A parallel line of work that can be merged back to main after "
					+ "review."),
			f(Category.PIPELINES, "CI", "Continuous Integration: merging and testing every change in the shared "
					+ "repo, constantly."),
			f(Category.PIPELINES, "CD", "Continuous Delivery/Deployment: a green artifact ships to environments "
					+ "automatically."),
			f(Category.PIPELINES, "Artifact", "A ready-to-run package produced by the build — a jar, a binary, a "
					+ "Docker image."),
			f(Category.CONTAINERS, "Image", "A read-only template containing code, runtime and dependencies."),
			f(Category.CONTAINERS, "Container", "A running instance of an image—standard and portable everywhere."),
			f(Category.CONTAINERS, "Multi-stage build", "A Dockerfile that builds with a fat toolchain image and "
					+ "copies only the artifact into a lean runtime image."),
			f(Category.ORCHESTRATION, "Pod", "The smallest deployable unit in Kubernetes: containers sharing a "
					+ "network and mount."),
			f(Category.ORCHESTRATION, "Reconcile", "The controller loop that nudges reality towards the desired "
					+ "state declared in the manifest."),
			f(Category.IAC, "IaC", "Infrastructure as Code: managing servers and cloud resources as versioned, "
					+ "reviewable config."),
			f(Category.OBSERVABILITY, "Health endpoint", "An endpoint (e.g. /health) probes use to decide whether "
					+ "a pod is ready or alive."),
			f(Category.SECURITY, "RBAC", "Role-based access control: what an authenticated user's role may do."),
			f(Category.SECURITY, "Rate limiting", "Capping requests per client — a first defence against bots and "
					+ "abuse."),
			f(Category.SHIP, "Shipping loop", "containerize → pipeline → registry → orchestrator → health checks "
					+ "→ monitoring."));

	public List<Flashcard> all() {
		return cards;
	}

	public List<Flashcard> byCategory(Category category) {
		return cards.stream().filter(c -> c.category() == category).toList();
	}

	private static Flashcard f(Category category, String front, String back) {
		return new Flashcard(category, front, back);
	}
}