# DevOps Zero → Hero — Solutions

Verified against the running app (`make start-devops`, port 8092). These are
answers, not command outputs — run each step yourself and compare.

---

## Lesson 1 — culture

1. **"DevOps is …"** — a culture and a mindset: a way of working where
   developers and operators share ownership, feedback flows continuously, and
   shipping is a habit — not a team, not a tool.
2. **Bullets that quote the video** (`/concepts`, What DevOps is): DevOps is
   "a culture, not a tool"; it is "a mindset, a way of working"; the video's
   phrase "something you do, not something you buy"; and the death of
   "works on my machine".
3. **Bug report**: `REQUIRED: app runs ONLY on the author's machine; no
   reproducible environment, no documented start command — pipeline cannot
   build it. Severity: blocks every later stage.`

## Lesson 2 — version control

4. **Graph** (`/git`): `main` runs left-to-right with the tags; commits shared
   with the feature branch stay on the shared spine until `feature/login`
   diverges; `v1.0.0` tags the merged, shippable point.
5. **New commit on the branch**: the JWT-auth feature commit (`JWT auth +
   role-based access`), which `main` does not have while the branch is open.
6. **Small PR**: one-file, one-purpose, no whitespace changes — the pipeline
   reviews the same diff the human reviews.

## Lesson 3 — CI/CD

7. **Clean run**: 8/8 stages pass; verdict GREEN. Stages: checkout, compile,
   tests, scan, image, push, deploy, verify.
8. **Sabotaged `tests`**: tests prints `✗ failed`, `scan image push deploy
   verify` report `SKIPPED (never reached)`; verdict RED. A red test stops the
   train so main stays deployable — the rest of the pipeline is only meaningful
   on a green base.
9. **Gate in the workflow**: the `api-rules` job (runs `mvn -B … test`);
   `deploy` needs it (`needs: api-rules`) so a red test never reaches it.

## Lesson 4 — containers

10. **`src` change**: `FROM`/`WORKDIR`/base layers CACHED; the `COPY . .` layer
    and everything below it REBUILT.
11. **`base` change**: every layer below the changed base rebuilds too — the
    cache key for each later layer changed. Verdict: "whole image rebuilds".
12/13. **Real build matches**: `docker build -f …/Dockerfile.api-rules` shows
    `CACHED` lines for untouched base-dependency steps and fresh `RUN mvn`
    layers, exactly like the lab. `curl -H 'API-Version: v2'
    http://localhost:8080/products` returns paged JSON — the shipped API.

## Lesson 5 — orchestration

14. **Kill sequence**: `Running` → pod 2 becomes `Replaced`/deleting →
    controller schedules a replacement (declared 3, observed 2) → `Starting`
    → readiness probe passes → `Running`. End state 3/3.
15. **Manifest**: `replicas: 3`; `maxUnavailable: 1, maxSurge: 1` (rolling —
    one replacement at a time); `readinessProbe` on `/products` decides *traffic
    eligibility*, `livenessProbe` decides *restart me*.
16. **Who knows the pod is healthy**: the probes — the cluster observes
    readiness/liveness; the pod's own opinion is never the sole authority.

## Lesson 6 — IaC

17. **No change**: verdict "no-op" — plan shows 0 to add / 0 to change;
    infra "already match(es)" the code.
18. **`change=db`**: `aws_rds_cluster` with `+ create` (tf-add). The table also
    highlights the diff symbol per resource.
19. **`terraform plan`**: creates VPC, 2 subnets, security group, RDS
    Postgres, ECS cluster + task definition + service. The `aws_ecs_service`
    block's `desired_count = 3` is lesson 5's replicas.

## Lesson 7 — ship the API

20. **The loop boxes**: local build → test gate → image → registry/push →
    desired state → infra; a failure at any box stops the change before prod.
21. **Four artifacts**: `Dockerfile.api-rules` (Dockerize the API),
    `course.yml` (CI/CD), `k8s/api-rules.yaml` (Kubernetes desired state),
    `terraform/main.tf` (IaC).
22. **`make docker-run-devops`** satisfies "the artifact exists and runs" for
    the course container (image → container → :8092), the same step that
    Dockerfile.api-rules plays for api-rules.

## Lesson 8 — databases

23. **The guilty comment**: `password = "change-me-in-secrets-manager"` —
    "the lesson is that secrets never belong in Terraform". Rewrite: read the
    password at apply time from AWS Secrets Manager (or `${var.db_password}`
    sourced from CI secrets), never from source control.
24. **Why not in the repo**: the connection string is a credential — repo +
    secrets = leaked credentials; rotation and per-environment values both
    belong to the secret store.

## Lesson 9 — observability

25. **Steady state**: availability 99.95% (≈ 4.4 h/yr allowed downtime); quiet
    panels, no alerts.
26. **No auto-heal**: PAGED — the on-call box fires ("the 3 a.m. page"),
    availability collapses. **With auto-heal**: the controller replaces the
    dead member and the panel returns to quiet without paging anyone.
27. **Runbook**: (1) hit the health/readiness endpoint — is the app answering?
    (2) check the last deploy tag and recent config change; (3) tail
    structured logs for the failing request pattern.

## Lesson 10 — auth

28. **Cards**: JWT — signed, stateless token proving *who* the caller is;
    RBAC — role → permissions map deciding *what they may do*; session vs
    JWT — server-held state vs portable claim; CORS + security headers — the
    browser-side boundary.
29. **Version layer**: roles would gate beside `API-Version` because any
    authorization that must inspect every request belongs in one negotiation
    layer, not scattered handler-side.

## Lesson 11 — security

30. **Posture table**:

    | client | bot-like | key | verdict | rule |
    |---|---|---|---|---|
    | browser | 5 | yes | ALLOWED | under the 62 threshold + valid key |
    | browser | 5 | no | BLOCKED | no API key |
    | spammer | 90 | yes | BLOCKED | risk score ≥ threshold |

31. **Middleware not controller**: one layer, one policy, runs *before* your
    handlers — and bot signals (browser fingerprint, rate, payload shape) only
    exist at that edge.

## Lesson 12 — dockerize the course

32. The containerized course is the identical app on :8092 — evidence that the
    artifact and the dev run agree.
33. **Final stage excludes**: Maven, the JDK (JRE only), the source tree, and
    the build caches — just the packaged jar + a JRE.
34. **Size gap**: the runtime image has no toolchain, no `.m2`, no `target/`
    intermediates — a JRE jar image is a fraction of the Maven stage.

## Lesson 13 — the arc

35. **Model answer**: "A developer works from a feature branch (version
    control); a PR is reviewed and merged to main; CI runs the test gate; when
    green, a Docker image is built and pushed; Kubernetes reconciles the
    deployment to the desired state; infrastructure is declared in Terraform
    so any environment is reproducible."
36. **Quiz**: 12/12 (each wrong answer explains itself — e.g. q9 explains that
    Terraform does not replace Kubernetes). **Flashcards**: flip through all
    17 cards in the Culture→Ship deck without opening any reveal twice.

---

## Integration project — evidence checklist

All steps run without cloud accounts:

1. `mvn -B -f eighth-api-rules/app/pom.xml test` → BUILD SUCCESS (19 tests).
2. `docker build -f devops-zero-hero/deploy/Dockerfile.api-rules -t api-rules .`
   → image `api-rules` listed.
3. `docker run -d -p 8080:8080 --name api-rules-lab api-rules` +
   `curl -H 'API-Version: v2' http://localhost:8080/products` → JSON with
   content/version respected (the versioning law from `eighth-api-rules`).
4. `course.yml`: `api-rules` job gates; `deploy` `needs` it — matches the
   pipeline lab's `tests` sabotage.
5. `k8s/api-rules.yaml`: `replicas: 3`, `maxUnavailable: 1`/`maxSurge: 1`,
   readiness `/products` after 10 s, liveness after 15 s.
6. `terraform plan` output mirrors `/iac?change=db`'s `+ create` diff table.
7. `docker rm -f api-rules-lab` → none left behind.
8. `make test` from the repo root → every course's suite green.