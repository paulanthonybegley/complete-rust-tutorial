# Lesson Plan — Observability Engineering Made Easy (Hands-On Lab System)

A lesson plan is a system, not a prompt. This plan feeds a live, self-contained
observability demo (`observability-shop`) into the nine-input framework so every
lab lands on the same stack the industry actually runs.

The course mirrors "Mastering Observability Engineering Made Easy!" (The Serious
CTO): logs, metrics, traces, correlation, sampling, alerting, log hygiene, and
an on-call drill — turned into interactive labs you click, not slides you read.

---

## 1. Learning goal

Collectively, by the end of the course each learner can:

- Name the **three pillars** (logs, metrics, traces), say what each is good at,
  and explain why logs + traces need each other.
- Run a **single action** and watch it become one log line, one counter, and one
  trace across microservices (the "keystone" moment).
- Explain **RED** (Rate, Errors, Duration) and point at the Prometheus query that
  answers each letter.
- follow a **failed checkout** from a dashboard number, to a trace, to the
  logged exception, to the failing component — and state the root cause.
- Explain **head vs tail sampling**, apply a keep-error / drop-OK policy, and
  read the decision log.
- Explain the **alert feedback loop**: rule → Alertmanager → webhook → on-call
  record, and what makes a good `for:` window.
- Practice **log hygiene** (redact secrets before they hit Loki) on real data.
- Run an **incident drill** in under five minutes: read the error code, form a
  hypothesis, solve, and justify the verdict.

Success is demonstrated when a learner can do every one of the above *without
being told the button to click.*

---

## 2. Lesson sequence

| # | Lesson | Lab (URL on `localhost:8083`) | Concept | Builds on |
|---|--------|-------------------------------|---------|-----------|
| 1 | The keystone | `/keystone` | All three pillars on one action | — |
| 2 | Logs | `/logs` | Structured logs, levels, MDC fields, Loki labels | 1 |
| 3 | Metrics | `/metrics` | Counters, histograms, p95, RED method | 1 |
| 4 | Traces | `/traces` | Trace/span/service model, Jaeger UI | 1 |
| 5 | Correlation | `/correlate` | `trace_id` joins logs↔traces, root cause | 2, 4 |
| 6 | Sampling | `/sampling` | Head vs tail sampling decisions | 4 |
| 7 | Alerts | `/alerts` | Rules, Alertmanager, webhook→DB loop | 3 |
| 8 | Hygiene | `/hygiene` | Secret redaction in logs ("safe vs leak") | 2 |
| 9 | Incident drill | `/incident` | On-call: read, hypothesize, solve | 5, 7 |
| 10 | Scale | `/scale` | What the same stack looks like at scale | all |

Every lab works the same way: a **control panel** generates controlled traffic,
a **live evidence pane** shows the result pulled from Jaeger / Prometheus /
Loki, and a **hypothesis** is checked against the UI.

---

## 3. Assessment evidence

Learners show mastery when they can produce the evidence below — most of it is
visible inside the running app, so assessment doubles as practice.

- **Keystone:** after one `POST /keystone/run`, point at the exact log line, the
  `checkout_total` counter row, and the trace navigation in Jaeger for that one
  request.
- **Metrics:** generate a "mixed" batch; read back `success`, `card_declined`,
  and p95 from `/metrics/stats` and the Grafana dashboard, and quote the rate in
  ops/s.
- **Traces/correlation:** from an order id, open the trace, read the child span
  (`payment.gateway.charge`, `db.order.insert`, `db.stock.update`), open its
  logs, and state one root cause.
- **Sampling:** run a 20-request batch; classify every row in `trace_decisions`
  as keep (reason `error`/`slow`/`sampled`) or drop (reason `dropped`).
- **Alerts:** push failure traffic, wait for `FailedCheckoutRateHigh` to fire,
  confirm a row lands in `alert_events`, and quote the `for:` window.
- **Hygiene:** leak and safe runs differ only in one line: `email=alice@…` vs
  `email=[email]`, `password=supersecret-123` vs `password=[redacted]`.
- **Incident:** mapping each error code to the right hypothesis
  (`card_declined`/`insufficient_funds` → gateway; `timeout` → timeout;
  `fraud_check` → fraud), and solving the order.

Exit check (5 minutes, no UI): given `checkout_failure_total{reason="card_declined"}`,
write the rate query; given an order id, name the three hop-spans you expect to see.

---

## 4. Learner profile

- **Who:** developers, SREs-in-training, students; comfortable with the command
  line and browsing an API. No prior observability tools knowledge assumed.
- **Why:** many learners know *of* Prometheus/Jaeger/Loki but have never run a
  story that connects them on real traffic.
- **Disposition:** prefers seeing over reading; confidence comes from watching
  numbers change as they click. Fast experimenters; short attention per rabbit
  hole.
- **Pace:** one lab ≈ 10–15 minutes; the whole course ≈ 2–3 hours including the
  drill. The stack runs before the first click (`docker compose up -d`), so the
  "setup tax" is one command.

---

## 5. Prior knowledge

Build on what learners usually already bring:

- **HTTP:** they know a POST with a body spawns `order_id`, status codes. Used in
  lesson 1 as the "free" hook.
- **Databases:** queries like `SELECT … FROM orders` (used for incident + sampling).
- **Terminal + Docker:** `docker compose up -d`, `docker exec obs-db psql …`.
- **Framing gains:** anyone who has done SQL knows "querying Loki is just logging
  with a WHERE"; anyone who has done `redis-cli` or `psql` instantly g tomorrow
  with `docker exec`. Reuse those muscle memories rather than introducing new CLIs.

**Known gaps to fill, not repeat:** how a distributed trace is assembled, why
aggregate KPIs need histograms, why raw logs are not a profiling tool, and the
difference between head and tail sampling. These are *taught*, not assumed.

---

## 6. Learning activities

Each lab follows the same activity template so learners build a repeatable ritual:

1. **Watch** (30 s): read the lab's concept card on the page.
2. **Crank** (1 min): generate controlled traffic — success / mixed / fail /
   slow batches (`/keystone/run`, `/metrics/traffic`, `/alerts/stress`,
   `/sampling/generate`, `/hygiene/log`, `/incident/generate`).
3. **Look** (2 min): read the live evidence pane, then open the same data in the
   tool-of-record: Jaeger (`:16686`), Prometheus (`:9090`), Grafana (`:3000`,
   admin/admin), Loki (via Grafana Explore).
4. **Hypothesize** (1 min): predict what the next card will show before clicking.
5. **Explain** (2 min): write the one-line formula/query/state that produced what
   you saw (e.g., the `rate()` expression behind a dashboard panel).

Cross-cutting activities: one **read-the-query** challenge per metrics lab, one
**two-truths-and-a-lie** about sampling per lesson 6, one **blame-the-minimum**
(which `for:` window would you set?) per lesson 7, and the timed **incident
drill** in lesson 9.

---

## 7. Output requirements

- **Format:** these lessons are one `README.md` course with `exercises.md`
  (learner-facing) and `solutions.md` (instructor-facing), plus this plan.
- **Constraint 1 — zero magic:** every number a learner reads must be traceable
  to a query, a rule, or a table that exists in the repo
  (`docker/prometheus/alert-rules.yml`, `db/init.sql`, `application.yml`).
- **Constraint 2 — clickable over screenshotty:** labs reference the running app
  and its URLs; no fake /static mock screenshots.
- **Constraint 3 — scoped depth:** exercises end at the exact concept the lesson
  names; no wandering into Grafana panel engineering.
- **Constraint 4 — language:** active voice, second person ("you"), a metric
  name or span name quoted on every page, and consistent naming
  (`checkout_total`, `payment.gateway.charge`, `trace_decisions`, …).
- **Deliverable:** tiny — a single page install (`docker compose up -d`) and a
  browser; nothing else to install.

---

## 8. Accessibility & supports

- **Low-input mode:** every lab has a one-click generate button and all evidence
  is *plain text rendered by the app* — no required colour reading; the UI also
  renders a text fallback for every status (see `partials/*.html`).
- **Slow pacing:** tail sampling deliberately waits (config `decision_wait: 10s`,
  longer end-to-end) before a trace
  shows in Jaeger — that pause doubles as a teaching beat ("the decision keeps
  spans buffered"), and the UI says so instead of failing silently.
- **Reduced cognitive load:** each page names the tool to open and the port;
  controls are grouped (traffic first, evidence below).
- **English-language learners / low-vision in Grafana:** dashboards reuse the
  same single datasource `prometheus` with identical metric selector naming as
  the lab pages, so the story is consistent; keyboard-only operation is
  possible everywhere (all POSTs are plain forms).
- **Self-check rails:** each exercise closes with a "Checkpoint" the learner can
  verify alone (a count, a row in a table, a firing alert).

---

## 9. Teacher decisions

- **Ordering:** do lessons 1–5 in order (correlation is the payoff of 2 + 4);
  lessons 6–8 are independent, so they can be exchanged if a class is already
  alerting literate.
- **Depth dial:** for a one-hour session, cut lessons 6 and 10 and compress 5
  into 2; for a full workshop, keep everything and end with the drill as a
  capstone.
- **Tool preference:** Prometheus is the reference read for *every* metric lesson
  (not Grafana) — Grafana is framed as a composition layer, not a calculator.
  Jaeger is read directly before anything is composed into Grafana.
- **Pace control:** each "hide the answer" trick (solutions behind
  `solutions.md`) is aimed at one question per lesson; ad-hoc browse is fine —
  exploration is the point of the evidence panes.
- **Verdict over rubric:** in the incident drill, the grade is *was the verdict
  justified*, not "right component" — learners may legitimately re-run a
  generate to get more data before answering.

---

## Running this course

```bash
cd master-observability
docker compose up -d          # one command; app on :8083
# open http://localhost:8083  -> card grid; start at /keystone
```

Learner-facing tasks: `exercises.md`. Instructor answer keys, expected table
contents, and the incident taxonomy: `solutions.md`.