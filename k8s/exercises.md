# Kubernetes Zero → Hero — Exercises

Twelve station labs for `/k8s`, each one a *carried* task you finish in the
simulator. Every exercise names the endpoint, the exact command, and the edit
that proves you did it. All of it runs offline on :$(PORT_K8S) — no cloud.

> Mark a station by running the action button; `/` marks it anyway when you
> change something, so do the *edit* first and read the event line after —
> that line is your receipt.

## Reader profile (from OLD.txt · learner profile)

A well-trained Linux/DevOps student — comfortable in a shell, has read a
Spring Boot `application.yml`, and is terrified specifically of *manifests*
and of *losing a cluster*. The sim exists so both fears die in a sandbox
first.

---

## 1 — arch (`/arch`)

Walk the apply→watch trace once, one step at a time carefree. Then run the
step control (`step=1..7`) to make the kubelet step light up first — that's
the remote piece most people can't picture until it's made local.

**Carry-out.** `api server → <etcd, scheduler, controller manager>, kubelet`.
Re-write it as a *sentence* without the word "server".

**Check.** "Who is the single writer of truth?" → `etcd`.

---

## 2 — manifest (`/manifest`)

Load the `api-rules` Deployment preset biased toward `apps/v1`, toggle JSON,
toggle to YAML back. The lab is: does the same tree validate after switching
`kind: Deployment → ReplicaSet`? It must *not* — that's the career-saving
moment.

**Carry-out.** `apiVersion + kind` as one line you can read out loud:
`apps/v1` is the *contract*, `Deployment` is the *noun*.

**Check.** The hidden validator rejects `networking.k8s.io/v1` for a Pod.

---

## 3 — pods (`/pods`)

**Open the console on the Pods tab, then run:**
`kubectl get pods -A` (the echo is `$ kubectl get pods -A`, `-A` = all
namespaces). Inspect the flaky pod's image — it's `busybox` "every 7 minutes".

**Carry-out.** Delete the flaky pod 3× in a row and note the new name each
time — a new pod is *provisioned*, the old name is dead.

**Check.** "Why did the pod come back after you `delete`d it?" (Its
Deployment wanted it — the ReplicaSet kept the count.)

---

## 4 — resources (`/resources`)

Open `api-rs-a1` and read the QoS line. Slide the CPU request above the limit
and hit validate: the API should refuse (request can't exceed limit on the
same container). Then drop it back and change the *limit* instead — a limit
above the node is a different kind of "no" (the node drops *you* at
schedule time, not apply time).

**Carry-out.** A line "request < limit ≤ node capacity" scrawled over the
fit cube.

**Check.** "Which field does the *limit* protect — you or the node?"

---

## 5 — probes (`/probes`)

Cycle the four scenarios (`Healthy / Exceeded / Entropy / NotReady`) and watch
the verdict + event line. The tab is a *decision tree*: what changes the
restart decision is not the value but the *kind* of probe.

**Carry-out.** One sentence: "readiness stops <traffic>, liveness restarts
<the container>."

**Check.** "A pod is healthy but slow — which probe fires and what does it
*not* do?" (Liveness — and it does not restart; readiness stops traffic.)

---

## 6 — exec (`/exec`)

`kubectl exec api-rs-a1 -- ls /app` then `-- env`. Then the big one:
`kubectl cp` a file from your simulated host into the pod, then `cat` it
inside and `cp` it back. Work is one `cp` away from being on the node.

**Carry-out.** List the four files you now know live "inside the pod"
(config dir, secret, app jar, and your copied file) — each with which *path*
you used to find it.

**Check.** "You need a file from pod A in pod B. `cp` it to where first?"
(The shared volumes is one answer; the host workspace `cp` goes through is
another.)

---

## 7 — config (`/config`)

Create a ConfigMap from a literal; mount it as a volume on `api-rs-a1`; edit
the file *without* deleting the pod; read it back with `exec` — the value is
live. Then do the same with a Secret and note: env vars are snapshotted at
start, mounts are live.

**Carry-out.** `kubectl get configmaps,secrets -n <your ns>` showing both
*created* + in use.

**Check.** "Secret changed — why is the pod still old?" (env snapshot at
start; only mounts pick it up live.)

---

## 8 — deploys (`/deploys`)

Scale `web` to 3, cut a pod, watch `/deploys` reconcile the count back to 3
(the *self-heal* loop). Then `rollout undo` back to the previous image.

**Carry-out.** Sequence a screenshot of the reconciler's diff telling you
`Replicaset ... created` — the "what's the Deployment thinking" log line.

**Check.** "The Deployment converged to 3 — to what *definition* of 3?"
(To the desired replicas in its spec — the number is a spec value, not an
observation.)

---

## 9 — storage (`/storage`)

Create a PVC of 1 Gi; see it bind to a PV; note the storage class line. Then
`kubectl delete pvc` — the PV goes *Released* but stays (Retain). Recreate
the claim and watch it NOT rebind to the same PV.

**Carry-out.** A line: "PVC is the request, PV is the resource, storage class
is the provisioner's business."

**Check.** "Retain vs Delete — which leaves your data after the whole claim
is gone?"

---

## 10 — services (`/services`)

Create the Service; `get svc` — find the ClusterIP. Kill pod #2 with the
deployment running at 2; the endpoints recompute but the ClusterIP stands
still. That's the whole point of the tab.

**Carry-out.** `ClusterIP : never chnges` + one pod IP that did change.

**Check.** "Why connect to a Service rather than a pod IP?" (Pod IPs change
every reschedule; the Service IP is stable for the app's life.)

---

## 11 — netpol (`/netpol`)

Set the pod selector + policy to `deny-all ingress` — then check the matrix:
SQL-like, every ingress cell red except the one pod that matches your
selector. Then sweep an egress row to its own allow once.

**Carry-out.** Color the cell matrix by hand once: red/red/…/green on the
ingress column for the allowed pod.

**Check.** "Before any policy: will a cluster pod to pod talk right now?"
(Default allow — that's why deny-all first matters.)

---

## 12 — ship (`/ship`)

Run every prior station again but in ship order: apply the manifest → the
deploy object → watch replicas → exec — all with the *console* (`/kubectl`),
not the click-tabs arena you used before. The ship tab's reward is the tiny
prompt: `make test` should be green.

**Carry-out.** Copy your `/ship` page's final "running: 3/3, ready" line; note
the one deployment named after the app (`api-rs`).

**Check.** "At the end, which object's replicas counter are we really reading?"
(The Deployment → the ReplicaSet the Deployment owns.)

---

## Scoresheet

- 12 exercises × `done` = station meter full.
- To call yourself Zero→Hero: complete `1, 4, 7, 8, 12` with *no* console
  errors, then do `2,3,5,6,9,10,11` casually.

**Hard-carry:** the `.run/k8s.log` file is the only thing a pro glances at;
make it say `Started K8sZeroHeroApplication in Xs`.
