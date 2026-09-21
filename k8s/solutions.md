# Kubernetes Zero → Hero — Solutions

The "answers" that belong with `lesson_plans.md` and `exercises.md`. Every
external check / assessment / device carries-one-sentence answer from those
two files, collected in one place. The app never prints these — the instructor
reads them from here.

---

## The one-sentence answers (quote these)

| Question (from plans) | Answer the instructor says |
| --- | --- |
| "Who is the single writer of truth?" | The API server — the only component with an etcd connection. |
| "Can you edit etcd directly to fix a bug?" | No client exists; that's exactly the design. Controllers write via the API, not to etcd. |
| "Where do you put the network policy YAML?" | The same `k8s/` folder of the app that ships the pods. Not in ops' inbox. |
| "What's the difference between a liveness and readiness probe firing?" | Liveness → restart the container. Readiness → take it out of rotation, do **not** restart. |
| "When does SRE use liveness vs readiness?" | Liveness for data-loss or wedged state; readiness for slow but healthy (queue-full). |
| "Why do we run the guides you watch FIRST?" | The arc is the curriculum: architecture → manifests → pods → resources → probes → exec → config → deploys → storage → services. The clicking is the recap. |
| "Why does limit create traffic?" | Limit is a cap, not a speedometer. Above it: OOMKill / retry storm / chaos. |

---

## Stations — expected simulator output

### `/manifest` — validate against the REAL bundled presets

Each "preset" in the dropdown is a small, intentionally-correct manifest. The
validator does real YAML → Map → tree → DeepCompare. If the tree's
`apiVersion`/`kind`/`metadata.name` is not the expected, the console prints
`!=` errors. A valid manifest that doesn't match the preset still validates —
it's *just not the preset*.

**Expected carry-out.** A tab showing your one-line description of what
`apiVersion` means (contract vs file format), plus one example of the
`apps/v1` line changing to `networking.k8s.io/v1` and what that *would* say
(that's a different object — pulled from its own controller).

### `/pods` — the self-healing demo

The four tabs (busybox flaky, nginx always-on, api-rs) should show a steady
`Running`, one `Failed`, and `CrashLoopBackOff`. After `kubectl delete pod
flaky` the flaky pod comes back — because a Deployment (or equivalent
controller) owns it. **If it does NOT come back, the learner missed the
controllers tab**: the ReplicaSet / Deployment is the *owner*, delete removes
the Pod object only, the control loop re-converges.

### `/resources` — the fit question

Two cells always opposite: node has room → ✓; node is full (or the pod asks
for more than the node has) → Offer repeated / Pending. The "correct" carry
is: "the resource's `requests`/`limits` map directly to the node's Line-2"
(allocatable). No other wording is "as correct".

### `/probes` — direction of the answers

`Healthy` scenario → all three probes green; `Exceeded` → the `returns` column
goes red only for the resource limit (the value is above limit → read-only). 
Remember: probes say *leave traffic on / restart / none*.

---

## The quiz — correct answers

| # | Question | Accept (any of) |
| --- | --- | --- |
| 1 | Why does the API server write to etcd first? | "single writer, truncation-free, everyone else reads" |
| 2 | What does `kubectl run` do that `kubectl apply` doesn't? | "starts an interactive/CLI container; apply is declarative file" (accept: "run is imperative") |
| 3 | What is the difference between a Pod and a Node? | "pod = container(s) + namespace; node = the machine it lands on" |
| 4 | Which probe restarts the container? | liveness |
| 5 | Service vs Deployment — which exposes the IP? | Service |
| 6 | What does `Limit` mean vs `Request`? | "cap vs guaranteed" |
| 7 | Why does a ConfigMap reload without a restart? | volume mount = live; env = snapshot at start |
| 8 | ReplicaSet vs Deployment? | "RS keeps N running; Deployment owns the RS" |
| 9 | Default deny what? | "netpol: empty is allow, present is deny-all-then-allow" |

_Order matches the on-screen stations; the simulator maps each to its station._

---

## Edge cases the simulator handles (so you can demo them)

1. `kubectl get pods` in a namespace with nothing → `No resources found in
   api-rs namespace`. (Empty is a real answer, not an error.)
2. `kubectl delete pod <owned-pod>` → the ReplicaSet recreates it instantly.
   This is the *self-heal* behavior — worth a screenshot.
3. `kubectl cp <file> <pod>:<path>` where `<path>` dir doesn't exist → cp
   fails gracefully ("No such file or directory") — because the container's
   filesystem is not your host workspace.
4. `rollout undo` past the last revision → "error: no rollback possible —
   there is only one revision." (A real cluster says the same.)
5. Resources: setting a request above the node capacity → pod stays Pending
   (scheduler can't place it). Setting a limit with no request → QoS drops to
   best-effort; the health value is the survival odds, not the number.

---

## What "correct" looks like for the carry-outs

The `lesson_plans.md` carry-outs are the low-bar proof. The app shows a green
`✓` after each. Here's the full expected set, one line each:

- **arch** — "the API server is the single writer of truth; etcd is only for
  it."
- **manifest** — "YAML and JSON are the same tree; the apiVersion is the
  contract."
- **pods** — the 3-phase screenshot (Running / Failed / CrashLoopBackOff) and
  the label line: "a pod is the smallest Atomic unit".
- **resources** — "(request, limit) → (guarantee, cap)" and the fit cell.
- **probes** — the four verdicts; *liveness restarts, readiness does not*.
- **exec** — a file moved both ways with `cp` and the path table.
- **config** — ConfigMap + Secret both mounted; live vs env note.
- **deploys** — 3→0→3 screenshot with the events line (reconcile input).
- **storage** — the PVC/PV bind + the Retain vs Delete line.
- **services** — ClusterIP circled "never changes", one changed pod IP.
- **netpol** — matrix screenshot with one green cell annotated with the rule.

All of these fit in a single terminal page. That's the point: one page of
kubectl output is the whole semester's evidence.

---

## Marking guide (borrowed from devops-zero-hero)

- **Full credit**: the carry-out line names the *object*, not the button.
- **Half credit**: the carry-out is the right object but the wrong direction
  (e.g. "liveness stops traffic").
- **No credit**: the carry-out quotes the video verbatim without tying it to
  the sim output (e.g. "watch the self-heal").
- **Always use**: the `/status` "done" lighthouse as the attendance record.
