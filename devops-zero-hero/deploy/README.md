# Ship kit — make the `eighth-api-rules` API play the role the video's
# `acquisitions` API plays. These files are runnable teaching artifacts that
# match every deploy chapter in the course.

## What is here

| Artifact | Chapter it proves | Run it |
| --- | --- | --- |
| `Dockerfile.api-rules` | Dockerize the API | `docker build -f devops-zero-hero/deploy/Dockerfile.api-rules -t api-rules .` |
| `.github/workflows/course.yml` | CI/CD + "tests never red" | push to GitHub; the `api-rules` job runs `mvn test` and builds the image |
| `k8s/api-rules.yaml` | Kubernetes desired-state / self-heal | `kubectl apply -f devops-zero-hero/deploy/k8s/api-rules.yaml` |
| `terraform/main.tf` | Infrastructure as Code | `terraform plan` then `terraform apply` in a real AWS account |

## The shipping loop (mirrors the course's Ship page)

1. **Build locally** — the Maven build is the source of truth:
   `mvn -f eighth-api-rules/app/pom.xml test && docker build .../Dockerfile.api-rules`
2. **Push on green** — GitHub Actions runs the same test gate. A red test
   stops the run before anything ships (the pipeline lab's `tests` sabotage).
3. **Declare the desired state** — `k8s/api-rules.yaml` says "3 replicas,
   non-root, healthy". The cluster's reconciliation loop turns that into
   reality, exactly like the K8s lab's `Replaced`/`Starting` pods.
4. **Reproduce the infra** — `terraform/main.tf` recreates VPC + RDS + ECS
   deterministically; the IaC lab shows the same `+ create` diff.

## Notes

- Context for `Dockerfile.api-rules` is the repo root, because a pipeline
  normally checks out the whole monorepo.
- The API has no `actuator`, so probes hit `/products` instead of `/actuator/health`.
- `terraform/main.tf` contains a placeholder DB password — the lesson is that
  secrets never belong in Terraform; they belong in a secrets manager.