# Kubernetes Zero → Hero — kubectl simulator course

A 12-station, browser-based `kubectl` course that teaches the arc from "what is
the control plane" to "ship it", running entirely in a local Spring Boot +
Thymeleaf + htmx simulator. No Docker, no cloud, no real cluster — everything
you need to run it is the JDK already on this machine, plus `make`.

Pairs with the **DevOps Zero → Hero** course (`../devops-zero-hero/`), which
takes you from Git → CI → containers → cluster. This one is the *simulator
half of the Kubernetes chapter*: the standalone `k8s` app makes the kubectl
lessons hands-on on your own machine.

## What you'll learn (12 stations)

| Station | Page    | Chapter       | It answers |
| --- | --- | --- | --- |
| 01 | `/arch`     | 0:01:42 | The control plane: who writes the truth |
| 02 | `/manifest` | 0:21:02 | Manifest → YAML → API versions |
| 03 | `/pods`     | 0:28:55 | Pods & namespaces — same command, two storylines |
| 04 | `/resources`| 0:41:43 | Requests, limits, QoS |
| 05 | `/probes`   | 0:51:01 | Liveness vs readiness: two verbs, one pod |
| 06 | `/exec`     | 0:58:20 | exec, cp, and the talk-with-the-app story |
| 07 | `/config`   | 1:05:34 | ConfigMaps, secrets, env vs mounts |
| 08 | `/deploys`  | 1:28:11 | Deployments, ReplicaSets, rollout |
| 09 | `/storage`  | 1:52:34 | PV, PVC, storage classes |
| 10 | `/services` | 1:41:xx | Stable addresses over churning pods |
| 11 | `/netpol`   | 2:15:08 | Network policies (deny-all-first) |
| 12 | `/ship`     | the arc  | Integration: walk the whole arc |

Each station has a **learning goal, sequence, activity, assessment, carry-out,
accessibility, and teacher decision** — the nine-input lesson-plan framework
from `OLD.txt`, applied station by station.

## Docs (read these before you teach)

- `lesson_plans.md` — the 12 lesson plans (the "system, not a prompt" core)
- `exercises.md` — the per-station carry-outs + assessment prompts
- `solutions.md` — answer keys + the scoring rubric
- `education.md` — teacher delivery guide (sequence, timing, mistakes-to-catch)
- `presentation.md` — slide-arc outline for the 45-minute talk
- `simulations-explained.md` — what the simulator does under the hood,
  and how each station maps to the real cluster video

## Up and running

```
make start-k8s       # starts the kubectl simulator on :8093 (no Docker needed)
make stop-k8s
make status          # shows k8s-simulator :8093 alongside the rest
make test            # runs the k8s + devops test suites (no Docker needed)
make docker-run-k8s  # build + run the course as a container on :8093
```

Head to <http://localhost:8093> and start at `/arch`.

## Same nine inputs, one frame per station

The course was built directly from `OLD.txt`'s nine lesson-plan inputs —
learning goal, sequence, assessment evidence, learner profile, prior
knowledge, activity, output requirements, accessibility/supports, and teacher
decisions. `lesson_plans.md` is the exact mirror of that framework, written
specifically for this repository's `k8s` simulator so nothing needs a cloud
account.

---

## Why a simulator?

`k8s/app` ships a real `kubectl` console that answers every command a learner
can type — `get`, `describe`, `run`, `logs`, `exec`, `cp`, `rollout`, `apply`,
`delete`, `scale`, probes, network policies, QoS, and the self-heal loop. It's
the same `$ kubectl` you'd paste into a real terminal; just safer, because the
cluster is a Spring in-memory `SimCluster` and nothing you `delete` survives
except the lesson.

That's what makes this "hero" rather than "zero": you can kill every pod in
the lab and watch the Deployment bring them all back — without fear, because
the worst thing that happens is a red line on a simulator that costs nothing.
