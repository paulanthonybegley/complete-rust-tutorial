# Kubernetes Zero → Hero — Lesson Plans

Twelve stations, one per chapter of the video. Each lesson is built from the
nine design inputs in `../OLD.txt` — learning goal, sequence, assessment
evidence, learner profile, prior knowledge, activities, output, accessibility
and teacher decisions — and every activity runs against the simulator in
`app/`, so nothing here needs a real cluster or a cloud account.

> "A lesson plan is a system, not a prompt." Each plan names the goal, the
> sequence, the check and the output; every command below is typed into the
> `/kubectl` console exactly as written (the `$ kubectl` prefix is the prompt,
> not something you type).

## Learner profile

A working developer who has been through the DevOps Zero → Hero course (or has
the equivalent): comfortable with `git`, can read a Spring Boot `application.yml`,
has shipped a container. Knows *that* `kubectl` exists; has never used it on a
real cluster some other team owns. Afraid of two things: deleting something
irrecoverable, and "manifests" (the thousand-line YAML myth). Prior knowledge
check: `kubectl get pods -A` in the simulator returns a table — if that happens
in the first minute, the station order is right.

## The stations at a glance

| # | Station   | Video chapter                    | The one claim it proves          |
|---|-----------|----------------------------------|----------------------------------|
| 1 | `/arch`   | What the control plane does      | The API server is the only writer of truth |
| 2 | `/manifest`| YAML → manifests → API versions | A manifest is a *diff request*, not a file upload |
| 3 | `/pods`   | Pods & namespaces                | A pod is the smallest schedulable unit |
| 4 | `/resources` | Requests, limits, QoS          | Limits are quotas, not speed limits |
| 5 | `/probes` | Liveness & readiness             | Readiness is a traffic gate; liveness is a restart trigger |
| 6 | `/exec`   | exec & cp                        | Files move through a *workspace*, not your laptop |
| 7 | `/config` | ConfigMaps, secrets, env, mounts | Config changes are deploys, not edits |
| 8 | `/deploys`| ReplicaSets, Deployments         | Desired state is why you never "SSH in to fix it" |
| 9 | `/storage`| PVs, PVCs, storage classes       | Storage is requested, never given by hand |
|10 | `/services`| Services end-to-end            | A Service is a stable address over unstable pods |
|11 | `/netpol` | Network policies                 | Default deny; everything else is an explicit carve-out |
|12 | `/ship`   | The whole arc                    | The operator loop, end to end     |

---

## Lesson 1 — The control plane

**Learning goal.** Say in one sentence what the API server does, and name the
one rule that makes a cluster trustworthy. (00:01:42.)

**Sequence.** Watch the intro + architecture chapter → open `/arch` → walk the
8-step trace (your kubectl → API server → etcd → scheduler → controller
manager → kubelet → runtime → proxy).

**Activity.** In `/arch`, step 1→8 one by one. At each hop answer "who wrote
that to persistent storage?" — only step 4 and the original apply may. That is
the whole point: *the API server is the single writer of truth.* No operator
ever writes straight to etcd.

**Assessment.** "A colleague says they'll fix the deployment 'directly in
etcd'. What do you say?" (You don't; that path is not a supported client.)

**Output.** A one-line diagram of the trace: `you → API → etcd, watch out to
scheduler → controller → kubelet → runtime`.

**Accessibility.** Every hop is also the text under the trace cards, not just
the video frame. Vocabulary: control plane, data plane, desired state — each
glossed on the page.

**Teacher decision.** If a learner can already recite "control plane / data
plane", let them skip to step 3 and just narrate the watch→act loop. The arc
is the curriculum only for learners who have not seen it.

---

## Lesson 2 — Manifest & YAML

**Learning goal.** Read any manifest without fear, and understand why
`apiVersion` matters more than the file format. (0:21:02, 0:24:52, 0:38:58.)

**Sequence.** YAML rules → manifests → API versioning, in that order, because
versioning is the payoff.

**Activity.** `/manifest`: pick the `deploy-web.yaml` preset located in a real YAML, then
`apply` it. Now toggle JSON — YAML and JSON are the same tree; the manifest is
the tree, not the formatting. Then switch `apiVersion: apps/v1` to
`apps/v1beta1` and validate: the simulator refuses (real clusters did too;
`apps/v1beta1` was removed a decade ago).

**Assessment.** "What does the `apiVersion` line communicate?" (The schema and
the behavior contract — not the file type.)

**Output.** Annotate one manifest line per language feature: `kind` = which
object, `spec` = desired state, `metadata` = name/labels. Three annotations.

---

## Lesson 3 — Pods & namespaces

**Learning goal.** Explain why a single pod is a unit of scheduling and why
namespaces keep a busy cluster legible. (0:28:55·0:34:00.)

**Sequence.** Watch pods + namespaces → `/pods` → the flaky pod.

**Activity.** In `/pods`, `get pods -A`, then find the top `Running` columns and `kubectl get pod
flaky`. Run `kubectl delete pod flaky` again — the container exits and is
re-created (imagePullBackOff → Running). That is *not* a Deployment healing; it
is the kubelet keeping the pod's single container alive bounds that pod. A pod
is not high availability; a Pod *set* is.

**Assessment.** "Delete a pod manually: what is the difference between a pod
that a Deployment owns and one it does not?" (The first is *replaced*; the
second is gone.)

**Output.** Two-line contrast: pod = single unit; ReplicaSet = the unit that
survives. Add `-o wide` to see NODE/IP as the columns that tell you *where*.

---

## Lesson 4 — Requests, limits, QoS

**Learning goal.** Read `resources:` and predict what happens when a container
hits its limit. (0:41:43, 0:44:56.)

**Sequence.** Watch the resource chapter → `/resources` → the capacity fit demo.

**Activity.** In `/resources`, add a container to an already-full node until the
score tips to *Unschedulable*, then add a second node. Read the `Fit` verdict
line: it names the failing dimension (CPU/Memory) — that is the scheduler's
only language.

**Assessment.** "Limit = 0.5 CPU — is that a cap on speed or on *time*?" (On
CPU time consumed; the container can burst up to the limit.)

**Output.** Label the QoS tiers on the page (Guaranteed / Burstable /
BestEffort) with a one-word field each: limits+requests set / limits only /
neither.

---

## Lesson 5 — Probes

**Learning goal.** Decide between liveness and readiness without a lecture.
(0:51:01.)

**Sequence.** Watch probes → `/probes` → each scenario in the dropdown.

**Activity.** Run the four scenarios and read the verdict line. The quiz
answer hides in the *direction* of the failure: "the pod is healthy but slow"
→ readiness (stop sending traffic) vs "the app is wedged" → liveness (restart
the container). Do not memorize the definitions; recall the *direction*.

**Assessment.** A probe fires at 3 a.m. The pod restarts. Which probe was it,
and which one would *not* have restarted it? (Liveness restarts; readiness
would have just unpod the service.)

**Output.** A table: probe / what it gates / title case example from the lab.

---

## Lesson 6 — exec & cp

**Learning goal.** Move files into and out of a container without a running
session. (0:58:20.)

**Sequence.** Watch exec+cp → `/exec` → the workspace.

**Activity.** In `/exec`, `exec api-rs-a1 -- env` to see the two containers, then
`cp` a file from the *workspace* into the pod, read it with `cat`, and `cp` it
back. Note the file you dragged started in the host workspace — because `kubectl
cp` traffic is not a shell pipe; it routes through the *kubelet's* filesystem.

**Assessment.** "Why does `kubectl cp` need a path inside the pod to exist
first?" (It mounts into an already-running container's fs, not a new one.)

**Output.** A saved `flag.txt` you placed and pulled back, with the two
command lines that did it.

---

## Lesson 7 — Config: ConfigMaps, secrets, mounts

**Learning goal.** See a config change as a *deploy*, because that is how it
reaches a running app. (1:05:34, 1:09:49, 1:18:05.)

**Sequence.** Watch ConfigMaps → secrets → env + volume mounts → `/config`.

**Activity.** In `/config`, mount the ConfigMap as a *volume* and reload — the
file updates in the pod without a restart. Now mount as *env* and reload: the
env var is frozen until the pod restarts. That contrast is the entire lesson.

**Assessment.** "Your secret changed but the pod still serves the old value.
Why?" (Two flat possibilities: not mounted as a live volume, or the process
cached it at startup.)

**Output.** One line: "env = snapshot at start; volume = live view." Both
demonstrated on screen.

---

## Lesson 8 — ReplicaSets & Deployments

**Learning goal.** Explain desired state and prove the self-heal loop.
(1:28:11, 1:36:01.)

**Sequence.** Watch the deployment chapter → `/deploys` → kill a pod.

**Activity.** Set 3 replicas → 3/3 Running. Kill pod #2. Watch it become
*Terminating*, a replacement enter *Starting*, then 3/3 again — with no human.
The ReplicaSet controller did that on a schedule of its own. That is the whole
idea behind "the cluster converges".

**Assessment.** "A pod dies at 3 a.m. Who notices, and on what schedule?" (The
ReplicaSet controller's informer watch — seconds, not a cron.)

**Output.** A screenshot of the 2→3 window and one sentence naming the
controller that closed the gap.

---

## Lesson 9 — Services

**Learning goal.** Give an unstable set of pods one stable address.
(1:41:xx.)

**Sequence.** Watch services → `/services` → the endpoint matrix.

**Activity.** In `/services`, scale a Deployment to 2, then kill pod #2 while
watching the endpoints table. The DB-NAME, HOST-NAME, and ENDPOINT stay the
same: the Service's IP, the ClusterIP, is stable across pod churn — which is
why apps connect to a Service, never to a pod IP.

**Assessment.** "Your pod IP changes on every delete. What still works?"
(The Service's stable address + its selector-driven endpoints.)

**Output.** A before/after (`2 endpoints → 1 → 2`) trail from live command
output, with the Service's ClusterIP annotated: "this number never changed".

---

## Lesson 10 — Storage

**Learning goal.** Distinguish a persistent volume from a claim, and from a
storage class. (1:52:34.)

**Sequence.** Watch storage → `/storage` → the PVC walk.

**Activity.** In `/storage`, claim a PVC, verify it binds to the PV, and see the
storage class fill in the "how" (dynamic vs static). Delete the claim and note
the reclaim policy line: `Retain` keeps the PV around — which is the word that
really protects data, not the PVC itself.

**Assessment.** "Your pod is gone. Is your data?" (Only if a PVC + PV with the
right reclaim policy drew the storage; the pod's own fs vanishes with it.)

**Output.** A one-line proof: the same claim, recreated after pod death, binds
to the *same* PV name.

---

## Lesson 11 — Network policies

**Learning goal.** Encode "default deny, then allow" and read an egress/ingress
matrix. (2:15:08.)

**Sequence.** Watch netpol → `/netpol` → the matrix.

**Activity.** In `/netpol`, select *default deny*, then allow ingress on port
8080 onlycars only from the same namespace. Read the one cell in the matrix that
turns green — that cell is the *rule* your pods now live by interviewed. Then
change the direction to egress and watch the counterpart cell flip.

**Assessment.** "A netpol has no 'allow all'. Show the shape of a permissive
policy." (A policy listing every allowed pairer — there is no wildcard.)

**Output.** A 3×3 verdict table with the allowed cell circled and the rule it
came from written beside it.

---

## Lesson 12 — Ship the whole arc

**Learning goal.** Walk the operator loop end-to-end and know where each piece
found you. (2:41:00 NodePort · 2:45:14.)

**Sequence.** Watch the NodePort + "next steps" → `/ship` → run the 12th
console one whole time.

**Activity.** In `/ship`, run `kubectl get all`, then `kubectl describe pod`,
then the quiz: the quiz is the final *assessment*, not a warm-up. Learners who
finished 1–11 should score it without rereading.

**Assessment.** Table-stakes: name the three control-loop watchers we met
(controller manager infoers, kubelet, kube-proxy) and what each one fights for.

**Output.** The course's last artifact: a comments-inline copy of a Deployment
manifest with all nine earlier ideas annotated (replicas, probe, selector,
resources, mounts, strategy, affinity — seven is fine).

---

## Integration project

Ship the simulator itself, as this course's "what ships" moment:

1. `mvn -f k8s/app/pom.xml test` green (no Docker needed).
2. `make start-k8s` → http://localhost:`${PORT_K8S}`, run `kubectl get pods -A`.
3. Reconcile a Deployment to 3 replicas)Skip a pod and watch self-heal.
4. Read `netpol` matrix aloud: which cell is allowed, which rule made it so.
5. Write the one-line "default deny" claim a reviewer would accept.

Rubric in `solutions.md`. Everything is executable on this machine with
`mvn` — no cloud credentials required.
