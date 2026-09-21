# Education guide — delivering the Kubernetes Zero → Hero course

Built for the same teacher who ran the DevOps Zero → Hero course: you have a
room of adults, a Spring Boot lab already warmed up on :8092, and now a second
simulator on :8093 that lets them type `kubectl` all afternoon without a
cluster. This is the "teacher decisions" input from `OLD.txt` made concrete.

---

## The frame, in one line

The ops-y courses are not "watch then click". The arc is:

    watch a chapter → read one question on screen → type the command →
    read the event line the simulator wrote back → put the one-line rule
    on the wall.

You are not a lecturer. You are the person who stops them from typing
`kubectl kubectl get pods -A` and expects it to work — i.e. who reminds them
**the console already shows `$ kubectl`; type only the tail.**

> The single most common stuck-learner moment, in every coach run we've
> observed: someone pastes the full `kubectl get pods -A` into the box
> (correct in a real terminal!) and the sim answers `Error: unknown command
> "kubectl"`. Handle it on day one by pointing at the `$ kubectl` prompt and
> saying "you're already inside kubectl". Prefer the `-o wide` button they can
> press with no typing to prove the pattern.

---

## Session shape (one-day / two-day / self-paced)

**One day (10:00–16:00).** Stations in the fixed order, one led station an
hour, `/quiz` and `/ship` as the bookends. Every anonymous self-pace learner
can stretch or compress a station ±15 minutes; the *arc* must not be
reordered, because the trace at lesson 1 is the story that lessons 2–12 pay
off.

**Two days.** Day 1: watch + walk (lessons 1–7). Day 2: operate (8–12),
finish with the integration lab. The second day's first ten minutes are a
re-run of lesson 1's trace — the whole point of having brought a simulator: a
loop you can re-walk.

**Self-paced.** The station list IS the syllabus. `/` shows the meter so they
know exactly where they are and how much is left — same feeling as a
real-world cluster being "mostly reconciled".

---

## The nine inputs, mapped to *this* room

| Input | What it means here | Proof you did it |
| --- | --- | --- |
| Learning goal | "kubectl the sim and survive your own cluster" | The `/quiz` scorecard |
| Sequence | The 12 stations in video order | The meter fills L→R |
| Assessment evidence | Every station's check + the final quiz | The `/status` "done" states |
| Learner profile | Adults who ran the DevOps course; confortable in shells; scared of YAML | They finish `/manifest` without asking *why YAML* |
| Prior knowledge | Git, Docker, a deployed API — the *devops-zero-hero* lessons 1–3, 8 | Day 2 opens with them re-walking the trace unprompted |
| Activities | The carry-outs (below) | Green verdicts in each tab |
| Output requirements | One script line + file per station | `k8s/app/.run` capture + `kubectl get all` |
| Accessibility | Short sentences, terminal is the medium, everything on one page | A learner who skipped the video still passes every station |
| Teacher decisions | This guide | — |

---

## The carry-outs, as a check-off list (these are the real curriculum)

Every "carry-out" in `lesson_plans.md` is written so it fits on one line of a
terminal. Hand these as a printed checklist on day one. A learner who can
*reproduce* the line on request, in the sim, has learned it. Anything on this
list they can't reproduce is the next morning's warm-up.

1. One sentence with the words *single writer* in it (etcd trace).
2. The three `apiVersion` values the sim names, in the order the lab asks.
3. The flaky-pod carry-out: "pod vs node" in five words.
4. The `request < limit` scrawl and what the `Limits` dialog refuses.
5. Which probe *restarts* the container (liveness) — and it does NOT stop
   traffic (readiness does that).
6. `kubectl cp` file moved into a pod and back, screenshotted twice.
7. "secret changed but the pod is stale — why?" (env = snapshot at boot;
   volume = live).
8. The self-heal screenshot: 3 → kill → 3, with the ReplicaSet line that
   explains who is counting.
9. The one-line: PVC is the request, PV is the resource, storage class is
   the provisioner that fills it.
10. ClusterIP: "never changes" circled in the endpoints table.
11. The netpol matrix with the one green cell annotated.
12. The final `/ship` output: a Deployment reconciling to 3 across a kill.

---

## Assessment, the only three forms

1. **The station check** (one sentence, oral). The question at the bottom of
   each lesson. Learners answer *without looking at the sim*.
2. **The `/quiz`"s twenty questions**. Summative; the scorecard + the two
   "why" free-text answers are all a teacher reads.
3. **The integration carry-out**. Proof they can run a *whole arc* — not a
   station — from memory. Full credit only if the deployment self-heals in
   front of you.

---

## Adult-learning: the two sentences an instructor must NOT say

- ❌ "Just watch the video again."
- ✅ "Your flaky pod restarting three times in a row IS the lesson — who
  decided it should, and on what schedule? Point at the line."

Every remediation path must send the learner back to the *event line*, not
back to the video, because the event line is the part they can already see.
The video is the second pass.

---

## Accessibility

- Terminal first. Every activity can be completed reading only the `.term out`
  blocks; the video is enrichment, never the *only* source of the answer.
- The `/manifest` tab has a JSON ↔ YAML toggle so no learner is skipped by a
  YAML wall. Lean on it if a learner freezes on indentation.
- Color is never the only channel: the QoS fit cube and the netpol matrix
  always print a written verdict, never just a red cell.

---

## What "done" looks like for YOU

After this course a stopped learner can:

- explain, in one sentence, why `etcd` is not a database you "log into";
- read a manifest and predict what `kubectl apply` will *change*, not just
  what it will create;
- predict the two places a broken probe appears (event line + restart
  counter) without being asked;
- and ship a 3-replica service end-to-end in the sim, walking away from a
  pod just as it's being replaced.

That is the Zero→Hero bar, and it is entirely local — no cloud account, no
Docker daemon, no `kubeconfig` to lose.
