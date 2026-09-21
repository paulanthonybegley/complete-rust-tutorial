# DevOps Zero → Hero — Exercises

Each lesson has a small exercise you can complete **with nothing but this
repository and Docker**. Steps marked `[app]` are done in the running lab on
<http://localhost:8092>; steps marked `[kit]` use `deploy/`; steps marked
`[voice]` are spoken to a partner, not typed.

Do the exercises in order. The integration project at the end is the exit
ticket.

---

## Lesson 1 — culture

1. `[voice]` Finish the sentence in one breath with no product names:
   "DevOps is …".
2. `[app]` `/concepts` → "What DevOps is". List the three bullets that quote the
   video's own words.
3. Write the "works on my machine" problem as a single-line bug report that a
   pipeline would catch.

## Lesson 2 — version control

4. `[app]` `/git` — draw the graph. Label: `main`, the feature branch, the
   merge point, `v1.0.0`.
5. `[app]` `/git?branch=feature/login` — what new commit appears that `main`
   doesn't have? (It's the video's own auth work.)
6. `[kit]` In `eighth-api-rules/app`, create a branch, change one comment in
   `api-rules.http`, commit. Explain to a partner why the PR would be "small".

## Lesson 3 — CI/CD

7. `[app]` `/pipeline` → Run. Record: how many stages passed, verdict.
8. `[app]` `/pipeline?failAt=tests` → Run. Which stages never ran? What does the
   verdict line tell a senior?
9. `[kit]` Open `.github/workflows/course.yml`. Which job is the test gate, and
   which line stops the deploy if tests are red?

## Lesson 4 — containers

10. `[app]` `/docker` (change `src`). Which layers CACHED, which REBUILT?
11. `[app]` `/docker?change=base`. Why does *everything* rebuild? (Caption it.)
12. `[kit]` `docker build -f devops-zero-hero/deploy/Dockerfile.api-rules -t api-rules .`
    Watch the build output and match real lines to the lab's `CACHED`/`REBUILT`.
13. Run the API container and curl `/products` with an `API-Version` header —
    that is Lesson 7's "the API we just shipped".

## Lesson 5 — orchestration

14. `[app]` `/k8s` — 3 replicas, kill pod 2. Sequence the pod states you watched
    (Running → … → Running).
15. `[kit]` Underline in `deploy/k8s/api-rules.yaml`: `replicas: 3`, rolling
    update knobs, readiness vs liveness probe.
16. `[voice]` "The cluster never checks the pods' opinion. What actually tells
    it a pod is healthy?" (Answer: the probes.)

## Lesson 6 — IaC

17. `[app]` `/iac` plan with no change. What did the verdict call it?
18. `[app]` `/iac?change=db` plan. Name the resource and its action symbol.
19. `[kit]` `terraform plan` in `deploy/terraform/` (or read `main.tf` if no
    account). Which resources would be created? Which block maps to lesson 5's
    replicas?

## Lesson 7 — ship the API

20. `[voice]` Walk the six-box `/ship` loop end to end. Say what stops at each
    box.
21. `[kit]` From `deploy/README.md`, list the four artifacts and which chapter
    each proves.
22. `[app]` On `/ship`, which step does `make docker-run-devops` satisfy for the
    *course* itself?

## Lesson 8 — databases

23. `[kit]` In `terraform/main.tf`, quote the comment that admits the password
    is a placeholder, and rewrite the line the way a real pipeline would.
24. `[voice]` "Why isn't the connection string in the repo?" One sentence.

## Lesson 9 — observability

25. `[app]` `/monitor` steady state: availability %, order of magnitude for
    allowed downtime.
26. `[app]` unhealthy + **no** auto-heal — what fires? Then auto-heal on — what
    happened instead?
27. Write a 3-step runbook for the 3 a.m. page.

## Lesson 10 — auth

28. `[app]` `/flashcards` security deck: flip JWT, RBAC, session vs JWT. For each,
    say the back-side definition in your own words.
29. `[kit]` In `eighth-api-rules/app`, find the version-negotiation layer
    (`WebConfig` / `API-Version` header). Write one sentence: "roles would gate
    here because …".

## Lesson 11 — security

30. `[app]` `/security` table: browser/bot-like-5/with-key, browser/no-key,
    spammer/with-key. Record ALLOWED/BLOCKED and the rule that decided each.
31. `[voice]` "Why middleware and not the controller?" (One answer: one layer,
    before your code runs.)

## Lesson 12 — dockerize the course

32. `make docker-run-devops` and browse the course **from the container** at
    :8092. Run `/docker` inside it — same layers, packaged this time.
33. Read `devops-zero-hero/Dockerfile` top to bottom. What does the final stage
    *not* contain?
34. `docker images` — compare the runtime image to the Maven build stage's size.
    Why is the gap so large?

## Lesson 13 — the arc

35. `[voice]` 3 minutes: "how does a change get to prod here?" Must use all of:
    culture, branch, gate, image, desired state, infra.
36. Open `/quiz`, answer all 12. Then `/flashcards` — flip through the whole
    `Culture→Ship` deck until the 17 cards stop needing a reveal.

---

## Integration project — "Ship `api-rules` for real, locally"

Exit ticket. Complete all steps on this machine (Docker required), then present
`[voice]`.

| # | Step | Evidence to show |
|---|------|------------------|
| 1 | `mvn -B -f eighth-api-rules/app/pom.xml test` | all tests green |
| 2 | `docker build -f devops-zero-hero/deploy/Dockerfile.api-rules -t api-rules .` | image listed, multi-stage output |
| 3 | `docker run -d -p 8080:8080 --name api-rules-lab api-rules` then curl `/products` with `API-Version: v2` | JSON + version header respected |
| 4 | Point at `course.yml` and the `/pipeline` lab | you can name the gate that blocks red |
| 5 | Read `deploy/k8s/api-rules.yaml` aloud | replicas, rolling update, readiness vs liveness |
| 6 | `terraform plan` in `deploy/terraform/` (or explain every block from `main.tf`) | plan matches the `/iac` lab's diff shape |
| 7 | Clean up: `docker rm -f api-rules-lab` | no orphans |
| 8 | `make test` from the repo root | every course green |

### Rubric

- **Pass (ship)** — 8/8 with spoken command of steps 4–6.
- **Merit (ship + harden)** — 8/8 plus: you explain why `runAsNonRoot` matters
  in the manifest, or you spot that the workflow's deploy job is a stub and say
  what a real one would do.
- **Distinction (ship + own)** — 8/8 plus you *show* a red pipeline: sabotage
  `/pipeline?failAt=tests` and narrate why main stayed deployable.