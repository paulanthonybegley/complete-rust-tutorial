# DevOps Zero → Hero — Education Log

How this course was built — the same OLD framework (`OLD.jpg` → `OLD.txt`)
used to build every course in this repository.

## Source analysis

- **Video**: "DevOps from Zero to Hero: Build and Deploy a Production API",
  JavaScript Mastery, <https://www.youtube.com/watch?v=H5FAxTBuNM8>, ~15:30.
- Because a 15-hour series is too long to be "one lesson", the video was
  chapter-mapped (via its own internal timestamps) and then decomposed into
  its repeated skills: culture, Git/GitHub, CI/CD, Docker, Kubernetes, IaC,
  building the API, Postgres, logging/middleware, JWT + RBAC, security
  middleware, Dockerizing, CRUD + testing. Those repeated skills became the 13
  features/lessons.

## Design decisions

1. **13 features → 13 lessons** — a one-lesson-per-chapter mapping would have
   split the build/API chapters apart; grouping by *skill* keeps the arc
   teachable. Each feature appears in the dashboard's ship meter.
2. **Ship the repo's own API** — the video ships its "acquisitions" API; this
   course ships `eighth-api-rules`, so learners touch real project code at
   every step (`deploy/`). This mirrors the video's pedagogical spine while
   staying 100% local (no cloud accounts).
3. **A single runnable artifact** — a Spring Boot 4 + Thymeleaf + HTMX lab
   (like `eu-independence`): pages for concepts and a separate sim per
   chapter-lab (pipeline, docker, k8s, iac, monitor, security, git), plus
   quiz + flashcards + ship + sources. All state in-memory; nothing to install
   but a JDK and Docker.
4. **Simulation, not decoration** — each lab models a *mechanism*:
   - pipeline: stage list, failure cuts the tail, verdict GREEN/RED;
   - docker: cache keys per layer (change `src`/`pom`/`base`, or `--no-cache`);
   - k8s: desired replicas vs observed pods, kill → Replaced → Starting;
   - iac: resources + `+ create/x/-destroy` diff, mode plan/apply/destroy;
   - monitor: availability + alert/paging state machine (auto-heal);
   - security: bot-likeness score + key gate → ALLOWED/BLOCKED;
   - git: feature branch divergence from `main` with tags.
5. **Exploration-first pages** — the htmx pattern that worked in `eu-independence`
   (drawer partials via `hx-get` + `hx-swap="outerHTML"`, `class="drawer open"`,
   `scrollIntoView`) was reused for concept detail.

## Architecture notes

- Package `com.example.devops`, artifact `devops-zero-hero`, Spring Boot 4.1.1,
  Java 17 property, port 8080 (served at 8092 via the Makefile).
- Records + in-memory services (ConceptService 13 cards, QuizService 12,
  FlashcardService 17, plus the sim services); controllers in `web/`;
  Thymeleaf templates in `templates/` with one shared `_fragments.html`
  (fragments must never render as a full page — the `eu-independence` gotcha).
- htmx is vendored (2.0.7) with camelCase `htmx:` events; no `hx-on:htmx:*`
  kebab wiring (the `eu-independence` lesson).
- Boolean stategs in sim forms use a hidden `value="false"` input plus a
  checkbox `value="true"` so unchecked binds correctly.

## The ship-kit

`deploy/` is a full, runnable version of the video's pipeline applied to
`eighth-api-rules`: a multi-stage Dockerfile with HEALTHCHECK and a non-root
user, a GitHub Actions workflow with a test gate, a Kubernetes deployment with
probes + rolling updates, and a Terraform main.tf (VPC → RDS → ECS). Together
they let a learner do the video's "ship it" arc purely locally, then point at
every block it replicates in a real cloud.

## Testing approach

42 MockMvc tests treat rendered HTML as the contract: every page loads, every
drawer/partial renders, every sim has a predictable verdict for both the happy
path and the sabotage path, quiz questions grade both right and wrong answers,
and flashcard grids filter. Run: `make test` (repo root) or
`mvn -B -f devops-zero-hero/app/pom.xml test`.

## What was verified

- Full test suite green (42/42).
- Manual/browser verification of htmx interactions: concept drawer opens and
  scrolls, pipeline sabotage, kill-a-pod reconciliation, quiz grading,
  flashcard flips, category filters.
- Docker: the course app builds (`make docker-run-devops`) and serves on
  :8092 from the container; the api-rules ship-kit image builds.

## Known limitations

- The video uses Neon/Drizzle + Arcjet; this course keeps everything local and
  in-memory, so lesson 8/11 sims teach the *idea* (secrets handling, bot
  defense) rather than the vendor SaaS.
- The workflow's deploy job is intentionally a stub (echo-only) to keep the
  repo credential-free.
- Terraform `main.tf` requires a real AWS account to apply; `plan`-shaped
  diffs are also demonstrated in `/iac`.

## Connection to the rest of the repo

Built in the same course system as `eu-independence` (Thymeleaf + htmx lab)
and `eighth-api-rules` (the API it ships). The Makefile hosts it as
`make start-devops` (port 8092) alongside the suite, and `make test` runs all
course suites so the whole repo stays green together.