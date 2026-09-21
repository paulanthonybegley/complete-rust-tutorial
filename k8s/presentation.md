# Kubernetes Zero → Hero — presentation.md

The one-arc deck for the 12-station seminar, built from the nine inputs in
`../OLD.txt`. A slide per station, a slide for the frame, a slide for the
ship. Total runtime target: 45 minutes of talk, the rest is the simulator.

---

## S0 — The frame (2 minutes)

One claim, one question, one warning.

- **Claim.** A cluster is a machine whose *control plane* you can see from
  one tab — and this whole course you will type into a simulator that cannot
  delete anything you care about.
- **Question.** "Before today, where did `kubectl` talk to — and who was the
  only other writer?"
- **Warning.** The simulator does not know the word `kubectl` at the front
  of a command — you are always already inside `kubectl`. (This is the one
  real-world friction we keep; type the tail, not the whole man page.)

**Slide artefact.** A terminal drawing: `$ kubectl █` with the word
"already inside" parked on the prompt.

---

## S1 — Architecture (2:00)

- The 8-hop trace, one box per hop, one arrow per writer.
- The slide ends with one big word: **etcd** — "the only place the future is
  decided" and the only thing nobody logs into.
- **Turn to the room.** "Who writes the truth?" — surprised that the answer
  is *the API server* and not "the YAML".

**Visual.** API server at centre; everything else either reads it or writes
*through* it reporting state.

---

## S2 — Manifests (1:30)

- YAML = JSON = the same tree; `apiVersion` is the contract, `kind` is the
  noun.
- Demo the *unexpected same-violation*: switch a preserved manifest to
  `apps/v1beta1` → the real API-server refusal shows up ("the API server is
  the only validator; the file is just a request").

**Turn to the room.** "What does `apiVersion` buy you?" ("Permission to
speak — the schema you're allowed to use today.")

---

## S3 — Pods & namespaces

- Pod = smallest schedulable unit. Namespace = the folder that keeps a busy
  cluster legible; `-A` crosses every folder in one read.

**Visual.** Two drawers: one labelled "namespaces cost nothing", one labelled
"default deny at the pod level is the *default* in your netpols".

---

## S4 — Resources & QoS

- `requests` = the ask, `limits` = the cap. Requests and limits both have
  shape (the simulator refuses an upside-down request>limit as fast as a real
  API server).
- QoS tier is the *third thing* you infer — Guaranteed / Burstable /
  BestEffort — before you ever look at `status`.

**Turn to the room.** "Which field protects YOU as the scheduler, and which
protects the NODE?"

---

## S5 — Probes

- Liveness = restart. Readiness = reroute traffic. Startup = let a slow
  first boot breathe. Three verbs, three "buttons".
- In the sim the probe verdicts come from the *event* tab: exhaustion,
  exceedance, entropy — each answers a different "why".

**Rule of thumb on the slide.** "Liveness decides life, readiness decides
mail."

---

## S6 — exec & cp

- `exec` puts YOU in the container; `cp` moves files pod↔workspace. The
  simulator models both so the "file on the node" moment lands: a deployed
  file is a file that lives *in a pod's filesystem*, not on your laptop.

**Turn to the room.** "Where does a file you `cp` into a pod ACTUALLY live?"

---

## S7 — Config: ConfigMaps & Secrets

- Config = a deploy, not an edit. Mounts are live; env is a snapshot.
- The one-off "secret changed, pod stale" quiz answer turns here.

**Visual.** A hand-drawn tape reel: every line is a reconcile pass.

---

## S8 — Deployments & ReplicaSets

- Desired state is the ONLY thing a Deployment remembers; a ReplicaSet is
  the counter; a pod is the counting token; self-heal = the loop that
  converges 3 → 2 → 3.

**Turn to the room.** "You `scale` a Deployment to 2. WHO converges the
count back to 3, and what tells them 3 is the number?"

---

## S9 — Storage

- PVC = the request, PV = the resource, StorageClass = the provisioner.
- The mapping in the sim binds a claim to a volume and bows out (Retain),
  exactly like the not-ship-if-unskippable ReclaimPolicy.

---

## S10 — Services

- Service = a stable address. NodePort = the door outside, ClusterIP = the
  door inside, LoadBalancer = the door with a real public IP.
- In the sim the IP that never changes is the one to point at.

**Visual.** A pole diagram: three doors, one address at the top.

---

## S11 — Network policies

- Default deny is the lesson, not the exception. Then each `allow` line =
  one rule; read a cell in the matrix to see "which rule let a given pair
  through".

**Turn to the room.** "Your pod is blocked. Where do you look first — and
what does 'default deny' say about who you blame?"

---

## S12 — Ship

- The last station is not a recap: it's the single end-to-end "you are the
  operator" moment — reconcile a real Deployment against a simulator, watch
  it converge, then hand in the one artifact: the `/ship` output with the
  replicas line.

**Closing slide.** "Zero → Hero: you type kubectl the way a cluster thinks."

---

## Carries for the presenter

- Every station is a *do*, not a "remember": each ends with a real
  terminal artefact the learner can screenshot.
- The feature-position map is `exercises.md`; the expected outputs live in
  `solutions.md`; the lesson-by-lesson pedagogy is `lesson_plans.md`.
- Time budget: 12 stations ≈ 45 min of slides + the rest sim time. In a
  two-hour slot, cut the slide deck at S7 and let the lab carry the middle.

---

## Closing carry-out (make it yours)

Print the one-liner the learner keeps: **"kubectl is the front door; the
API server is the bouncer; etcd is the back office; everything else just
keeps the lights on."** That sentence is the certificate.
