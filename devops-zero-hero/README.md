# DevOps Zero → Hero — Build & Deploy a Production API

An educational course built from the YouTube video **"DevOps from Zero to Hero:
Build and Deploy a Production API"** (JavaScript Mastery,
<https://www.youtube.com/watch?v=H5FAxTBuNM8>, ~15:30 hours), which takes a
beginner from "what is DevOps" to shipping a production API with Docker,
Kubernetes, Infrastructure as Code and a full CI/CD pipeline.

The API this course ships is the repo's own [`../eighth-api-rules`](../eighth-api-rules)
— a Spring Boot 4 REST API teaching the 8 API Laws — so the course teaches the
DevOps arc **on real code that already lives in this repository** (the video's
"acquisitions" API plays the same role there).

> Tagline from the source framework (`OLD.jpg` → `OLD.txt`):
> **"A lesson plan is a system, not a prompt."**

## The features raised in the video

Every feature raised in the video is a lesson in this course and a section of
the app:

| # | Category | Feature the video raises |
|---|----------|--------------------------|
| 1 | Culture | DevOps is "a culture, a mindset, a way of working — not a tool", no more "works on my machine" |
| 2 | Version control | Git, GitHub, feature branches, pull requests, tags; main must always be deployable |
| 3 | CI/CD | Continuous Integration tests ("tests never red") + Continuous Delivery to prod |
| 4 | Containers | Docker: images vs containers, layers & caching, ports, `docker build`, multi-stage builds |
| 5 | Orchestration | Kubernetes: desired state, deployments, replicas, rolling updates, self-healing |
| 6 | IaC | Infrastructure as Code with Terraform; environments (dev/stag/prod); records, not snowflakes |
| 7 | Build & ship an API | A real API walking GitHub → Docker → Kubernetes → Terraform |
| 8 | Database | Postgres (the video uses Neon + Drizzle); migrations, connection strings, secrets |
| 9 | Observability | Structured logging, middleware/loggers, health & readiness, paging discipline |
| 10 | Auth | JWT, roles, RBAC, protected routes, security headers/CORS |
| 11 | Security | Bot defense and threat intelligence (Arcjet-style middleware), rate limits, key checks |
| 12 | Tests & gates | Unit + integration tests, test-driven gates in the pipeline, "tests never red" |
| 13 | The hero arc | From first successful PR to deployed production API |

> The course API ships via the `deploy/` ship-kit — same chapters, real files
> (Dockerfile, GitHub Actions workflow, Kubernetes manifest, Terraform).

## Repository layout

```
devops-zero-hero/
├── OLD.txt          # OCR text extracted from OLD.jpg (the lesson-planning framework)
├── README.md        # this overview
├── lesson_plans.md  # 13 lesson plans built on the OLD framework
├── exercises.md     # per-lesson exercises + integration project rubric
├── solutions.md     # sample answers (verified against the running app)
├── education.md     # education log: how this course was built
├── linkedin-post.md # shareable social post
├── Dockerfile       # containerize the course app itself (multi-stage)
├── compose.yaml     # run the course app in Docker on :8092
├── deploy/          # the ship-kit: ship eighth-api-rules like the video ships its API
│   ├── Dockerfile.api-rules
│   ├── .github/workflows/course.yml
│   ├── k8s/api-rules.yaml
│   └── terraform/main.tf
└── app/             # Spring Boot 4 + Thymeleaf + HTMX interactive lab
    └── src/main/
        ├── java/com/example/devops/
        │   ├── model/        # Concept, PipelineStage, PodState, QuizQuestion, ...
        │   ├── service/       # PipelineService, DockerService, K8sService, ...
        │   └── web/           # ConceptController, PipelineController, ...
        └── resources/
            ├── templates/     # Thymeleaf pages + fragments
            └── static/        # style.css + vendored htmx
```

## Run the app

Requirements: JDK 17+ (tested on 24), Maven 3.9+, optionally Docker.

```bash
make start-devops              # from repo root → http://localhost:8092
# or in devops-zero-hero/app:
mvn spring-boot:run            # http://localhost:8080 (or --server.port=8092)
mvn test                       # 42 tests verify every page + sim renders
make docker-run-devops         # build + run the course itself as a container :8092
```

Open <http://localhost:8092>. No database is needed — every sim is in-memory so
the focus stays on the teaching.

### What's inside the app

- **Dashboard** (`/`) — the hero arc as a 92%-complete "Ship meter".
- **Concepts** (`/concepts`) — all 13 features as cards, filterable by category
  (Culture → Ship), each opening an htmx drawer with the video's own bullets.
- **Container lab** (`/docker`) — the whole ship-an-image arc as one page:
  *Pipeline* (8-stage CI/CD; sabotage any stage and watch the run turn RED),
  *Docker build* (layer-cache animator: change `src`/`pom`/`base` or flip
  `--no-cache`, see exactly which layers CACHED vs REBUILT against the real
  Java 17 multi-stage Dockerfile), and *Kubernetes* (steady state 1–5
  replicas; kill a pod and watch the controller self-heal). The standalone
  `/pipeline` and `/k8s` views remain part of the flow.
- **IaC lab** (`/iac`) — `plan`/`apply`/`destroy` against a changed world
  (nothing, Postgres, scale-up) with a terraform-style diff table.
- **Monitor** (`/monitor`) — availability gauges, a paging discipline sim
  (auto-heal vs the 3 a.m. page).
- **Security** (`/security`) — the video's bot-defense posture: pick a client,
  a bot-likeness score and an API-key decision; watch ALLOWED ⇄ BLOCKED flip.
- **Git graph** (`/git`) — feature branches diverging from `main`, tags, the
  PR that ships.
- **Quiz** (`/quiz`) — 12 self-grading questions with instant htmx feedback.
- **Flashcards** (`/flashcards`) — 17 flip cards across the whole arc.
- **Ship** (`/ship`) — the 6-step shipping loop mapped to the `deploy/` kit.
- **Sources** (`/sources`) — the full chapter map with timestamps.

## Learning order

1. Watch the video (<https://www.youtube.com/watch?v=H5FAxTBuNM8>).
2. Read `lesson_plans.md` — 13 lessons, each built with the 9 design inputs
   from `OLD.txt`.
3. Do the matching exercises in `exercises.md` while driving the app.
4. Check your work against `solutions.md`.
5. Finish the integration project: **ship the real `eighth-api-rules`**
   through `deploy/` and explain each step out loud.
6. Prove it: run `make test` from the repo root — every course stays green.