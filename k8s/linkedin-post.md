# Kubernetes Zero → Hero — LinkedIn post

**"kubectl is a CLI. Kubernetes is a loop."**

I built a browser-based **kubectl simulator** and wrote the lesson set that
makes the control plane real in your hands — not a cloud account in sight.
12 stations, one in-memory cluster, `$ kubectl` you can actually type.

What the simulator teaches: the whole arc in one console.

- 🏛️ `/arch` — who writes the truth (etcd, API server, scheduler, kubelet)
- 📝 `/manifest` — YAML → versioned objects, `apply -f` not `create`
- 🫛 `/pods` — pods & namespaces; one command, two storylines
- 📉 `/resources` — requests/limits and why bursty pods get evicted
- 💓 `/probes` — liveness vs readiness, same verb, different job
- ⚙️ `/exec` — exec, cp, logs: getting *inside* the workload
- 🗂️ `/config` — ConfigMaps + Secrets, env vs mount
- 🔄 `/deploys` — Deployments, ReplicaSets, `rollout undo`
- 💾 `/storage` — PVs, PVCs, storage classes without a cloud bill
- 🌐 `/services` — a stable address over churning pod IPs
- 🛡️ `/netpol` — deny-all-first, then open exactly one port
- 🚢 `/ship` — walk the whole loop until the self-heal kicks in

Kill the flaky pod and watch the controller bring it back. That's the loop
lesson — and the simulator makes it safe to fail on purpose.

A lesson plan is a system, not a prompt. So every station carries a goal,
a hands-on activity, an assessment, and a carry-out. Because Big G's rubric
— pods, Deployments, Services, network policies — is only 4 lines, and the
gap between "read it" and "do it" is a whole career.

Repo: [complete-rust-tutorial/k8s](https://github.com/your-org/complete-rust-tutorial)
`make start-k8s` → http://localhost:8093
