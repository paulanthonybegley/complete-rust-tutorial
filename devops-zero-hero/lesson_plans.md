# DevOps Zero → Hero — Lesson Plans

13 lessons, one per feature raised in the video. Each lesson is built from the
nine design inputs recorded in `OLD.txt`: learning goal, sequence, assessment,
learner profile, prior knowledge, activities, outputs, accessibility and
teacher decisions.

> "A lesson plan is a system, not a prompt." Every plan below names the goal,
> the sequence, the check and the output — and every activity is runnable using
> the app in `app/` and the ship-kit in `deploy/`. **Nothing here requires a
> cloud account.**

## Learner profile

Adult developer with 6–12 months of experience: can run a Spring Boot API,
knows what Git is, has never run a container or a pipeline. Motivated by
building something real; intimidated by the density of the DevOps term list.
Prior knowledge check: repo layout, `main`, running `mvn test`.

---

## Lesson 1 — DevOps is a culture, not a tool

**Learning goal.** Say what DevOps is in one sentence and distinguish culture
from tooling. (Video 00:06:04–00:20:41.)

**Sequence.**
1. Watch the intro. List every "practical tool" you saw.
2. Watch the "what is DevOps" segment; note the phrase "something you do, not
   something you buy".
3. Open `/concepts` → "What DevOps is". Read the bullets that quote the video.

**Assessment.** In one minute, without the video: "DevOps is …" — the check: your
answer names values (feedback, shared ownership) not products.

**Activity.** In `/concepts`, flip to "Works on my machine" and re-type it as a
problem statement: "deployable from only one place".

**Output.** One paragraph: why "we use GitHub Actions" is *not* a DevOps story.

**Accessibility.** Visual: film the whole 15-hour series? No — the 13 lessons
use clips only. Every new term is glossed in the drawer. Keep sentences short.

**Teacher decisions.** If a learner already runs containers, they lead Lesson 4's
labs; everyone still does Lessons 1–3 first because the arc is the curriculum.

---

## Lesson 2 — Version control: your deployable main

**Learning goal.** Explain why `main` must always be green, run a feature branch
and describe a pull request. (00:25:42–00:50:33.)

**Sequence.** Git basics → branching → the review loop → tags.
1. Open `/git`. Identify `main`, the `feature/login` branch and the `v1.0.0` tag.
2. Switch the graph to `feature/login`; note how the branch diverges.

**Assessment.** Draw the graph from `/git` on paper with three arrows: where does
the feature come from, where does it land, what does the tag mark?

**Output.** In `eighth-api-rules`, create a real feature branch, change one
`.http` comment, commit, and explain what would make a good PR.

---

## Lesson 3 — CI/CD: test, then deliver

**Learning goal.** Separate *integration* from *delivery* and explain why a red
test must stop the train. (00:50:33, 00:55:06, 04:38:50.)

**Sequence.** Watch the first pipeline build → the pipeline diagram → testing.

**Activity.** `/pipeline`: run a clean build (stages light up). Now sabotage the
`tests` stage — 8 steps in, the pipeline prints RED and the remaining stages call
*SKIPPED*. Screenshot it. That screenshot is your study card for "tests never red".

**Assessment.** "CI is dead code without ___" — the graders accept red/green
gates *and* a reason (main stays deployable).

**Output.** The RED screenshot + one sentence: what the deploy stage would have
done if the tests had passed.

---

## Lesson 4 — Containers: images, layers, caching

**Learning goal.** Contrast image vs container, read a Dockerfile's layers, and
predict which layers reuse a cache. (01:03:21–01:55:39.)

**Sequence.** Watch the Docker chapter → read `deploy/Dockerfile.api-rules`
(top to bottom) → run the lab.

**Activity.** `/docker`: build with a `src` change (top layers REBUILT, base
CACHED) then change `base` (everything REBUILT — "whole image rebuilds").
Flip `--no-cache` and watch caching disappear. Confirm the real files:
`docker build -f devops-zero-hero/deploy/Dockerfile.api-rules -t api-rules .`

**Assessment.** "Change one source line → how many layers rebuild and why?"

**Output.** Label each line of `Dockerfile.api-rules` with the layer it creates.
Three lines max per layer.

---

## Lesson 5 — Orchestration: the controller loop

**Learning goal.** Explain desired state and prove self-healing. (01:55:39–02:26:34.)

**Sequence.** Watch Kubernetes → the deployment concept → the rolling update.

**Activity.** `/k8s`: set 3 replicas (3/3 Running). Kill pod #2. Watch it become
*Replaced*, a *Starting* pod appear, then 3/3 again. The app named "Self-heal"
that for you — that's the reconciliation loop.

**Assessment.** "A pod dies at 3 a.m. What in Kubernetes notices, and on what
schedule?" (Liveness/readiness probes; see the real ones in
`deploy/k8s/api-rules.yaml`.)

**Output.** Annotate `deploy/k8s/api-rules.yaml`: replicas, rollingUpdate,
readinessProbe, livenessProbe — one line each.

---

## Lesson 6 — Infrastructure as Code

**Learning goal.** Reproduce infrastructure deterministically and show a plan
before you change anything. (02:26:34–02:29:37.)

**Sequence.** Watch the IaC chapter → plan → apply → destroy.

**Activity.** `/iac`: `plan` with no change = no-op. Add the Postgres resource and
`plan` again — a diff table grows (`aws_rds_cluster` with `+ create`).
`terraform plan` in `deploy/terraform/` should look like that table, not a
surprise.

**Assessment.** "Why does IaC beat a hand-built console cluster? Name two
reasons." (Reproducible + reviewable.)

**Output.** Two pull-request comments a reviewer would write on `main.tf`.

---

## Lesson 7 — Build & ship the API end-to-end

**Learning goal.** Chain Git → CI → Docker → Kubernetes → Terraform and name
what each hop is for. (02:29:37–02:46:09.) This is the spine of the course: the
video ships *its* API; this course ships `eighth-api-rules`.

**Sequence.** Watch the chapter → open `/ship` (6-step shipping loop) → read
`deploy/README.md`.

**Activity.** Walk the loop out loud: local build → green tests →
`Dockerfile.api-rules` → GitHub Actions pushes the image → the k8s deployment
reconciles → Terraform reproduced the infra.

**Assessment.** Given a broken dependency change, at which hop does the loop
stop? (The test gate — hop 2.)

**Output.** The `/ship` page describes the arc; give each step a one-line
"what could fail here" note in your notebook.

---

## Lesson 8 — Databases in production

**Learning goal.** Connect an API to a database without leaking how. (02:46:09–02:54:18.)

**Sequence.** Watch the Neon/Postgres + Drizzle segment → migrations → secrets.

**Activity.** In `deploy/terraform/main.tf`, find the RDS block and the comment
next to `password`. Re-write the password line the way a secure pipeline would
(secrets manager; nothing in source).

**Assessment.** "Where should a connection string live, and why not in
`.env` committed to the repo?"

**Output.** One sentence migration policy: "schema changes ship like …" (answer:
as reviewed, versioned, gated code — not as hand-run SQL in prod).

---

## Lesson 9 — Observability: logs, health, paging

**Learning goal.** Distinguish health from performance and decide when to page.
(02:54:18–03:05:15.)

**Sequence.** Watch the logger/middleware segment → the monitoring sim.

**Activity.** `/monitor`: steady state reads quiet, 99.95% availability. Drop the
cluster to unhealthy *without* auto-heal: PAGED — "the 3 a.m. page". Repeat with
auto-heal on: the box heals itself, nobody pages.

**Assessment.** "99.9% vs 99.95% — what changed about the budget for being down?"
(27 min → 4.4 h/yr.)

**Output.** A 3-line runbook: what you'd check first when the page fires
(health endpoint → recent deploy → logs).

---

## Lesson 10 — Auth: JWT and roles

**Learning goal.** Separate *who* from *what they may do*. (03:05:15–03:35:56.)

**Sequence.** Watch the auth chapter → JWT basics → RBAC.

**Activity.** `/flashcards`, security deck: flip "JWT", "RBAC", "session vs
JWT". For each: term on front, plain-English claim on back. Then in
`eighth-api-rules`, find where roles would gate a route (the API has an
`API-Version` negotiation layer already — that is a sibling of authz).

**Assessment.** "A JWT proves who you are but not what you may do — add one
sentence."

**Output.** A mental model diagram: request → JWT verified → role checked →
resource.

---

## Lesson 11 — Security middleware & bot defense

**Learning goal.** Show how threat-intelligence middleware blocks traffic
*before* your handlers run. (03:35:56, 03:47:53–04:03:11.)

**Sequence.** Watch the security middleware (Arcjet) chapter → the lab.

**Activity.** `/security`: start with a browser + low bot-likeness + valid key →
ALLOWED. Then: same human, no key → BLOCKED. Then a spammer with a key →
BLOCKED by risk score. Read the verdict line each time — it explains the rule.

**Assessment.** "Why does bot defense belong in middleware, not in each
controller?" (One place, before your code.)

**Output.** Posture table for three clients: browser/human, scraper, spammer —
allowed? which rule stopped them?

---

## Lesson 12 — Dockerize your API (the course's own image)

**Learning goal.** Apply everything: the course's own app becomes the artifact.
(04:03:11–04:21:49, 04:21:49–04:38:50 detail.)

**Sequence.** Watch the Dockerization + CRUD/testing segments → build the course
app.

**Activity.** `make docker-run-devops` — build `devops-zero-hero/Dockerfile`
from the multi-stage template, run it on :8092, and browse the course *from the
container*. Same app, packaged. Then flip through `/docker` one last time: the
layers you see there are the layers this build really used.

**Assessment.** "What does the runtime stage of the course Dockerfile contain
that the build stage does not?" (JRE only, no Maven.)

**Output.** `docker images` + one line: why the image is ~90% smaller than the
build stage.

---

## Lesson 13 — The hero arc

**Learning goal.** Present the whole delivery chain confidently. (00:00 intro
recap + all chapters; 04:48:35 "next steps".)

**Sequence.** Revisit the ship meter (92%) → the "next steps" chapter → present.

**Activity.** Fast-forward-watch the arc from the dashboard: 1–13. Pick any stage
of `/pipeline` that was sabotaged in class and explain, un-prompted, what a real
pipeline would do there.

**Assessment.** Open-ended (two options):
- *Talk*: 3 minutes, "how does a change get to prod here?", must name culture,
  branch, gate, image, desired state, infra.
- *Write*: the integration project checklist from `exercises.md` as a blog draft.

**Output.** The course's final artefact: a runnable `api-rules` image +
`kubectl apply`/`terraform plan` files that a senior could honestly accept.

---

## Integration project

Ship the real API, talk about it, prove it locally:

1. `mvn -f eighth-api-rules/app/pom.xml test` green.
2. `docker build -f devops-zero-hero/deploy/Dockerfile.api-rules -t api-rules .`
   (repo-root context) and `docker run -p 8080:8080 api-rules`.
3. Name the GitHub Actions job that would gate the push, and the stage of
   `/pipeline` it matches.
4. Read `deploy/k8s/api-rules.yaml` aloud: three replicas, rolling update,
   readiness probe.
5. `terraform plan` the API infra; point at the `aws_ecs_service` block and say
   what the controller will keep true.

Rubric in `exercises.md`. Everything above is executable on this machine with
Docker installed — no cloud credentials required.