# presentation.md — A 12-minute demo of the DevOps Zero→Hero lab

*The exact click sequence through the UI, what appears on screen, and the
one-line takeaway to say at each stop. Everything runs in a browser at
`http://localhost:8092` — no cloud, no terminals, no installs.*

**Setup (do this before the audience arrives):**
`make start-devops` at the repo root, then open
`http://localhost:8092` in a maximised browser. That's the whole setup.

---

## 0 — The hook (60 seconds) · `http://localhost:8092`

**Action:** Nothing to click yet. Read the room with the home page.

**On screen:**
- Headline: *"DevOps isn't something you watch, it's something you do."*
- A **Ship meter** at 92% — "share of concepts that end up practised or shipped".
- Tiles per theme, and a nav with eleven stops: Concepts, Containers, IaC, Git, Monitor, Security, Quiz, Flashcards, Ship it, Sources.

**Say:** "This course is built from one 5-hour video. The difference: every
acronym the video explains, this lab lets you *poke, break and heal*."

---

## 1 — The centerpiece: the integrated Container arc · click **Containers**

This is the reason the nav has a single **Containers** entry instead of three
separate labs. One page walks the whole ship-an-image story in three panels.

### Panel 1 — "The pipeline ships it" (CI/CD)

**Action:** In the *Make the pipeline fail at* dropdown choose
**Unit & integration tests** → **Run pipeline**.

**On screen:** Eight stages. Checkout and Compile go green, then the third
stage turns RED — *"FAIL — order test red — expected 201, got 422"* — and
every stage after it falls to *"— skipped · never reached"*.
Verdict: **RED**, jobs **2 / 8**, first red job **stage 3**,
*"the pipeline stopped at the first failure ("tests")"*.

**Say:** "The pipeline stops the train the moment a job fails. Nothing ships
that didn't earn its way here. That's CI/CD's whole job — and it cost one
click."

### Panel 2 — "The build packs it" (Docker)

**Action:** *What changed in the source?* → **the base image** → **Rebuild image**.

**On screen:** All five layers REBUILT (`9s + 55s + 26s + 14s + 3s`), verdict
**107s wall-clock**, *"every layer below it rebuilds. Whole image from
scratch."* — zero CACHED.

**Action:** Now **pom.xml (dependencies)** → **Rebuild image**.

**On screen:** The two base images stay **CACHED · 0s**; dependencies, the jar
and the runtime COPY rebuild — **43s**. Then tick **force --no-cache** →
**Rebuild image** and everything rebuilds cold (**107s**).

**Say:** "Watch *what a change costs*. Editing one source file is 17 seconds
of rebuild; touching the base is the whole image. Docker's superpower isn't
speed — it's knowing *what's still good after you change something.*"

### Panel 3 — "The cluster runs it" (Kubernetes)

**Action:** *Kill a pod* → **pod 2** → **Reconcile**.

**On screen:** Five pods become four + a replacement:
`api-rules-aaaaa-2 · Replaced`, `api-rules-aaaaa-4 · Starting`,
ready **2 / 3**. Verdict:
*"Pod 2 died — the Deployment controller noticed 2/3 ready and scheduled a
replacement. Self-heal counter: 1. Desired state stays at 3 replicas."*

**Action:** Clear kill pod, tick **rolling update** → **Reconcile**.

**On screen:** Same pods, new generation, *"no downtime, 3/3 ready."*

**Say:** "Kubernetes doesn't fix pods, it *reconciles reality to desired
state* — forever. The sim never tells you; it shows you, one click at a
time. And all three panels share one vocabulary: *desired state,
CACHED vs REBUILT, RED stops the train* — the same words the real
`course.yml`, `Dockerfile.api-rules` and `api-rules.yaml` use."

---

## 2 — Terraform: say it, don't spray it · click **IaC**

**Action:** *Change to the desired state* → **add a Postgres cluster** →
Command **terraform plan** → Run.

**On screen:** One diff row: `aws_rds_cluster.app_db · add`, stats
**7 already in sync / 1 to touch**, verdict *"Planned: 7 unchanged, 1 to
add… review it like a pull request."*

**Action:** Same change but **terraform apply** → Run.

**On screen:** Same diff, verdict flips to *"Applied: the RDS cluster is now
provisioned."*

**Action:** Command **terraform destroy** → Run.

**On screen:** The whole environment cascades away in dependency order —
10 destroy rows — *"no stale cloud bills, and the same code rebuilds it
tomorrow."*

**Say:** "Infrastructure as code means *plan → review → apply, diff like a
pull request*. Terraform's job, in fact, is *having nothing to do* — apply
with no change and it tells you: **7 modules unchanged**."

---

## 3 — Observability: turn a metric into a 3 a.m. page · click **Monitor**

**Action:** Tick **pod went unhealthy**, *leave* **auto-heal enabled**
unchecked → **Sample now**.

**On screen:** Uptime 99.4%, P95 **2100ms**, error rate **4.2%**, phone
**PAGED** — alert log `PAGE: API_RULES · P95 latency 2100ms · error rate 4.2% ·
pod unready — unacked`.

**Action:** Tick **auto-heal enabled (orchestrator)** → **Sample now**.

**On screen:** Metrics heal back overnight — 99.9%, **245ms**, 0.3%, phone
**quiet** — *"you slept through the night."*

**Say:** "Monitoring's job is to give you the number someone will page you
about, *before* it reaches that number. Here's what auto-healing buys: a nod,
not a phone call."

---

## 4 — Security: score the request, not the customer · click **Security**

**Action:** Client → **spammer**, Bot-likeness **90**, leave the key ticked →
**Run guard**.

**On screen:** Three checks flagged, risk score **100/100**, verdict
**BLOCKED** — *"risk score 100/100 crossed the threshold."*

**Action:** Tick the key back to *off* on a clean browser request →
**Run guard**.

**On screen:** **BLOCKED** before any scoring runs — *"no API key. The edge
guard refuses anonymous traffic first."*

**Say:** "Security is day one, not day 90. Bots don't get a hearing — and
neither does a request without a key."

---

## 5 — Git: history as a time machine · click **Git**

**Action:** In the dropdown above the log choose **feature/login**.

**On screen:** Main's linear history diverges into two login commits
(*"feat: JWT auth"*, *"feat: Neon Postgres wiring"*) then reconverges —
the classic branch-and-merge shape, drawn live.

**Say:** "Git is a time machine with a branching *maybe*. This graph makes the
mental model — not the commands — the lesson."

---

## 6 — Concepts: the drawer deep-dive · click **Concepts**

**Action:** Open the **Docker** card's drawer.

**On screen:** The card flips open with the *bullet list taken from the real
video chapter* — *"portable unit"*, *"docker build -t"* — and a one-liner
naming the chapter that raises it.

**Say:** "Every card is traceable to a timestamp in the source material. No
summary invented after the fact — the teaching points come from the taught
artifact."

---

## 7 — Assessment is built in · click **Quiz**, then **Flashcards**

**Action:** In the Quiz, answer a question correctly.

**On screen:** The card turns green: **Correct** + the explanation.

**Action:** Deliberately answer another one wrong.

**On screen:** **Not quite** — *"You picked: … Expected: …"* plus the
explanation, and a *Try another answer* button that swaps the card back.

**Action:** In Flashcards, pick a theme deck and flip a card.

**On screen:** Click-to-flip cards — front term, back definition.

**Say:** "Formative assessment lives *inside* the tool. Nobody grades you;
every answer teaches you something, whether right or wrong."

---

## 8 — Close the loop · click **Ship it**, then **Sources**

**Action:** Read *Ship it* — the arc mapped onto the real `api-rules` API.

**On screen:** Every lab lands on one production artifact.

**Action:** Then **Sources**: the chapter map with video timestamps —
`00:50:33 CI/CD pipelines`, `01:03:21 Docker crash course`,
`01:55:39 Kubernetes` — each linking straight into the lab that practises it.

**Say:** "One video, one arc. Every timestamp has a playground."

---

## The virtues this sequence demonstrates

1. **Zero friction.** No cloud accounts, no terminal, no install — the whole
   syllabus is a URL. The success criterion is 200 ms for any click, not a
   setup ritual.
2. **"Do", not "watch".** Every concept is a fault you can inject
   (sabotage a pipeline stage, kill a pod, make a pod sick, send a spammer);
   *violated expectations* are the pedagogy.
3. **The sims are honest, not fake.** Layer names quote the real
   `Dockerfile.api-rules` line-for-line (`eclipse-temurin:17-jre`,
   `COPY src + RUN mvn package`), and the four real artifacts ship beside the
   sims — `course.yml`, `Dockerfile.api-rules`, `api-rules.yaml`,
   `terraform/main.tf` — ready to graduate to. This honesty is why an
   audience member noticing *"there's no Terraform in my Dockerfile"* is
   itself the lesson (see `simulations-explained.md`).
4. **One vocabulary across labs.** CACHED / REBUILT, desired state,
   plan → apply, RED stops the train — the sims teach the *shared mental
   model*, so the acronyms become one system.
5. **The integrated Container arc.** Three formerly-separate labs merged into
   one page whose panels swap independently (verified: breaking panel 2 never
   disturbs panels 1 or 3). The dependency chain is the display.
6. **Assessment embedded.** Self-grading quiz + revision decks + a
   timestamped chapter map, all inside the tool.
7. **The contract is testable.** 42 MockMvc tests treat the *rendered HTML*
   as the contract — every page, every fragment, every verdict string
   (a recent audit caught and fixed six broken `.formatted()` verdicts, a
   non-cascading Docker rebuild rule, IaC off-by-ones, and bewailed checkbox
   ordering). The demo you just gave is the same thing the test suite
   guarantees on every build. A malfunction here isn't a story — it's a
   failing test.

---

### Presenter cheat-sheet (the exact clicks)

| Stop | Nav / URL | Click | Watch |
|------|-----------|-------|-------|
| 0 | `/` | — | Ship meter 92% |
| 1a | Containers → panel 1 | fail at **Unit & integration tests** | RED, 2/8, never reached |
| 1b | panel 2 | change **base image** → then **pom.xml** → tick **--no-cache** | 107s → 43s → 107s |
| 1c | panel 3 | kill **pod 2** → tick **rolling update** | Replaced + Starting, counter 1; then no downtime 3/3 |
| 2 | IaC | plan **db** → apply **db** → **destroy** | 7/1 → applied → 10 destroys |
| 3 | Monitor | sick pod, no auto-heal → sick pod + auto-heal | PAGED 2100ms → quiet 245ms |
| 4 | Security | **spammer/90** → drop the key | BLOCKED 100/100 → BLOCKED no key |
| 5 | Git | **feature/login** | branch divergence |
| 6 | Concepts | open **Docker** drawer | video bullets |
| 7 | Quiz + Flashcards | answer right, answer wrong, flip | Correct / Not quite / flip |
| 8 | Ship it → Sources | — | arc → timestamps |

*End on the tagline that frames the whole course:*
**"A lesson plan is a system, not a prompt."**