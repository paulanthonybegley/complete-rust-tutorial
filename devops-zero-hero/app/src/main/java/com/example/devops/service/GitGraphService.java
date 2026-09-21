package com.example.devops.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.devops.model.CommitNode;

/**
 * A tiny, hand-drawn git graph for the version-control explorer. Columns are
 * branches; the '*' marks a branch tip.
 */
@Service
public class GitGraphService {

	private static final List<CommitNode> ALL = List.of(
			n("HEAD → main", "9f3a2c1", "chore: tag v1.0.0", "you", "just now", 0),
			n("", "8b71f04", "feat: /health liveness check", "you", "2m ago", 0),
			n("", "5c09dda", "fix: pin base image digest", "reviewer", "1h ago", 0),
			n("", "8f2ae10", "ci: add security scan stage", "ci-bot", "3h ago", 0),
			n("*", "32bc77a", "feat: dockerize api-rules (multi-stage)", "you", "5h ago", 0),
			n("", "fd11e90", "feat: JWT auth + role-based access", "feature/login", "yesterday", 1),
			n("", "c4d90a2", "feat: Neon Postgres wiring", "feature/login", "yesterday", 1),
			n("", "07c3f5b", "refactor: shared error shape", "you", "2d ago", 0),
			n("", "4e8d210", "feat: products and orders CRUD", "you", "3d ago", 0),
			n("", "0036aa0", "initial commit", "you", "5d ago", 0));

	public List<CommitNode> forBranch(String branch) {
		String b = branch == null || branch.isBlank() ? "main" : branch;
		if (b.equals("feature/login")) {
			List<CommitNode> out = new ArrayList<>();
			boolean seenDivergence = false;
			for (CommitNode node : ALL) {
				if (node.column() == 1) {
					out.add(node);
					seenDivergence = true;
				} else if (seenDivergence) {
					out.add(node);
				}
			}
			return out;
		}
		return ALL.stream().filter(n2 -> n2.column() == 0).toList();
	}

	private static CommitNode n(String mark, String sha, String message, String author, String when, int column) {
		return new CommitNode(mark, sha, message, author, when, column);
	}
}