# simulations-explained.md — "There's no Terraform in my Dockerfile!"

*The educational explainer for the Container lab, for anyone who opens a
Dockerfile expecting to find Kubernetes, CI/CD, or Terraform inside it.*

> **The short answer:** a Dockerfile is the recipe for exactly one thing — a
> container image. CI/CD, Kubernetes, and Terraform are separate concerns,
> each in its own file, read by its own tool. That separation *is* the lesson.

---

## 1. What a Dockerfile actually is

A Dockerfile is the **recipe for exactly one thing: a container image**. Not a
pipeline, not a cluster, not cloud infrastructure. It answers only:

- what bytes go into this image,
- in what order (each line = a cached layer),
- and what command runs when the container starts.

Nothing else. Here is the real shipping Dockerfile from this course,
`devops-zero-hero/deploy/Dockerfile.api-rules`, line by line:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build   # "builder" stage: full JDK + Maven
WORKDIR /build
COPY eighth-api-rules/app/pom.xml .          #     pull in the dependency list
RUN mvn -q -B dependency:go-offline          #     download deps FIRST (cache them)
COPY eighth-api-rules/app/src ./src          #     then the source code
RUN mvn -q -B package -DskipTests            #     compile + jar

FROM eclipse-temurin:17-jre                  # "runtime" stage: slim JRE only
RUN groupadd -r app && useradd -r -g app app #     non-root user (security)
WORKDIR /workspace
COPY --from=build /build/target/api-rules-0.0.1-SNAPSHOT.jar app.jar
USER app                                     #     run as non-root
EXPOSE 8080                                  #     which port to listen on
HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=3 \
  CMD wget -qO- http://localhost:8080/products | grep -q '"content"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]       #     the process that IS the container
```

Every line is about the **image**, and the layer order is deliberate: the
early layers (the 9s base, the 26s dependency download) rarely change, so
they stay cached. That is exactly what the `/docker` lab animates for you:
`REBUILT · 14s` vs `CACHED · 0s`.

---

## 2. Where CI/CD, Kubernetes, and Terraform actually live

Each concern is a separate manifest, because **each is consumed by a different
tool at a different time**:

| Concern          | File                                     | Tool that reads it | When                                    |
|------------------|------------------------------------------|--------------------|-----------------------------------------|
| CI/CD            | `deploy/.github/workflows/course.yml`    | GitHub Actions     | on every `push` / pull request          |
| Container image  | `deploy/Dockerfile.api-rules`            | `docker build`     | inside the workflow's "Build" step      |
| Kubernetes       | `deploy/k8s/api-rules.yaml`              | `kubectl apply`    | after an image exists                   |
| Infrastructure   | `deploy/terraform/main.tf`               | `terraform apply`  | once, on the cloud account              |

Follow the dependency chain — it is the whole Container-arc flow:

- **CI/CD runs things.** `course.yml` runs the tests first (the "tests never
  red" gate), then calls `docker build` — i.e. **the workflow *uses* the
  Dockerfile**. The pipeline does not live *in* the Dockerfile; it
  *orchestrates* it.
- **Kubernetes wants a ready image.** `api-rules.yaml` says
  `image: ghcr.io/your-org/api-rules:latest` — it *pulls the image the
  Dockerfile produced* and declares desired state: `replicas: 3`, a rolling
  update (`maxUnavailable: 1, maxSurge: 1`), and readiness/liveness probes.
  The yaml does not contain the image's code any more than the Dockerfile
  contains the cluster.
- **Terraform describes the *space* around all of it.** `main.tf` provisions
  the Postgres RDS database the video's API needs, plus a VPC, subnets, and a
  Fargate task whose `container_definitions` reference the same image.
  Terraform never builds containers; it provisions places for them to run.

---

## 3. The mental model — the whole course in one sentence

> A Dockerfile is a **recipe for a dish**. The pipeline is the **kitchen's
> ticket system** (the dish is only made if prep passed). Kubernetes is the
> **head waiter** (keeps 3 portions on the pass — if one plate breaks, the
> recipe is used to remake it). Terraform is the **building's plumbing
> blueprint** (the kitchen exists at all).

So opening the Dockerfile and finding no Terraform is **correct**: the recipe
card never mentions the building, the waitstaff, or the ticket machine.
Mixing them would couple everything together — the anti-pattern this course
teaches you to avoid. Each file changes at its own cadence and is owned by
its own team.

---

## 4. How the four simulations teach this mess-free

The labs are honest — every layer name on the `/docker` page quotes the real
`Dockerfile.api-rules` lines (`eclipse-temurin:17-jre`,
`maven:3.9-eclipse-temurin-17`, `COPY src + RUN mvn package`,
`USER app + HEALTHCHECK + EXPOSE 8080`):

| Lab / panel        | The real artifact it mirrors           | What the sim shows you                          |
|--------------------|----------------------------------------|-------------------------------------------------|
| `/pipeline` (step 1, "the pipeline ships it") | `deploy/.github/workflows/course.yml` | 8-stage CI/CD; sabotage `tests` → run goes RED — in real life the image is never built after red tests |
| `/docker` (step 2, "the build packs it")       | `deploy/Dockerfile.api-rules`         | layer-cache: change `src` / `pom` / `base`, or `--no-cache` — watch exactly which layers REBUILT vs CACHED |
| `/k8s` (step 3, "the cluster runs it")         | `deploy/k8s/api-rules.yaml`           | steady state 1–5 replicas; kill a pod → "Replaced" — the controller re-runs the recipe for a fresh plate |
| `/iac` (step 4, "then automate the place it runs") | `deploy/terraform/main.tf`        | plan / apply / destroy — the same diff shape as a real `terraform apply` |

Each box stays its own box — exactly like the real artifacts. That is the
point: **the files, like the sims, are kept separate on purpose.**

---

## 5. Are the simulations artificial? Yes — and that's the point

Every sim resolves its external world *inside itself*: the pipeline sim does
not run real `mvn`; the layer sim does not call real `docker build`; the k8s
sim has no real `kubectl` and no real cluster (just a pod list held in
memory); the IaC sim produces no real plan/apply/destroy.

The reason is the real dependency chain — every stage of it is cloud-heavy
and side-effectful:

| Real stage needs...                          | ...which is why the sim fakes it |
|----------------------------------------------|----------------------------------|
| GitHub repository + Actions minutes          | a `failAt` parameter prints stage logs |
| a container registry to push to              | layer states computed as strings |
| a live Kubernetes cluster                    | an in-memory pod list            |
| an AWS account + a provisioned Postgres      | a fake plan / apply / destroy    |

So yes: **artificial, intentionally.** The browser can rehearse the entire
Container arc end-to-end with zero cloud credentials, zero `docker` daemon,
and zero terminals.

But faithful, not fake. The sims simulate the *dependency interactions*, not
the mechanics — you still watch a RED run stop the train, a `base` change
cascade through every layer, and a killed pod come back as "Replaced". And
the sim's strings quote the real artifacts line-for-line, so what you click
is what the real files declare.

### What is *not* artificial

The four artifacts ship alongside the sims and are genuinely runnable:

- `deploy/Dockerfile.api-rules` actually builds — verified with a local
  `docker build`, ran on a local port, returned real HTTP 200s.
- `deploy/.github/workflows/course.yml` runs the *real* test suite and a
  *real* `docker build` on GitHub; only the deploy step is stubbed
  (`if: ${{ false }}`).
- `deploy/k8s/api-rules.yaml` and `deploy/terraform/main.tf` are complete,
  valid manifests you can apply once you have a cluster and an account.

That is the "graduation path": rehearse on the sims today, run the real
files tomorrow. The sims don't dodge dependencies so much as **postpone**
them until the lesson is already learned.