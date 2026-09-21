# Kubernetes Zero → Hero 🤝 The DevOps Zero → Hero room

Same author voice, same simulator engine, same nine lesson-design inputs from
`../OLD.txt` — a sibling course, not a sequel. The DevOps course ends where a
pipeline pushes an image to a cluster; the Kubernetes course picks up exactly
there: **that image is now running as what?** (Answer: a Deployment's pods,
which the ReplicaSet will recreate behind a stable Service on a supplied PV —
and before you get any of those words back, you'll have typed each one.)

This app is a *read-and-write* `kubectl` simulator. It is **not** a "look at
the picture" demo: everything you could ask a real cluster, you can ask this
one — `get`, `describe`, `run`, `apply`, `rollout`, `scale`, `delete`,
`logs`, `exec`, `cp`, `top`, `explain`, and the network-policy and
resource-QoS outcomes are decided by an actual simulated reconciliation loop,
not by a lookup table pretending to be one.

The only differences from a real cluster are ones that protect the learner:

- **No cloud."** The node "names" `n1a1` are a friendly read of a CIDR; the
  API server, scheduler, controller-manager, kubelet and kube-proxy trace
  steps are events in the browser. Nothing leaves this machine.
- **Nothing is permanent."** Every pod can be deleted and rebuilt; every
  namespace can be wiped. That is the *point* — the first `kubectl delete`
  you run here is the last scary one you'll ever run.
- **The learning is the output.** Each of the 12 stations ends with a
  carry-out you can print and hand in; the evidence is the simulator's own
  console lines (see `lesson_plans.md`).

## What you'll ship

1. A Visual Studio Code–free trace of the control-plane loop (because the
   API server is the single writer of truth).
2. Impossibly many `kubectl get`/`describe` outputs you can actually read.
3. A Deployment that heals itself after a pod it owns dies.
4. A Service that never changes a pod's IPs — and a matrix proving who is
   allowed to talk to whom, rule by rule.

## Fits where the DevOps course left off

| If you finished | You get here |
| --- | --- |
| The `pipeline` lesson | Your image "is out there" as a Deployment |
| The `k8s` station in DevOps | Same manifest, now *reconciled* |
| The `iac` + `security` chapters | The probes + netpol stations feel trivial |
| The `/ship` recap | The whole arc, walked once more for no one but you |

## Run it

- `make start-k8s` → http://localhost:8093 (`PORT_K8S` overridable)
- Stations: `/arch /manifest /pods /resources /probes /exec /config
  /deploys /storage /services /netpol /ship`, plus `/quiz`, `/flashcards`,
  `/sources`
- Docs: `lesson_plans.md`, `exercises.md`, `solutions.md`, `education.md`,
  `simulations-explained.md`, `presentation.md`

DevOps Zero → Hero made you pipeline-literate. This makes you *cluster*-safe —
starting from a simulator that cannot break anything, ending with an
understanding of why every `kubectl apply` you will ever do is a *reconcile
request*, not a file upload.
