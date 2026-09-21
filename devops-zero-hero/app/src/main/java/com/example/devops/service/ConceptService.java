package com.example.devops.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.devops.model.Category;
import com.example.devops.model.Concept;
import com.example.devops.model.Figure;
import com.example.devops.model.VideoStatus;

/**
 * Every feature raised in "DevOps from Zero to Hero: Build and Deploy a
 * Production API". Each concept maps back to what the video says and to the
 * key commands/artifacts that make it real.
 */
@Service
public class ConceptService {

	private final List<Concept> concepts = List.of(
			c("culture", Category.CULTURE, VideoStatus.EXPLAINED, "What DevOps is",
					"a culture, not a tool",
					"DevOps breaks the Dev-vs-Ops tug of war: developers who want to ship fast and ops teams who "
							+ "protect stability threw code over the wall until nothing shipped. DevOps is the culture "
							+ "shift that unites them with collaboration, automation and shared feedback — simpler than it "
							+ "sounds.",
					List.of(
							"Software used to work like: write code, run it on your laptop, deploy it to a server, done.",
							"Vibe shipping works until traffic spikes, hackers attack or the app dies at 3 a.m.",
							"Companies split into producers (devs) and guardians of stability (ops) — and got a tug of war.",
							"DevOps is not a tool or a role — it is a culture, a set of practices and an automation toolbox.",
							"Restaurant metaphor: devs are the chefs, ops is the front of house; without a system the "
									+ "kitchen is chaos."),
					List.of(fig("Dev vs Ops  →  DevOps", "the shift",
							"From 'code over the wall' to one team with one goal."),
							fig("collaboration · automation · feedback", "the three pillars",
									"Speed and stability stop being enemies."),
							fig("‘DevOps sounds scary as hell’", "the myth",
									"Half of tutorials gatekeep it — it is simpler than it looks."))),
			c("loop", Category.CULTURE, VideoStatus.EXPLAINED, "The infinity loop",
					"how an idea becomes software",
					"Every idea that becomes software flows around the loop Plan → Code → Build → Test → Release → "
							+ "Deploy → Operate → Monitor, then feeds back. Dev and ops are connected the entire time — "
							+ "the loop is the process, not a conference graphic.",
					List.of(
							"Plan: what to build, when to ship, who owns it, how success is measured — traceable, not sticky notes.",
							"Code: clean, modular, testable; Git with reviews, branch rules and automated checks.",
							"Build: raw text becomes an artifact — compile, install dependencies, bundle, package, scan.",
							"Test: catch problems early when they are cheap — unit, integration, e2e, security scans.",
							"Release → Deploy → Operate → Monitor: ship controlled, run it, watch it — then loop back "
									+ "with feedback."),
					List.of(fig("Plan·Code·Build·Test·Release·Deploy·Operate·Monitor", "the 8 stages",
							"One continuous loop, not a timeline."),
							fig("Jira · Linear · GitHub Projects · Notion", "planning",
									"The tool matters less than the discipline."))),
			c("git", Category.VERSION_CONTROL, VideoStatus.PRACTISED, "Version control (Git & GitHub)",
					"history is a time machine",
					"Git is the version-control pillar: every change is a tracked commit, branches let many people "
							+ "work in parallel, pull requests and branch rules keep the mainline clean — and revert is "
							+ "your escape hatch.",
					List.of(
							"A commit is a snapshot; git keeps a distributed history of them.",
							"Branches let you build features in parallel without colliding on main.",
							"Code reviews and branch protection keep the mainline readable and green.",
							"Revert to a previous state instead of panicking — 'history is a time machine'."),
					List.of(fig("git commit -m 'feat: …'", "snapshot", "One logical change per commit."),
							fig("git checkout -b feature/login", "branch", "Parallel work without colliding."),
							fig("git revert <sha>", "undo", "Safe rewind, even for released lines."),
							fig("PR + branch protection", "review", "Mainline stays reviewable and green."))),
			c("pipeline", Category.PIPELINES, VideoStatus.PRACTISED, "CI/CD pipelines",
					"the assembly line",
					"Continuous Integration merges and tests everyone's code constantly; Continuous Deployment ships "
							+ "it automatically. A pipeline is the assembly line where each job must pass before the next "
							+ "runs — build, test, scan and deploy as code.",
					List.of(
							"CI: every push integrates the shared repo and runs the tests — integration problems are "
									+ "found daily, not weekly.",
							"CD: a green build rolls to staging and then production without a human babysitting.",
							"Pipelines are code — repeatable, consistent and auditable, like any other source file.",
							"The video's first pipeline: every commit builds and pushes a Docker image."),
					List.of(fig("push → test → build → scan → deploy", "the pipeline",
							"Stages that gate each other, top to bottom."),
							fig("GitHub Actions · Jenkins · GitLab CI", "the runners",
									"Workflow defined as YAML inside the repo."))),
			c("build", Category.PIPELINES, VideoStatus.PRACTISED, "Build: source → artifact",
					"bake the dough into bread",
					"Source code is raw dough — you can't serve it. The build bakes it into an artifact: compiling, "
							+ "installing dependencies, bundling, packaging and scanning — automated so the same recipe "
							+ "runs identically on every machine.",
					List.of(
							"Source files usually can't execute directly in production — they need compiling, deps and "
									+ "a package.",
							"Artifacts: a compiled binary, a .jar, a Docker image.",
							"Automation makes the build consistent — same result anywhere, every commit.",
							"Building an image is baking dough into bread; a ready-to-run artifact."),
					List.of(fig("mvn -B package", ".jar artifact", "Our API: a self-executing Spring Boot jar."),
							fig("docker build -t api:1.0.0 .", "image artifact",
									"Bake the jar + runtime into a portable image."),
							fig("lint + SAST + SCA", "scanning", "Catch mistakes and known vulnerabilities at build time."))),
			c("docker", Category.CONTAINERS, VideoStatus.PRACTISED, "Docker",
					"kill 'it works on my machine'",
					"Docker packages your app with everything it needs into an image and runs it in a container — a "
							+ "standard, portable unit that behaves the same on a laptop, a CI runner and a production "
							+ "node.",
					List.of(
							"Containers include code, runtime, dependencies and settings — run anywhere Docker runs.",
							"Images are templates; containers are the running instances.",
							"A Dockerfile declares how to build the image — reproducible, layered.",
							"Ports, volumes and env vars connect containers to the outside world."),
					List.of(fig("docker build -t acme/api:1.0 .", "build", "Create an image from a Dockerfile."),
							fig("docker run -p 8080:8080 acme/api:1.0", "run",
									"Start a container and expose port 8080."),
							fig("docker compose up", "stack", "Dev environment for several services in one command."))),
			c("dockerize", Category.CONTAINERS, VideoStatus.PRACTISED, "Dockerization",
					"multi-stage builds",
					"The video containerizes the production API: a Dockerfile with a multi-stage build uses a fat "
							+ "builder image for Maven and ships only a lean runtime image — plus Compose to wire the "
							+ "API to its database and config.",
					List.of(
							"A Dockerfile is the build recipe; .dockerignore keeps junk out of the build context.",
							"Multi-stage: build with Maven, run on a slim JRE — no toolchain in production.",
							"Compose wires the API, its Postgres and config together in one file.",
							"Health checks tell orchestrators when the container is actually ready."),
					List.of(fig("FROM maven:… AS builder / FROM eclipse-temurin:…", "multi-stage",
							"One image to build, one lean image to run."),
							fig("docker compose up -d", "the stack", "App + database in one command."),
							fig("EXPOSE 8080", "port contract", "The port the container listens on."),
							fig("docker build --no-cache", "cold build", "Bypass the layer cache for a clean rebuild."))),
			c("kubernetes", Category.ORCHESTRATION, VideoStatus.PRACTISED, "Kubernetes",
					"the orchestrator",
					"Kubernetes runs containers at production scale: pods scheduled across machines, self-healing "
							+ "restarts, on-demand scaling and load balancing — 'kill a pod and it respawns'.",
					List.of(
							"A pod is the smallest deployable unit — containers that share a network and mount.",
							"Deployments declare the desired state; the controller reconciles reality towards it.",
							"Self-healing: a dead pod is replaced automatically — that is the demo.",
							"Rolling updates and scaling change replicas without downtime."),
					List.of(fig("kubectl apply -f api-rules.yaml", "declare", "Desired state as YAML."),
							fig("kubectl get pods", "observe", "Reality vs the desired state."),
							fig("kubectl scale deploy api-rules --replicas=3", "scale",
									"Handle the traffic spike on demand."),
							fig("Deployment · Service · HPA", "the objects", "Desires, exposure and autoscaling."))),
			c("iac", Category.IAC, VideoStatus.EXPLAINED, "Infrastructure as Code",
					"environments as files",
					"IaC manages infrastructure with machine-readable config instead of consoles and click-ops: "
							+ "Terraform describes a VPC, database and cluster in code, and plan/apply diffs reality "
							+ "against the desired state.",
					List.of(
							"Terraform describes resources — VPC, DB, cluster, registry — in HCL.",
							"Plan → Apply: the provider diffs real infrastructure against the declared state.",
							"Environments become reproducible code — destroy them and recreate them on demand.",
							"Infrastructure gets the same review, versioning and discipline as application code."),
					List.of(fig("terraform init / plan / apply", "the loop",
							"Declare, plan, apply — and drift-check forever."),
							fig("resource \"aws_eks_cluster\" \"app\" {}", "HCL", "Desired state as data."),
							fig("terraform destroy", "tear down", "No stale cloud bills; rebuild anytime."))),
			c("monitor", Category.OBSERVABILITY, VideoStatus.PRACTISED, "Monitoring & logging",
					"you can't operate what you can't see",
					"Health endpoints, structured logs and metrics tell you a service is alive and how it feels — "
							+ "and alerting tells someone when it isn't. That '3 a.m. page' is the price of operating "
							+ "something people rely on.",
					List.of(
							"The API ships a health endpoint; orchestrators probe it for readiness and liveness.",
							"Structured logging turns logs into queryable events, not walls of text.",
							"Metrics — latency, error rate, uptime — feed dashboards and thresholds.",
							"The famous 3 a.m. page: a threshold crossing turns a metric into an alert."),
					List.of(fig("GET /health", "liveness", "Is it alive? Probe it, several times a minute."),
							fig("Prometheus · Grafana", "the view", "Scrape metrics, draw dashboards."),
							fig("P95 latency 900ms → page", "the alert", "A threshold becomes a page."),
							fig("{level, service, trace, msg}", "structured log", "Logs as JSON events you can query."))),
			c("security", Category.SECURITY, VideoStatus.PRACTISED, "Security",
					"protection from day one",
					"Real APIs get attacked — bots, spam, scraping and abuse. The video bakes protection in from the "
							+ "start: input validation, JWT auth with role-based access, and a bot-mitigation service "
							+ "guarding every endpoint in real time.",
					List.of(
							"Security isn't tackled later — it is there from the first build.",
							"Validation rejects malformed input before it reaches business logic.",
							"JWT authentication + RBAC gate admin vs user actions.",
							"Bot/spam/abuse protection sits in front of the API and blocks in real time.",
							"Secrets come from the environment, never from the repo."),
					List.of(fig("Arcjet / bot-mitigation", "the guard", "Block bots, spam and abuse in real time."),
							fig("JWT + RBAC", "identity", "Prove who you are; allow what your role may do."),
							fig("input validation", "first line", "Reject junk before logic runs."),
							fig("SECRETS via env", "hygiene", "Never commit a key to git."))),
			c("testing", Category.PIPELINES, VideoStatus.PRACTISED, "Testing in the pipeline",
					"fail fast, fail early",
					"Tests are the tripwire keeping bad code out of production: unit, integration, e2e and security "
							+ "scans all run automatically in CI, when problems are still cheap to fix.",
					List.of(
							"Untested code straight to production is a gamble that pays until it doesn't.",
							"Unit tests check one unit; integration tests check the real wiring.",
							"E2E workflows walk the whole journey; scans hunt secrets and vulnerable dependencies.",
							"A red test stops the pipeline before users ever see the change."),
					List.of(fig("mvn test", "our suite", "19 tests guard the api-rules contracts."),
							fig("unit → integration → e2e → scan", "the ladder",
									"From cheap to expensive, all automated."),
							fig("JUnit 5 · Pact · Playwright · SAST", "the toolbox",
									"Pick by cost, coverage and risk."))),
			c("ship", Category.SHIP, VideoStatus.SHIPPED, "Ship the production API",
					"from zero to hero",
					"Everything lands in one arc: a production API is validated, authenticated, protected, "
							+ "containerized, pushed through a pipeline, deployed on an orchestrator and watched by "
							+ "metrics. That is the whole video — and the kit in deploy/ repeats it for our api-rules "
							+ "API.",
					List.of(
							"Production stack: Postgres, JWT auth, RBAC, validation, structured logging, bot defense.",
							"Containerize → CI/CD → registry → orchestrator → health checks → monitoring.",
							"The pipeline builds and tests on every commit; deploy is one green button (or none).",
							"Wrap-up: keep the feedback loop visible — plan, ship, measure, repeat."),
					List.of(fig("build → push → deploy → verify", "the arc",
							"Every concept from the opening lands in the closing deploy."),
							fig("Neon · Arcjet · Docker · K8s · IaC", "the stack",
									"Real tools that real companies use every day."),
							fig("deploy/ kit", "yours now", "The Dockerfile, workflow, manifests and Terraform for "
									+ "api-rules live in deploy/."))));

	public List<Concept> all() {
		return concepts;
	}

	public List<Concept> byCategory(Category category) {
		return concepts.stream().filter(c -> c.category() == category).toList();
	}

	public Concept byId(String id) {
		return concepts.stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
	}

	private static Concept c(String id, Category category, VideoStatus status, String title, String subtitle,
			String summary, List<String> inVideo, List<Figure> figures) {
		return new Concept(id, category, status, title, subtitle, summary, inVideo, figures);
	}

	private static Figure fig(String value, String label, String note) {
		return new Figure(value, label, note);
	}
}