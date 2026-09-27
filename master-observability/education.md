# education.md — Observability Shop: What Was Achieved, Why, and How

> The complete education log for the `master-observability` project, written for
> "future me" (and anyone else) who wants to learn from, reuse, or pick up where
> this session left off. It records the goal, the reasoning behind every
> architecture decision, the exact implementation, the things that broke, and
> what each break taught. Every claim about a port, a query, a span name, or a
> table is verified against the running stack — this is not a wish-list of
> features, it is a ledger of working software.

---

## Table of Contents

1. [How to read this document](#0-how-to-read-this-document)
2. [The question this project answers](#1-the-question-this-project-answers)
3. [What "observability" actually means](#2-what-observability-actually-means)
4. [Why a shop, why the playlist, why hands-on](#3-why-a-shop-why-the-playlist-why-hands-on)
5. [The pedagogy: a lesson plan is a system, not a prompt](#4-the-pedagogy-a-lesson-plan-is-a-system-not-a-prompt)
6. [Goals and success criteria](#5-goals-and-success-criteria)
7. [The stack at a glance](#6-the-stack-at-a-glance)
8. [The ten labs](#7-the-ten-labs)
9. [The full telemetry pipeline](#8-the-full-telemetry-pipeline)
10. [What was proven end-to-end](#9-what-was-proven-end-to-end)
11. [Deliverables inventory](#10-deliverables-inventory)
12. [The application core: ShopService](#11-the-application-core-shopservice)
13. [Instrumentation: the OpenTelemetry agent](#12-instrumentation-the-opentelemetry-agent)
14. [The metrics pillar](#13-the-metrics-pillar)
15. [The tracing pillar](#14-the-tracing-pillar)
16. [The logging pillar](#15-the-logging-pillar)
17. [Correlation: order → trace → logs → root cause](#16-correlation)
18. [Sampling: the economics of "keep everything"](#17-sampling)
19. [The alerting loop](#18-the-alerting-loop)
20. [Log hygiene and secret redaction](#19-log-hygiene-and-secret-redaction)
21. [The incident drill](#20-the-incident-drill)
22. [Templates and HTMX](#21-templates-and-htmx)
23. [Docker orchestration and the config-in-image decision](#22-docker-orchestration-and-the-config-in-image-decision)
24. [Spring Boot 4 realities](#23-spring-boot-4-realities)
25. [What broke and what it taught](#24-what-broke-and-what-it-taught)
26. [The lesson system: the 9-input framework applied](#25-the-lesson-system-the-9-input-framework-applied)
27. [Verification: the test plan and its results](#26-verification-the-test-plan-and-its-results)
28. [Honest limitations and tradeoffs](#27-honest-limitations-and-tradeoffs)
29. [Where to go next](#28-where-to-go-next)
30. [Appendices](#appendices)

---

## 0. How to read this document

This file has three parts, mirroring its title.

- **What.** Part II. The inventory of things that now exist and work: a running
  instrumented shop, ten interactive labs, a five-tool telemetry pipeline, and
  a complete teaching package. If you only read one part, read this one.
- **Why.** Part I. The reasoning: why observability was worth teaching, why a
  *clickable* demo instead of screenshots, why this particular stack, why the
  design of each lab is the way it is for pedagogical reasons and not
  convenience.
- **How.** Parts III and IV. The implementation: how each mechanism was built,
  and the ledger of things that broke along the way — each break is a separate,
  self-contained lesson.

Two conventions carry through the whole file. First, every term that a learner
meets in the app (a metric name, a span name, a Loki label, a port, a table
column) is written exactly as it appears in the code so it can be looked up with
`grep` the moment a sentence feels too abstract. Second, claims about "it
works" are either marked with the URL and command that proves them, or taken
straight from a verified test run described in Part VI. Anything that was
designed but not exercised is explicitly labelled as such. There is a small
number of those, and they are honest.

---

# PART I — THE WHY

## 1. The question this project answers

The project answers one question, and everything else in this file is evidence
for the answer:

> Can a beginner be taught production observability — not as vocabulary, but as
> a *practice* — by clicking buttons on a real stack, in under three hours, with
> nothing but Docker on their machine?

The word "real" is doing the work in that question. It is easy to teach
observability with screenshots, with demo dashboards that have no data behind
them, or with diagrams of a pipeline that has never been switched on. All of
those fail the moment the learner sits in an actual incident, because nothing in
their memory maps a dashboard panel to the query that produced it, or a span to
the code that emitted it. The stronger claim this project makes is that the
teaching object should be *the stack itself*: a learner should generate a
failed checkout with a button press, and then see the counter rise, the alert
file, the webhook land in a database table, and the trace that carried the
exception — all within about a minute, all caused by their own click.

Everything in this repo exists to make that claim true. The application is a
Spring Boot 4 checkout service wrapped in an OpenTelemetry agent. It writes
structured logs that reach Loki, increments counters that reach Prometheus, and
emits distributed traces that a collector samples before they reach Jaeger. A
Prometheus rule watches the failure ratio, hands fired alerts to Alertmanager,
which POSTs them to the application itself, which records them in PostgreSQL.
A dozen web pages sit on top of this pipeline, each one an experiment the
learner can run and read the result of. The lesson plan, the exercises, and the
solutions file explain why each experiment was chosen and what answer to look
for.

The answer, in short, is yes. The build works, the pipeline is verified, the
labs are live, and this document records how it all came to be.

## 2. What "observability" actually means

"Observability" is one of the most abused words in software. Before the build
started it was important to pin down a working definition, because the 
definition decides the architecture. The definition used here, which comes out
of the playlist this project mirrors (*Mastering Observability Engineering Made
Easy!* by The Serious CTO), is the control-theory one smuggled into software
engineering:

> Observability is the ability to answer *unexpected* questions about a
> system's behaviour using the data the system already emits — without shipping
> new code.

The word "unexpected" is the point. Monitoring is answering the questions you
predicted — "is the checkout endpoint up?", "is the error rate above 10%?".
Observability is answering the question you did not predict — "did order 6006
fail because the payments provider declined the card, or because our fraud
check marked it suspicious, or because the database insert raced the stock
update?" — an hour after the incident started, using logs, metrics, and traces
that were always being collected.

The three pillars follow from that definition:

- **Metrics** give the answer to "how much?" and "how many?" — aggregate data
  over time, cheap to store, queried with maths. Prometheus holds counters
  like `checkout_total{outcome="success"}` and histograms like
  `checkout_duration_seconds`. Metrics tell you that *something* is wrong.
- **Logs** give the answer to "what happened?" — discrete events with a
  timestamp and a message, the richest data but the noisiest. Loki stores them
  as an event stream addressed by labels. Logs tell you *what* happened.
- **Traces** give the answer to "through which services did one request flow,
  and which hop took the time or threw the error?" — Jaeger stores them as
  span trees. Traces tell you *where* it happened.

The deep teaching point, which Lesson 5 of the course makes concrete, is that
none of the three is sufficient alone. A counter says "failed checkouts are
rising" but not why. A log line says "card_declined" but not which component
declined it or how long the hop took. A trace shows the `payment.gateway.charge`
span failed but the reason lives in that span's log event. The "glue" that
connects all three is the correlation identifier: every request carries a
`trace_id` from the moment it enters the system, and because the same field is
written into the log line, the metric label, and the span, a learner can start
with a customer complaint, get the order id, look up the trace, open the failing
span, jump to its logs, and read the exception message. That single crossing from
one pillar to another is the entire reward of the course.

## 3. Why a shop, why the playlist, why hands-on

Three choices shaped the build and all three were deliberate.

**Why "the shop"?** A checkout flow is the smallest scenario that has every
property an observability teacher needs. It is a business transaction with a
customer, a product, a payment, stock, and a database — so failures have
*meaning* (a shop that cannot charge cards is a shop losing money, not just a
service returning 500). It spans multiple components — an HTTP endpoint, a
payment "gateway", two database operations — so a distributed trace has
something to join across. It has a natural error taxonomy — `card_declined`,
`insufficient_funds`, `timeout`, `fraud_check` — which becomes the incident
drill's answer key. And it needs exactly three database operations
(`db.order.insert`, `db.stock.update`, and lookups against `customers` and
`products`) that make a waterfall readable. A payment is also *relatable*: every
learner has bought something online, so the sentence "our checkout is failing"
carries immediate stakes.

**Why the playlist?** The source material is a structured video course, not an
article. The repo therefore inherits its syllabus: the three pillars, then the
cross-cutting concerns (correlation, sampling, alerting), then hygiene and
on-call practice. Reusing a real creator's syllabus keeps the coverage honest —
the project is not a random collection of pretty features, it is the video
series made executable. The straightforward reason for doing it this way is
that the lesson-planning framework used in this workspace (documented in the
root `OLD.txt`) is a *system*: it takes learning-goal, sequence, and assessment
inputs and produces a matched set of activities. A video course is the most
well-sequenced form of lecture material there is, so it is the ideal input to a
system whose job is to turn lecture into practice.

**Why hands-on?** Because observability is a *procedure*, not a fact. Naming
the three pillars is trivia; the skill is the ritual: generate traffic, watch
a metric, open a trace, read the span logs, form a hypothesis, confirm it.
Procedures cannot be learned from reading, they must be performed, and they are
best performed on a system where the outcome is visible. Every lab in this repo
follows the same ritual ("crank, look, hypothesise, explain") so that by the
time the learner reaches the incident drill, the ritual is muscle memory and the
drill is merely time-limited application. If a lesson plan's activity cannot be
clicked and cannot produce evidence, it is not part of this course.

## 4. The pedagogy: a lesson plan is a system, not a prompt

The workspace's lesson-authoring philosophy is captured in one sentence that
appears in the root `OLD.txt`: *a lesson plan is a system, not a prompt.* The
image that accompanies it lists nine design inputs — learning goal, lesson
sequence, assessment evidence, learner profile, prior knowledge, learning
activities, output requirements, accessibility, and teacher decisions. The
claim is that if you feed all nine into the design of a course, the course
stops being a single ad-hoc worksheet and becomes a coherent system where every
part enforces the others: the assessment tests exactly the learning goal, the
activities produce exactly the assessment evidence, the sequence builds on
prior knowledge, and the accessibility supports cover every learner in the
profile.

This course applies that framework deliberately, and the application is
documented in full in `lessons/lesson-plan.md`. A condensed version of the nine
inputs as they were actually applied:

- **Learning goal.** By the end, a learner can walk a failing order from a
  dashboard number to a logged exception message and state the root cause; can
  read and write RED queries; can explain head versus tail sampling using real
  decision rows; and can run an on-call drill by reading error codes and
  justifying a verdict.
- **Lesson sequence.** Ten lessons in a strict build order: keystone (all three
  pillars in one), then logs, metrics, traces, then correlation (which fuses
  logs and traces), then the independent concerns — sampling, alerts, hygiene —
  and finally the incident drill as the capstone, with scale as the "what
  happens in prod" epilogue.
- **Assessment evidence.** Every lab closes with a self-checkable checkpoint: a
  count from a database table, a firing alert, a Loki query that returns the
  expected line. Assessment is not a separate exam; it is the evidence the
  learner already produced by using the tool.
- **Learner profile.** Developers and SREs-in-training who are comfortable with
  terminals and browsers but have never connected logs, metrics, and traces on
  one request. Fast experimenters; confident when they watch numbers move in
  response to their own actions.
- **Prior knowledge.** HTTP and status codes, SQL `SELECT`, Docker basics.
  The labs intentionally reuse that knowledge — "querying Loki is SQL with a
  WHERE clause" — rather than introducing a new CLI on every page.
- **Learning activities.** A single recurring ritual: watch the concept card,
  crank the traffic generator, look at the live evidence pane, open the same
  data in the tool of record, hypothesise what the next click will show.
- **Output requirements.** Zero magic — every number is traceable to a query,
  rule, or table in the repo; clickable over screenshotty; scoped depth; active
  voice and consistent naming.
- **Accessibility and supports.** One-click buttons, plain-text evidence
  panels, no colour-dependent semantics, keyboard-operable forms, and a
  deliberately slow beat (the sampling wait) that doubles as a teaching moment
  rather than a silent failure.
- **Teacher decisions.** Prometheus is the reference reader for metrics (not
  Grafana), the tool of record is read directly before any composition layer;
  lessons 6–8 are swappable; the drill's grade is "justified verdict", not
  "right component".

The three lesson documents (`lesson-plan.md`, `exercises.md`, `solutions.md`)
are the frozen application of this system, and every answer in the solutions
file was verified against the running stack before release — the same standard
the rest of this build holds.

## 5. Goals and success criteria

The project was judged against five concrete success criteria, all of which
were turned into pass/fail tests:

1. **One-command onboarding.** `docker compose up -d` must bring up the whole
   stack — database, app, collector, Jaeger, Prometheus, Alertmanager, Loki,
   promtail, Grafana — and the home page must answer HTTP 200 on
   `localhost:8083` within about a minute. Passed; verified by repeated
   cold-start runs.
2. **End-to-end instrumentation truth.** A single `POST /keystone/run` must
   produce: one structured JSON log line in Loki containing the request's
   `trace_id`; an increment of `checkout_total` visible in Prometheus; and a
   distributed trace visible in Jaeger consisting of the root span plus
   `payment.gateway.charge`, `db.order.insert`, and `db.stock.update`. All
   three must be demonstrably the *same request*. Passed; the verification is
   in Part VI.
3. **Closed alerting loop.** Controlled failure traffic must drive the
   `FailedCheckoutRateHigh` rule past a `for: 30s` window, fire it in
   Alertmanager, POST it to the app's webhook, and insert a row into
   `alert_events`. Passed; verified end-to-end, including a fix for the
   webhook that initially returned 500.
4. **Teaching surface.** All ten lab pages plus every fragment they call must
   return 200 and render real (not sample) data. Passed; the page inventory and
   status codes are listed in Part VI.
5. **Lesson deliverables.** `lesson-plan.md`, `exercises.md`, `solutions.md`,
   `README.md`, and this education log must exist and be grounded in the code.
   Passed; all deliverables are in the repo at the time of writing.

Those five criteria are the contract between this build and the reader. The
rest of this document is the record of how the contract was met.

---

# PART II — WHAT WAS ACHIEVED

## 6. The stack at a glance

The whole system runs as nine containers managed by one Docker Compose file.
Every service has a name, a container name, a published port, and a specific
job. This table is the anchor for every later section:

| Service | Container | Job | Host port |
|---------|-----------|-----|-----------|
| `app` | `obs-app` | Spring Boot 4 shop, OTel agent, web UI | **8083** (→8080) |
| `db` | `obs-db` | PostgreSQL 16, seeded schema | 5434 |
| `otel-collector` | `obs-otel` | OTLP receiver, tail sampling, forwarder | 4317, 4318 |
| `jaeger` | `obs-jaeger` | Trace storage + UI | 16686 |
| `prometheus` | `obs-prometheus` | Scraping, rule evaluation, query API | 9090 |
| `alertmanager` | `obs-alertmanager` | Alert routing + webhook delivery | 9093 |
| `loki` | `obs-loki` | Log storage + LogQL | 3100 |
| `promtail` | (picks it up) | Log shipping into Loki | — |
| `grafana` | `obs-grafana` | Dashboards over all of the above | 3000 |

Three design decisions are already visible in this table and recur throughout
the file.

**Ports were chosen to survive a busy laptop.** The default ports for
Postgres and a Java app (5432 and 8080) are exactly the ones a developer's
other projects grab. The compose file publishes the app on 8083 and the
database on 5434 specifically to avoid the conflicts this workshop otherwise
suffers on its own machines; the in-network service names (`app:8080`,
`db:5432`) are unaffected, which the smoke tests rely on.

**The collector owns the network edge.** Prometheus and Jaml-less traces do
not go straight from app to Jaeger; they go through `otel-collector`, which
receives on two ports (4317 gRPC and 4318 HTTP) because the OpenTelemetry SDK
and agent default to HTTP and upstream systems frequently prefer gRPC. The
collector is the choke point where tail sampling happens, and the whole Lesson 6
(lab URL `/sampling`) is a story about what happens at that choke point.

**The app talks to the tools it teaches.** The shop is not only *instrumented
by* the pipeline, it *reads from* the pipeline: the lab pages query Prometheus's
`/api/v1/query`, Jaeger's `/api/traces/{id}`, and Loki's
`/loki/api/v1/query_range` over HTTP, parse the JSON, and render the results.
That means every page is a live demonstration of the query language of the tool
it covers — the metrics page literally shows the `rate()` and
`histogram_quantile()` expressions the learner is being taught, because it runs
them. This "the UI is a client of the tooling" decision is the single most
consequential architecture choice of the project.

## 7. The ten labs

The card grid on the home page is the syllabus. Each card is a route on the
shop's web server, and each route corresponds exactly to one lesson. What
follows is the inventory of the labs as they exist today, with the route, the
mechanism, and the one teaching point each is built to make.

**`/keystone` — the three pillars in one action.** The route runs a single
checkout through `ShopService` and renders three panes: the structured log
line, the `checkout_total` counter snapshot, and the trace link. It is called
"keystone" because it is the stone that locks the other nine arches together:
it establishes the vocabulary (pillar, event, span, counter) that every later
lesson reuses. Modes `success`, `fail`, `slow`, and `vip` exist so the learner
can experiment with what changes in all three pillars when one input changes.

**`/logs` — structured logs as a queryable stream.** A slider drives a
LogQL query against Loki (`{service="app"} |= "level=WARN"` etc.) and the page
renders the resulting lines with their labels. The teaching point is the
label-versus-field distinction: labels are the address of the log stream
(`service`, `level`), fields are the rich JSON body (`trace_id`, `userId`,
`event`). The learner must write a query that returns only the ERROR logs for
one flow; the name of the game is that the whole page is a LogQL tutorial where
Loki itself grades the answer.

**`/metrics` — counters, histograms, p95, and the RED method.** Traffic
generation modes (`success`, `fail`, `mixed`, `slow`) pump requests, and the
stats fragment renders live Prometheus answers: rates by outcome, rates by
failure reason, request rate by status, HTTP latency percentiles, the
`ALERTS` count, and stock levels. The `slow` mode exists specifically to fill
the `checkout_duration_seconds` histogram so the p95/p50 rows stop reading
"—" and become real numbers. This lab gives the learner the three expressions
they will reuse forever: `rate(...)`, `sum by (reason)(rate(...))`, and
`histogram_quantile(0.95, sum by (le)(rate(..._bucket[1m])))`.

**`/traces` — the trace/span/service model.** A list of the collector's kept
traces, and a detail page (`/traces/{id}`) that renders the span tree with a
depth-aware waterfall, error badges, per-span services, and the span logs. The
important implementation point is that the tree is rebuilt on the server with a
DFS walk of the flat span list (see section 14), because Jaeger's raw API
returns spans flat and learners deserve a readable tree. The page also links
out to the Jaeger UI, so the learner sees the same trace in *composition tool*
terms after seeing it in *render-it-myself* terms.

**`/correlate` — the payoff.** Failed orders from the database, each with its
`trace_id`. Clicking one assembles four pieces of evidence: the trace from
Jaeger, the logs from Loki filtered by the same `trace_id`, the failure rate
from Prometheus, and a root-cause hint read from the failing span's
`exceptionMessage`. The teaching point is the asymmetry — which evidence is
only in the trace (hop order, per-hop duration), which is only in the logs
(the exception message), and how the identifier joins them.

**`/sampling` — the economics of "keep everything".** Eight worker threads run
probes; every probe is decided by the app-side `TraceDecider` (error → keep,
slow → keep, VIP → keep, else probabilistic) and the decision is written to
`trace_decisions`. The same policies run in the collector's real `tail_sampling`
pipeline, so the learner can compare the *decision* (what was kept) with the
*outcome* (what actually appears in Jaeger). This lab turns an abstract cost
discussion into a self-auditing table with reasons and counts.

**`/alerts` — from threshold to on-call record.** The page reads Prometheus's
`/api/v1/rules` and `/api/v1/alerts` and renders live rule state, then a
"stress" button generates hard failure traffic. When `FailedCheckoutRateHigh`
fires, Alertmanager POSTs it to the app's `/internal/alert` webhook, which
inserts a row into `alert_events`. The learner follows the whole loop and lands
on a record in `psql`. The page frames the `for:` window — 30 seconds for the
business rule, 2 minutes for the instance-down rule — as the anti-alert-fatigue
decision, not a configuration detail.

**`/hygiene` — redaction before the wire.** Two buttons write the same "customer
signup" log line into Loki: `safe` runs it through `SafeLog` redaction, `leak`
dumps it raw. The evidence pane fetches both back from Loki so the learner can
see, in a log store, `email=[email] password=[redacted]` next to
`email=alice@example.com password=supersecret-123`. The hard teaching point, made
explicit in the UI copy, is that a dashboard cannot un-ship a log — redaction
belongs at the call site.

**`/incident` — the on-call drill.** Generate fires twenty real checkouts
(alternating success/failure, some slow). A solve table lists the failed
orders; clicking one shows the trace and a hypothesis picker mapped to the
order's `error_code` (`card_declined`/`insufficient_funds` → gateway,
`timeout` → timeout, `fraud_check` → fraud). A correct answer flips the order
to `solved` in the database and shows the reasoning; a wrong answer tells the
learner to go read the failing span's logs again. This is the capstone lab
because it requires every earlier skill at once.

**`/scale` — what this stack becomes.** Reads Prometheus targets and renders
the scrape topology, giving the learner the vocabulary to go from five
containers on a laptop to an autoscaled production fleet: same OTLP edge, same
tail-sampling collector, same rule engine — bigger.

## 8. The full telemetry pipeline

A learner's click becomes data in exactly this order, and the build makes every
hop visible:

1. **Traffic generation.** A button (or the keystone auto-run) calls
   `ShopService.checkout(...)`. The service simulates a real payment flow:
   gateway charge, fraud check, stock update, order insert, with deterministic
   delays in every mode (`slow` sleeps hundreds of milliseconds) and a
   controlled random failure draw (`fail` forces failure, background traffic
   fails ~20% of the time, reason uniformly drawn from
   `[card_declined, insufficient_funds, timeout, fraud_check]`).
2. **Instrumentation split.** The OpenTelemetry agent, attached with a
   `-javaagent` at container start, intercepts the HTTP server, the JDBC
   calls, and the application's own `Tracer`-built spans. It generates the
   `trace_id`/`span_id` propagated through the request and exports spans over
   OTLP/HTTP to `otel-collector:4318`. The application, meanwhile, writes
   Micrometer counters/histograms that Prometheus scrapes from the internal
   actuator endpoint, and SLF4J log lines whose MDC includes `trace_id`,
   `span_id`, `userId`, `flow`, and `tenant`, which become the JSON fields.
3. **Collector decision.** The collector's `tail_sampling` batch decides each
   trace after its `decision_wait` (configured at 10 s, far below the ~30 s a
   stranger might expect while the store warms up): errors always kept, latency
   ≥ 300 ms always kept, VIP users (`user.tier=vip`) always kept, everything
   else probabilistically kept at 40%, and the rest dropped. Kept traces go to
   Jaeger.
4. **Storage and serving.** Prometheus stores series and evaluates rules
   every 15 seconds. Loki stores logs shipped by promtail, labelled with
   `service="app"` (and container labels). Jaeger stores spans. Each of the
   three tools has an HTTP API that the lab pages call directly.
5. **Alert loop.** Prometheus detects the failure ratio above 10% for 30
   seconds, marks the alert `firing`, Alertmanager groups and POSTs the
   notification to `http://app:8080/internal/alert`, and the app inserts
   `alert_events` rows with the alert's labels and status.
6. **Evidence rendering.** A page's fragment (say `/metrics/stats` or
   `/correlate/evidence`) issues its own HTTP requests back into the same
   three tools, parses the JSON into a view model, and HTMX swaps the HTML
   into the page without a reload.

The pedagogical consequence of building the pipeline this way is that there is
no single "demo mode". Every number on every page is produced by the real
machinery the course is teaching, which is exactly the property the "zero
magic" output requirement demands.

## 9. What was proven end-to-end

The build is not believed to work on the strength of compilation. The
following were observed live during the verification pass, and the commands
that produced them are in Part VI:

- All ten lab pages and every fragment they call return HTTP 200, including two
  that initially crashed and were fixed as part of the pass.
- A `checkout` trace appears in Jaeger with root
  `POST /keystone/run` and children `payment.gateway.charge`,
  `db.order.insert`, and `db.stock.update`, plus auto-instrumented
  `SELECT postgres.customers` and `SELECT postgres.products` spans.
- Loki returns the keystone log line with `trace_id`, `span_id`, `userId`,
  `flow`, and the exact event name, so the log and the trace are provably the
  same request.
- `FailedCheckoutRateHigh` transitions through pending to firing under stress
  and a row appears in `alert_events` via the Alertmanager webhook. The webhook
  itself had to be fixed during the pass (see section 23) — the fix is part of
  the proof, not an assumption.
- The `trace_decisions` table, after a 20-request fail-mix batch, contained
  error-kept, slow-kept, sampled-kept, and dropped rows in proportions matching
  the configured policy.
- The hygiene lab's Loki evidence shows the safe line redacted and the leak line
  fully readable, side by side, retrieved by the same evidence query.
- Grafana has all three datasources provisioned and both dashboards loaded.

## 10. Deliverables inventory

The repo ships five kinds of deliverable, and each kind is owned by a mechanism
that is exercised in the build:

- **A runnable application** (`src/main/java`) — ten web controllers
  (`ObsInterceptor` included), a `ShopService` domain, an `OrderLookup`,
  an `AlertStore`, instrumentation helpers (`BusinessMetrics`, `SafeLog`,
  `TraceCtx`, `TraceDecider`), three client classes (`PrometheusClient`,
  `LokiClient`, `JaegerClient`) with a shared `HttpJson` helper, and an
  `ObsProps` configuration type bound to the `OBS_*` environment variables.
- **Container configuration** (`docker/`) — per-service images with configs
  baked in (see section 22), the collector's sampling pipeline, the Prometheus
  scrape config and alert rules, the Alertmanager webhook, Grafana provisioning,
  and the database schema/seed.
- **A web UI** (`src/main/resources/templates`) — `layout.html` with head/nav/
  footer fragments, one page template per lab, one fragment per dynamic region,
  and a hand-written stylesheet.
- **The lesson system** (`lessons/`) — `lesson-plan.md`, `exercises.md`,
  `solutions.md`.
- **Public-facing documentation** — `README.md` and `linkedin-post.md`, plus
  this education log.

---

# PART III — HOW (THE IMPLEMENTATION)

## 11. The application core: ShopService

`ShopService` is the heart of the shop and the seed of every other lab, so it
repays careful reading. It is a single class that simulates a checkout enough
to be *observable* — which is a different bar from being *functional*. The real
trade with a provider, a stock reserve, and an order archive is reduced to
three vivid operations, but every one of those operations is made to do three
things the build cares about: take time, take a span, and take a log.

The checkout entry point is `checkout(customerId, productId, quantity, flow,
forceFail, slowMs)`. The `flow` argument is the discriminator that later lets a
single service serve every lab: when the keystone page runs a request the flow
is `"keystone"` and the resulting MDC field is written into the log line; when
the incident drill generates traffic the flow is `"incident"`; when sampling
probes run the flow is `"checkout"`. Because MDC fields flow into every log
line and every metric label has the traffic's origin on it, a learner can
query "what did the incident drill contribute?" — the flow discriminator is a
poor-m-"oft attributing traffic, and it is present everywhere the build needs
it.

Inside the checkout, three micro-flows are simulated with actual `Span`s:

- `payment.gateway.charge` — the most important span in the course. It carries
  the failure, and the failure is translated into one of four reasons drawn
  from `FAIL_REASONS` (`card_declined`, `insufficient_funds`, `timeout`,
  `fraud_check`). When it fails, the span is recorded with an error status and
  the exception message is attached as a span log event — that single log event
  is what the correlation lab later reads back as the "root cause" hint.
- `db.order.insert` and `db.stock.update` — the two database spans. Together
  with the also-real `SELECT postgres.customers` and
  `SELECT postgres.products` spans produced by the OTel JDBC instrumentation,
  they give the waterfall its familiar shape and make the "three hops" puzzle
  (part of the final exit check) answerable from memory alone.
- The order row is inserted with `insertOrder`, and this is one of the build's
  dark corridors (see section 24): the query uses `RETURNING id`, which the
  PostgreSQL driver surfaces as a multi-key generated-key map, so the service
  must read `keyHolder.getKeys().get("id")` rather than the happy-path
  `keyHolder.getKey()`.

Instrumentation flows through deliberately. Every checkout creates a
`Timer.Sample` via `BusinessMetrics.recordSuccess/recordFailure`, so the timer
and its buckets are correlated with the outcome on the same request. Every
failure calls `metrics.recordFailure(sample, reason)`, which increments both the
outcome counter and a per-reason counter, so the two are offset by nothing and
the R.E.D. expressions reconcile. And every operation logs at levels chosen to
give the `/logs` lab material to filter, with DEBUG-ish chatter on one path,
WARN on the gateway failure, and an INFO completion line carrying the event
name, `orderId`, `durationMs`, `flow`, and the trace/span ids that the MDC added.

There is a second entry path, `probe(endpoint, durationMs, fail, vip)`, used
by the sampling lab. Probes exist so `/sampling/generate` can manufacture
controlled durations and failure outcomes *without* incurring real database
writes — the lab is about the decision loop, not about building up the orders
table, so probes sleep the requested duration, record the outcome, and let the
`TraceDecider` decide. The orthogonal concerns (an orders table for incidents,
a decisions table for sampling) are exactly the separation a real shop ends up
with, and the build keeps them separate on purpose so each lab teaches its own
table.

## 12. Instrumentation: the OpenTelemetry agent

The single largest free win of the entire build is the OpenTelemetry Java
agent. Instead of hand-writing instrumentation for HTTP, JDBC, and the rest,
the application ships with `-javaagent:opentelemetry-javaagent.jar` and lets
the agent do the plumbing, while the application adds only the business spans
and the metadata it specifically wants teaching.

The agent's configuration is minimal on purpose, and every line of it is
commented in the compose file:

- `OTEL_SERVICE_NAME=observability-shop` — the Jaeger service selector and the
  Prometheus `application` tag's sibling; without it, traces arrive in Jaeger
  named "unknown_service".
- `OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318` — the HTTP (not
  gRPC) endpoint. This is the most instructive `env` in the whole project:
  the agent emits OTLP over HTTP by default, the collector listens on both
  4317 (gRPC) and 4318 (HTTP), and pointing the agent at 4317 produces the
  loudest "would you like to know more?" error of the build ("Connection
  reset"), which is itself a teaching moment about the double personality of
  the OTLP protocol (then the subject of a fixed one-liner and a lot of
  lesson copy).
- `OTEL_METRICS_EXPORTER=none` and `OTEL_LOGS_EXPORTER=none` — a decision about
  role separation: the OTel agent owns *traces*, while Micrometer owns metrics
  (scraped by Prometheus) and SLF4J/logback owns logs (shipped by promtail).
  Running all three exporters and both gateways is possible but would blur the
  very boundaries the course is teaching; the sentinel is that the alert rule
  reads `checkout_failure_total{application="observability-shop"}` — that
  `application` tag comes from Micrometer config, not the agent.
- `OTEL_TRACES_SAMPLER=parentbased_always_on` — head sampling left fully on so
  the *collector* (the tail) is the only place sampling decisions are made;
  nothing is dropped before the decision point, reassuringly matching the
  `TraceDecider` mirror.

The agent also contributes the spans the course treats as the telco-native
landscape: the root HTTP span `POST /keystone/run`, the JDBC spans
(`SELECT postgres.customers`, etc.), and the context propagation that threads
`trace_id`/`span_id` into the MDC (which the JSON log encoder then serialises).
The application's own `Tracer`-built spans (`payment.gateway.charge`,
`db.order.insert`, `db.stock.update`) are created with the same remote context,
so the three layers (HTTP, business, JDBC) compose into one tree without the
application ever knowing how.

## 13. The metrics pillar

**Registration.** `BusinessMetrics` holds three Micrometer meters: a timer
`checkout_duration_seconds`, a counter `checkout_total` labelled by `outcome`,
and a counter `checkout_failure_total` labelled by `reason`. Two configuration
lines in `application.yml` matter here: `management.metrics.tags.application:
observability-shop` attaches the tag every alert and every lab query filters
on, and `management.metrics.distribution.percentiles-histogram` is enabled for
`checkout_duration` so the histogram buckets exist for `histogram_quantile`.
Without the percentiles-histogram switch the p95 rows in `/metrics/stats` would
stay "—" forever; with it, the `slow` traffic mode fills real buckets and the
percentile becomes visible.

**Query surface.** `/metrics/stats` renders, for a learner, five families of
live answers straight from Prometheus `/api/v1/query`:

- rate by outcome: `sum by (outcome) (rate(checkout_total{application="observability-shop"}[1m]))`
- rate by failure reason: `sum by (reason) (rate(checkout_failure_total{...}[1m]))`
- HTTP request rate by status: `sum by (status) (rate(http_server_requests_seconds_count{...}[1m]))`
- latency percentiles:
  `histogram_quantile(0.95, sum by (le) (rate(checkout_duration_seconds_bucket{...}[1m])))`
  and the p50 twin
- alert count: `count(ALERTS{alertstate="firing"})`

and stock levels (`stock_level{product=...}`), which the alert and SLO dashboard
graph draw from the same sources. The `PrometheusClient` (`client/PrometheusClient.java`)
wraps the query API with `instant(query)` for raw JSON and a `instantValue(query)`
helper that returns the first value as a double or `NaN` when empty — the
"nan-safe" path means a page renders a "—" rather than crashing the moment
Prometheus has no data for a series yet. That non-crashing behaviour is a real
product decision for a teaching tool: the empty state is the prompt to generate
traffic, not an error state.

**The R.E.D. reading.** The three queries above are the R.E.D. method made
concrete on one screen: Rate is `rate(checkout_total[1m])`, Errors is the
failure-rate-by-reason query, Duration is the histogram percentile. The final
exit check in `exercises.md` reverses the direction — given the metric name,
write the query — so the learner is drilled until the mapping is reflex. The
alert rules (`docker/prometheus/alert-rules.yml`) reuse exactly these series,
with a ratio expression:

```
(sum(rate(checkout_failure_total{application="observability-shop"}[2m])) /
 clamp_min(sum(rate(checkout_total{...}[2m])), 1)) > 0.10
```

The `clamp_min(..., 1)` protects the ratio from dividing by zero when traffic
dips — a dirty-real-production detail that the `/alerts` page surfaces in its
copy as the difference between a *business* alert and an *infrastructure*
alert.

## 14. The tracing pillar

**Fetching.** Traces come from Jaeger's `GET /api/traces/{traceId}` API, wrapped
in `JaegerClient`. `recent(limit)` hits the service-based list endpoint, and
`byId(traceId)` fetches one trace. This direct-to-Jaeger read matters
pedagogically: the app demonstrates the API, and then links out to
`http://localhost:16686/trace/{traceId}` so the learner sees the same data
through Jaeger's own UI. There is no middleware pretending to be a trace store.

**Rendering the tree.** The detail page does not just paste Jaeger's flat span
array into a table. `JaegerClient.dfs` performs a depth-first walk of the
span list into a display order, annotating each span with a depth value; the
template then draws a waterfall whose bar width is proportional to the span's
microsecond duration relative to the trace, with a left margin proportional to
depth. This is the most rewarding twenty lines of the whole UI code: it turns
an array of possibly-out-of-order spans into the picture every article about
Jaeger uses, computed inside the app the learner is looking at.

**Span logs and the exception.** The failing span carries, in its `logs/events`
section, the `exception.message` that the correlation lab reads back. The span
model deliberately exposes `exceptionMessage()` as a first-class accessor so
the "root cause" hint in `/correlate/evidence` is one method call, not a
sleuthing exercise — and the learner is expected to check it against the trace
themselves before trusting it.

**Honest about the delay.** Because the collector *tail*-samples, a just-posted
trace is not visible until the decision window passes. The UI says so, with a
copy line on the keystone pane ("retry after a few seconds, or open the trace
in Jaeger"). This is a deliberate honesty: the wait is the mechanism the
sampling lesson teaches, and the build would be betraying its own thesis if it
hid or faked it.

## 15. The logging pillar

**Producing.** The application logs with SLF4J/logback. Three layers conspire
so that a line like the keystone completion event arrives in Loki complete and
addressable:

1. `ObsInterceptor` is a Spring handler interceptor that pushes request
   context into the MDC *before* the controller runs — `tenant=shop`, and
   `userId`/`customerId` read from the request parameters, falling back to n/a
   for unprompted traffic. Because the interceptor runs for every handler, no
   controller has to remember to set the context.
2. The OpenTelemetry agent maintains its own async context, and when the Java
   agent is present its bridge injects `trace_id` and `span_id` into the same
   SLF4J MDC (that is the "MDC propagation" mechanism), so the correlation
   fields are available to every logger on the thread.
3. `logback-spring.xml` configures a `LogstashEncoder` whose custom
   `customFields` mark the line with service identity. The encoder emits the
   whole MDC as JSON fields, so `trace_id`, `span_id`, `userId`, `flow`, and
   `tenant` ride every line automatically.

A custom masking provider was contemplated for this file and deliberately
rejected after a working spike proved it was a dead end (the logstash-logback
encoder 8.1 API appends a *duplicate* `message` field instead of replacing the
default provider — a rabbit hole whose reward was a cleaner decision to redact
at the call site). The place where masking is allowed to live, and the place
the hygiene lab teaches, is `SafeLog`, applied where a sensitive value first
enters a log message. This is section 19's subject; its relevance here is that
`logback-spring.xml` has *no* masking at all, which is exactly what makes the
Loki "leak" line so convincingly leaky.

**Shipping.** `promtail` scrapes the app container's stdout/docker json
stream, attaches a label set (`service="app"` plus the container label), and
pushes lines to Loki. The label decision shows up everywhere in the lab copy:
the LogQL selector is `{service="app"}` and the hygiene evidence queries are
`{service="app"} |= "LEAK-DEMO"` and `{service="app"} |= "email=[email]"`.
Promtail's label is the "address" of a log stream — the teaching distinction
the `/logs` lab drives home with a table of labels versus JSON fields.

**Reading.** `LokiClient.query(logql, limit)` hits
`/loki/api/v1/query_range`, parses the streams into `LogLine` records
(timestamp, text, stream labels), and the lab templates render them with the
labels beside the text. When Loki is unreachable the client returns a
structured error into the view model rather than throwing, preserving the
"generate traffic first" lesson.

## 16. Correlation

Correlation is the product sold by the first five lessons, and `/correlate` is
where it is manufactured. The route's flow:

1. **Find the failed orders.** `OrderLookup.recentFailures(limit)` reads the
   `orders` table for status `failed`, with `error_code` and `trace_id`
   populated. It deliberately filters out the `legacy-%` trace-id rows that the
   seed data uses for *traces that never existed* — those rows are there to
   teach that not every failure row corresponds to a trace you can open, and
   that "missing trace" is itself a diagnostic signal. Real generated
   checkouts get real random hex trace ids, so they pass the filter and resolve.
2. **Assemble evidence.** Clicking a row calls `/correlate/evidence` with the
   order id. The controller looks up the order, issues three async-friendly HTTP
   calls (trace from Jaeger by `trace_id`, logs from Loki filtered by the same
   id, failure-rate instant query from Prometheus), and derives the root-cause
   hint from the failing span's exception message.
3. **Render.** The template shows four quadrants: the trace snippet with a link
   to Jaeger, the log lines that carry the same `trace_id`, the numerical
   failure rate, and the root-cause sentence. The pedagogical payoff is the
   *crossing*: the learner sees that "the trace shows the hop that failed" and
   "the logs show the message the hop wrote" are two facts that needed each
   other, joined by nothing more exotic than a field named `trace_id` present
   in both stores.

This lab is the reason `OrderLookup` exists as a domain service rather than a
scattered query: the "recent failures" concept (with the legacy-id exclusion)
is used by both `/correlate` and `/incident`, and keeping the isolation rule in
one place is the difference between a teaching tool that works and one whose
pages disagree about what a failure is.

## 17. Sampling

Sampling is the lab most likely to be *skipped* and most worth *doing*, because
it is where observability meets the economics. Two machines in the build make
the same decision, and the lab's whole trick is that the learner can compare
them.

**The real machinery — the collector.** `otel-collector-config.yml` declares a
`tail_sampling` processor with four policies, read in order:

1. `failures-kept` — `status_code` policy, keep every span set whose status is
   ERROR.
2. `slow-kept` — `latency` policy, keep traces whose duration crosses 300 ms.
3. `vip-kept` — `string_attribute` policy, keep traces whose `user.tier`
   attribute equals `vip`.
4. `background-sampled` — `probabilistic` policy, keep 40% of whatever remains.

Everything that survives all four is exported to Jaeger (via the
`otlp/jaeger` exporter over gRPC to Jaeger's own collector); everything that
does not is dropped with confidence. `decision_wait: 10s`
means a trace is buffered for ten seconds past its last span before the
decision — the visible delay that keystone and traces pages warn about, and a
real production value, not a demo stagger.

**The mirror — `TraceDecider`.** The app-side class encodes the identical
priority list (error → slow → vip → probabilistic) and returns a
`Decision(kept, reason)` record. The `/sampling/generate` route runs 1..100
probes across an 8-thread pool, and for each probe the `recordDecision` SQL
inserts a row into `trace_decisions` with `trace_id`, `endpoint`, `kept`,
`reason`, and `duration_ms`. The pool matters: the trace ids are genuinely
parallel, so the decisions table has realistic interleaving instead of a
serial monotone list, and the 8 threads underline that `trace_decisions` is
about *throughput* decisions, the very thing sampling optimises.

**Reading the two together.** `/sampling/stats` aggregates the decisions table
for the last 15 minutes by reason and kept-count. Because the collector runs
the same priority list on the same traffic, the learner can, in principle,
reconcile "the decision table says this trace was dropped" with "Jaeger has no
such trace" — and the rare mismatch (a kept trace not yet visible, a dropped
trace still buffered) teaches the time constants (decision_wait vs scrape
once more). The row counts seen in a verified run — error-kept, slow-kept,
one sampled-kept, a few dropped — match the policy's shape, and are recorded
in Part VI.

## 18. The alerting loop

Alerting is the second-highest-leverage lab because it closes a loop the
learner is intuitively familiar with from incident post-mortems, but has
usually never walked *from inside*.

**Two rules, two philosophies.** `docker/prometheus/alert-rules.yml` is
deliberately split into two groups so the learner sees the "what is worth a
human's 3 a.m." contrast:

- `shop.business` / `FailedCheckoutRateHigh`: the R.E.D. ratio
  (`failure rate over the last 2m / checkout rate, floor-1, > 0.10`), a 30-second
  `for:` window, `severity=critical`, `team=checkout`, a human-readable
  description and a runbook URL pointing at the incident lab. It only fires
  when checkouts *actually* fail at a wrong rate.
- `shop.infra` / `ShopInstanceDown`: `up{job="observability-shop"} == 0` for
  2 minutes, `severity=page`, `team=platform` — the "your app is a black box"
  alert that every CPU-based toy rule wants to be, and that this rule set is
  deliberately *not*.

The comments in the file make the pedagogy explicit: "an alert should fire only
when a HUMAN has something to do". The `/alerts` page reads rule state live from
Prometheus (`/api/v1/rules` → per-rule `inactive`/`pending`/`firing`) and lists
currently-firing alerts from `/api/v1/alerts`, so the "pending → firing" dance
under the `for:` window is visible on the page.

**The hand-off to Alertmanager.** Prometheus's alertmanager group config maps
alerts (grouped by `alertname` and `team`, with a 5-second group_wait and a
10-second group_interval) to a single receiver `shop-webhook`, whose webhook
URL is `http://app:8080/internal/alert`. `send_resolved` is on, so the loop is
bidirectional in the real sense: when the shop recovers, Alertmanager POSTs a
resolved notification too, and the `alert_events` table records the life cycle.

**The receiving end.** `AlertWebhookController` is a plain
`@PostMapping("/internal/alert")` that accepts the Alertmanager payload,
iterates its `alerts[]`, and inserts one row per alert into `alert_events`
(fingerprint, alert name, status, start time, labels JSONB, annotations JSONB).
The controller's body is ten lines because the interesting work, in an
educational build, is showing that the whole production loop is *just a POST
endpoint* a learner could write. Its Javadoc says so. And the webhook binding
exposed a genuinely instructive Boot 4 landmine (Jackson 3's `tools.jackson`
`JsonNode` cannot be bound from the old `com.fasterxml.jackson` import — the
initial version returned 500 on every delivery until the import was changed),
which is fully documented in Part IV.

**The evidence table.** `AlertStore.insert` writes the row and the lab's
"fragment" shows the last few events; the verified run produced exactly the
expected `FailedCheckoutRateHigh | firing | {alertname, team=checkout,
severity=critical}` rows. The loop is thus: rule → (for:30s) → firing → HTTP
POST → table row, with every hop visible and queryable.

## 19. Log hygiene and secret redaction

The hygiene lab exists because the author believes the most common *real*
observability crime is not missing instrumentation but shipping secrets into a
system that has no deletion. The lab narrows that to a single, memorably gross
wrong: a log line that contains a customer's email and password verbatim.

`HygieneController` builds the identical raw string in both branches
(`customer signup name=Alice ... email=alice@example.com password=supersecret-123`).
In `safe` mode the string is passed through `SafeLog.redact(...)` before it
reaches the logger; in `leak` mode it is logged raw with a WARN marker
`[LEAK-DEMO]` so the evidence query has a stable anchor. The controller then
runs the two evidence queries (`|= "LEAK-DEMO"` and `|= "email=[email]"`) and
renders them together. The verified result side-by-side in Loki:

- safe: `customer signup name=Alice ... email=[email] password=[redacted]`
- leak: `customer signup name=Alice ... email=alice@example.com password=supersecret-123`

`SafeLog` is a hand-rolled, dependency-free set of regex redactions (email and
password-value patterns). It is deliberately *not* a logging library feature:
the whole point, stated in the UI copy and the class's Javadoc, is that
redaction must happen at the **call site**, before a value joins a log message,
because no dashboard, no retention policy, and no downstream processor can
un-write a line that was shipped with the secret already inside it. The lab's
moment is when a learner fetches both lines back from Loki: they cannot
imagine fixing the leak "in Grafana", because Grafana is reading the same
poisoned stream. That is the lesson, and it sticks.

## 20. The incident drill

The capstone lab simulates the weakest part of most early on-call practice:
guessing. `IncidentController` sets up a scenario where the *answer is knowable*
if the learner uses what they have learned, and unknowable if they do not.

`POST /incident/generate` runs twenty real checkouts with a deterministic
pattern (every even index forced to fail, every fifth request slowed to
450 ms), each recorded in the `orders` table with a real trace id and a real
`error_code`. That pattern matters: it guarantees a rich solve table (ten
failures, spread across `card_declined`/`insufficient_funds`/`timeout`/
`fraud_check`, some with slow-but-successful siblings) every time, so the drill
never has a "everything succeeded, nothing to solve" state.

Clicking a failed order's **solve** opens a page with three things: the order
facts (`productId`, `customerId`, `error_code`, `trace_id`); the live trace
fetched from Jaeger for that trace id; and a hypothesis picker whose three
options map onto the taxonomy embedded in the controller (`gateway`,
`timeout`, `fraud`, plus "something else").

`POST /incident/answer` grades the pick against the order's error code:

- `card_declined` or `insufficient_funds` → correct answer is **gateway**
  (both are a payment rejection; the distinction between decline and
  insufficient-funds is a *customer* story, not a *tech* story);
- `timeout` → **timeout**;
- `fraud_check` → **fraud** (a business decision, not a gateway rejection);
- anything else → wrong.

A correct answer sets the order's status to `solved` in PostgreSQL (evidence the
learner can re-verify with psql), stores the hypothesis for the outcome card,
and happily explains the chain of reasoning. A wrong answer explains why the
pick does not fit the code and points back at the failing span's exception
message — explicitly telling the learner they may **re-run generate** for more
evidence, because in real on-call you *collect more data*, you do not restart
the system and hope. That single line — "you may generate more traffic, do not
restart-and-pray" — converts a game into a professional habit.

## 21. Templates and HTMX

The UI is server-rendered Thymeleaf driven by HTMX partial swaps. The decision
was architectural as much as aesthetic: every lab button is a real HTTP form
POST, every evidence pane is a real fragment, and the "page" the learner sees
is the same HTML the server would render with a full reload. There is no
JavaScript framework and no API-as-a-service layer pretending to be the thing
being taught.

`layout.html` composes `head`, `nav`, and `footer` fragments so every page
shares the same cost and the same look. Each lab has a page template and each
dynamic region has a *fragment* template under `partials/` — for example
`partials/metrics.html :: stats`, `partials/sampling.html :: stats`,
`partials/correlate.html :: evidence`, `partials/alerts.html :: overview`,
`partials/hygiene.html :: evidence`, `partials/incident.html :: generated` and
`:: solve`, and `partials/keystone.html :: result`. Fragment endpoints are
HTMX targets: `hx-post`, `hx-get`, `hx-swap`, with `hx-trigger="load"` on the
keystone run (the lab auto-fires one request so the page is never empty), and
the alert overview self-refreshing every few seconds while a drill is running.

Templating every lab this way has a subtle teaching benefit the README claims
explicitly: the learner can View-Source the exact HTML their button produced,
trace it to the fragment, trace the fragment to the controller, and see there
is no magic between "I clicked" and "the query ran". Two painful Thymeleaf
incidents (the EL1007E on an iteration variable used outside its `th:each`,
and the fiction that `#lists.last(...)` exists) live in Part IV; both were
fixed by replacing clever-in-template expressions with pre-computed model
values, which is itself a lesson in where logic belongs.

## 22. Docker orchestration and the config-in-image decision

Nine services, one compose file, and one hard constraint that shapes every
Dockerfile: **macOS TCC denies container-initiated access to host-mounted
paths for daemons**, so volume-mounting Prometheus/Alertmanager/Grafana configs
from the host (the usual Docker Desktop pattern) silently fails on a Mac. The
build's answer is that every piece of configuration the services need is *baked
into its own image* at build time with a per-service Dockerfile:

- `docker/db.Dockerfile` extends `postgres:16`, copies `db/init.sql` into
  `/docker-entrypoint-initdb.d/`, and creates the app's database role —
  schema and seed are first-boot facts, not runtime mounts.
- `docker/app.Dockerfile` is a multi-stage Maven build (compile stage with a
  JDK CI image, run stage with a JRE plus the OTel agent jar) producing an
  image whose entrypoint sleeps off the agent's HTTP call until the collector
  is listening, then starts the jar. The bake step also writes the
  `logback-spring.xml` that points the encoder at the MDC.
- `docker/prometheus.Dockerfile`, `docker/alertmanager.Dockerfile`, and the
  Grafana provisioning folder copy their respective `*.yml`/`*.json` into
  canonical image paths with ownership fixed so the daemons can read them.
- `docker/otel-collector/` has its own build that layers the config at
  `/etc/otelcol-contrib/config.yaml`.

The tradeoff (a config change is an image rebuild, not an edit on the host) is
accepted and disclosed in the README, because the alternative — a compose file
that half-works on macOS — is worse for a teaching tool whose users are
overwhelmingly on laptops.

Networking decisions inside `docker-compose.yml` get the same "explain the
default" treatment. `depends_on: condition: service_healthy` gates the app
behind a real Postgres readiness check. Environment variables use the
underscore convention (`OBS_PROMETHEUS_URL`, `OBS_LOKI_URL`,
`OBS_JAEGER_URL`, `OBS_ALERTMANAGER_URL`) so they bind cleanly to `ObsProps`
and match the config-reference table in the README. And the heart of the 
observability plumbing — `OTEL_EXPORTER_OTLP_ENDPOINT` pointing at
`otel-collector:4318` with `OTEL_METRICS_EXPORTER=none` and
`OTEL_LOGS_EXPORTER=none` — is commented inline, because a future student of
this repo deserves to see the decision in the file that makes it, not in a
blog post.

## 23. Spring Boot 4 realities

Building on Spring Boot 4 (rather than the widely-documented Boot 3) was a
deliberate bet that cost real debugging and paid real lessons. Three facts
about Boot 4 were discovered the hard way and are now commented in the code:

1. **The embedded Jackson is Jackson 3** (`tools.jackson`), not the
   `com.fasterxml.jackson` the internet's copy-paste still assumes. Request
   bodies that arrive as JSON (the Alertmanager webhook) must be bound to
   `tools.jackson.databind.JsonNode`; binding the old package's `JsonNode`
   compiles fine and fails at runtime with
   `InvalidDefinitionException: Cannot construct instance of ... JsonNode`.
   This exact bug was the reason the alert webhook initially returned HTTP 500
   and everything downstream of it (the *whole* alert loop) looked broken.
2. **Actuator's WebFlux-era permissions model** is stricter: plus
   `management.endpoints.access.default: read_only` was needed so that the
   actuator surfaces are *observable by the container's healthcheck* and by
   the composite because the default posture in Boot 4 is read-only anyway.
   The `include: health,info,prometheus,metrics` legacy style still works —
   verified by fetching `:8081/actuator/prometheus` from inside the app
   container.
3. **The management port must be explicitly separated.** `management.server.port:
   8081` keeps actuator off the published 8080 so the *apps's own scrape
   endpoint is not accidentally the one the lab's HTTP-server metrics describe*
   — a subtle correctness point that matters when `http_server_requests_seconds`
   shows up in a metrics lab.

Two Java-21 compiler truths also shaped the code: casting an erased
`Double` directly to `int` is a compile error (you must call `.intValue()`),
which surfaced in the metrics stats computation; and the Postgres JDBC
`RETURNING id` returns a multi-key generated key, so `keyHolder.getKeys()
.get("id")` is the correct read, and `keyHolder.getKey()` throws. Each is
recorded in Part IV with the exact error text, because the error text is what
another learner will paste into a search engine.

---

# PART IV — WHAT BROKE AND WHAT IT TAUGHT

## 24. The failures ledger

The honestest section of this file. Building an observability course by
"showing the real pipeline" guarantees the real pipeline shows its real teeth,
and the value of this project is mostly stored in the teeth. Every failure
below cost minutes or hours, left a fix, and now doubles as a classroom
anecdote. They are listed in roughly the order they were met.

### 24.1 The OTel export endpoint: gRPC versus HTTP

**The symptom.** Traces silently failed to reach Jaeger for a long time. The
app logs showed `Connection reset` retrying against the collector, and the
`GET /traces` page rendered an empty list that was indistinguishable from
"nothing happened yet".

**The root cause.** The OpenTelemetry agent's default exporter protocol is
**HTTP/protobuf** (`http/protobuf`), not gRPC. The collector exposes both a
gRPC endpoint (4317) and an HTTP endpoint (4318). The compose file originally
sent the agent to `http://otel-collector:4317`, which is a gRPC port speaking
a wire format the agent never even tried to speak. Result: the exporter's
endpoints were incompatible and every export attempt was a connection reset.

**The fix and the lesson.** Set
`OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318`. One line. But the
lesson is worth far more than the line: OTLP has two personalities, the agent
silently defaults to one of them, and "connection reset at the exporter" must
make you suspect a *protocol* mismatch, not a network one. This is now a
commented line in the compose file and a permanent candidate for the "when the
collector won't talk to you" incident drill.

### 24.2 The sampling wait: tail-sampling *delays* everything

**The symptom.** After working otherwise-perfectly, the keystone pane and the
traces page repeatedly showed "no trace yet" for periods that felt too long,
leading to a false failure analysis ("the exporter is still broken").

**The root cause.** It was not broken. The collector's `tail_sampling`
processor buffers a trace for its `decision_wait` (10 s in the config, plus
delivery latency) before deciding anything reaches Jaeger. A freshly-posted
trace is *supposed* to be invisible for tens of seconds. The demo was working
correctly; the person reading the screen had forgotten the mechanism they
themselves had configured.

**The fix and the lesson.** Wait the decision window before asserting "missing
trace". The lesson is the one the `/sampling` lab and the keystone copy both
state: sampling turns "is there a trace yet?" into a *timing statement*, and
any dashboard composed over a sampled backend carries that latency. The
educational product decision that followed was to surface the wait in the UI
copy instead of pretending it away — "retry in a few seconds, or open Jaeger" —
because hiding the delay would be teaching the *opposite* of the truth.

### 24.3 The webhook 500: Jackson 3 vs Jackson 2 JsonNode

**The symptom.** The alert loop "almost worked": `FailedCheckoutRateHigh`
fired, Alertmanager's logs showed it retrying the webhook with `unexpected
status code 500: {"path":"/internal/alert"}`, retries exhausted, and
`alert_events` stayed empty.

**The root cause.** Spring Boot 4's *default* HTTP-message-conversion Jackson is
**Jackson 3**, whose classes live under `tools.jackson`. The webhook controller
had imported `com.fasterxml.jackson.databind.JsonNode`
(Jackson 2) because that is what the query parsers in `HttpJson` use. Boot 4
then tried to deserialize the Alertmanager POST *into* a Jackson-2 abstract
`JsonNode` — and reported
`InvalidDefinitionException: Cannot construct instance of ...
com.fasterxml.jackson.databind.JsonNode (no Creators ... abstract types either
need to be mapped to concrete types ...)`.

**The fix and the lesson.** Import `tools.jackson.databind.JsonNode` from the
request-binding controller (the Jackson-2 parsers in the HTTP client layer are
fine; the *request body binding* must speak Boot 4's dialect). The lesson is a
two-part warning that cost a full debug cycle: (a) Boot 4 is not Boot 3 — the
JSON library's package moved (Jackson 3 lives under `tools.jackson`), and
copy-pasting a `JsonNode` signature
from Stack Overflow compiles clean and dies at runtime; (b) an abstract type
with "no default constructor" is the tell — the framework cannot instantiate
the very thing you asked it to build. Uncertain import paths during Boot-major
upgrades deserve an explicit `errortype=HttpMessageConversionException` read,
not a grep of the deserializer stack.

### 24.4 Thymeleaf's invented utility: `#lists.last(...)`

**The symptom.** `GET /traces/list` returned 500 with
`SpelEvaluationException: EL1004E: Method call: Method last(java.util.ArrayList)
cannot be found on type org.thymeleaf.expression.Lists`.

**The root cause.** The template author (this writer) assumed Thymeleaf's
`Lists` utility had a `last()` like `first()`. It does not. The expression
failed to evaluate against the ArrayList in the model.

**The fix and the lesson.** Replace `#lists.last(t.spans())` with a tested
equivalent (`t.spans().get(t.spans().size()-1)` guarded for emptiness, or a
pre-computed model value). The lesson is a Thymeleaf truth worth teaching:
the template expression language is small *on purpose*, and when an expression
feels too clever it is a signal to compute the value in the controller instead
— a piece of discipline this build now practises so often (see 24.6) that it
became a design rule.

### 24.5 The missing page mapping: a 404 wearing lipstick

**The symptom.** `/sampling` returned HTTP 404 while `/sampling/stats` and
`/sampling/generate` worked perfectly — the page template existed, the
fragments worked, and there was one embarrassingly simple oversight: no
`@GetMapping("/sampling")` handler had been written.

**The root cause.** Human error during the rush to add the fragment and POST
endpoints; the controller had the two routes a tester would exercise but not
the one a human would open.

**The fix and the lesson.** Add the one-liner handler. The lesson is a small
validation habit: a page route and its fragment routes are a *contract* —
fragment-200 without page-200 is a broken contract. Every smoke test in this
build now checks page + fragments together, and the smoke-test sheet in Part VI
reflects that.

### 24.6 Iteration status outside its scope: `f_iter.last`

**The symptom.** `GET /incident` returned 500 with
`SpelEvaluationException: EL1007E: Property or field 'last' cannot be found on
null`.

**The root cause.** Thymeleaf creates an iteration-status variable (`f_iter`)
scoped to the element that carries `th:each`. The incident template referenced
`f_iter.last` in a sibling `<span>` *outside* that element, where the variable
simply does not exist — hence "on null".

**The fix and the lesson.** Pre-compute the joined id string in the controller
(`failures.stream().map(f -> "#" + f.id()).collect(joining(", "))`) and render
one string instead of a loop with separators. This is the design rule from
24.4 applied to its logical conclusion: iteration status variables are local
to the loop element, and a template that needs comma-separated values should
receive a string, not a loop. (The networked author of this build lost ten
minutes to a variable that was never in scope and will not repeat it.)

### 24.7 Postgres `RETURNING` and `keyHolder.getKey()`

**The symptom.** The first real checkout that inserted an order crashed with
"KeyHolder … The getKey method should only be used when a single key is
returned. The current key entry contains multiple keys: [{id=6001, …}]".

**The root cause.** `INSERT ... RETURNING id` in PostgreSQL returns a *row*.
Spring's `GeneratedKeyHolder` surfaces the row as a map containing (at least)
`id` and other RETURNING columns, and `getKey()` refuses to invent a single
primitive from a multi-key map.

**The fix and the lesson.** `keyHolder.getKeys().get("id")` — explicitly read
the column you named. The lesson is a JDBC shop-floor rule: `RETURNING` gives
you a typed row, and code that reaches for a scalar `key` is assuming a driver
that you do not have. Database-backed labs that "worked" in the writer's head
specifically teach how partial a mental model is without a real driver.

### 24.8 Casting an erased Double to int

**The symptom.** A compile error in the metrics percentile juggling:
`incompatible types: possible lossy conversion from Double to int` (javac 21).

**The root cause.** The code obtained a `Double` (from a Prometheus
`histogram_quantile` value) and wrote `(int) someDouble`. In a generic context
(an erased `Double`), Java 21 requires an explicit unboxing step; the old-line
cast is rejected.

**The fix and the lesson.** `.intValue()` on the boxed value. Java's
moving-needle casting rules are a two-minute fix but a real "welcome to the
present" moment for code that grew up on earlier compilers, and the fix was
the 21st-century confirmation that *boxed wrappers and primitives are not
interchangeable in generic positions*.

### 24.9 The composition story: `<providers>` vs `<provider>` (and giving up on masking)

**The symptom.** An early attempt at log masking configured
`logstash-logback-encoder` with a `<providers>` element; logback failed to
start, then a custom `MessageJsonProvider` was written and produced log lines
with a **duplicate** `message` field.

**The root cause.** Two separate truths, both worth their own bullet: the 8.1
encoder accepts `<provider>` (singular) with a `class` attribute and rejects
the community `<providers>` element; and, more fundamentally, replacing the
encoder's default message provider is not a supported extension point in that
version — a custom provider *appends* a field rather than overriding
`message`.

**The fix and the lesson.** Deleted the whole masking experiment and moved
redaction to `SafeLog` at the call site (the outcome now documented in
section 19). The lesson is the anti-pattern's anatomy: a logging library makes
its extension surfaces *pluggable*, and the moment a log pipeline needs
"fix" logic it belongs in the application code where the value was born, not
in the encoder. This is exactly the principle the hygiene lab teaches to
learners, surfaced here as a build-time war story.

### 24.10 Configs that would not mount (macOS TCC)

**The symptom.** Docker Desktop on macOS showed containers starting but the
prometheus/alertmanager/grafana daemons either ignoring their configs or
failing to read mounted paths — a flaky, environment-specific failure.

**The root cause.** macOS's privacy framework (TCC) denies container daemons
access to host bind-mount paths; the mounts "worked" visually but the
container's daemon cannot actually open the file. A Linux-hosted or
Docker-Socket-relay setup behaves differently, so the failure was invisible on
CI-like pipelines.

**The fix and the lesson.** Bake every config into its image via per-service
Dockerfiles (section 22), accepting a rebuild-on-config-change tradeoff that is
disclosed in the README. The lesson is a portability rule born from a silent
failure: a config that must be read by a daemon should be *in the image*, and
a compose file that relies on host mounts for a daemon config is a
portability lie.

### 24.11 The empty state is the lesson, not the bug

**The symptom.** Repeatedly "the dashboards are empty" read as a failure when
the real state was "no traffic in the lookback window". `/metrics/stats`,
Grafana panels, and p95 rows all legitimately render empty or "—" until
traffic exists.

**The root cause.** Nothing. This is not a defect; it is the correct behaviour
of rate/percentile queries over silence, made confusing by a demo that advertises
"every number is live".

**The fix and the lesson.** Product, not plumbing: the metrics page renders a
"generate traffic first" hint when series are empty, and the p95/p50 rows
display "—" instead of crashing (the `NaN`-safe reads in section 13). The
lesson that a teaching tool must distinguish *no data* from *no evidence* is
one of the subtler design principles this build settled on.

### 24.12 Time constants are first-class curriculum

**The symptom.** A recurring cost, not a one-time break: every part of the
pipeline is *real*, so every part has real time constants — scrape intervals,
decision windows, pending→firing transitions, key retention — and every one of
them is a place a test can pass too early or a learner can misread.

**The root cause.** Inherent in the "clickable over screenshotty" decision.

**The fix and the lesson.** The verification sheets in Part VI fixed windows
everywhere (sleep 30 s+, re-scrape, re-check), and the lesson-plan's pacing
requirements now budget for those windows so a live demo never "loses" to a
timer. Time constants are first-class curriculum, not scaffolding to remove.

---

# PART V — THE LESSON SYSTEM

## 25. The 9-input framework applied

The workspace's lesson-authoring philosophy is the one-sentence system from the
root `OLD.txt`: *a lesson plan is a system, not a prompt*. Nine design inputs
(Learning goal, Lesson sequence, Assessment evidence, Learner profile, Prior
knowledge, Learning activities, Output requirements, Accessibility & supports,
Teacher decisions) are meant to be fed in *together* so the course is coherent
and every part enforces every other. The `lessons/lesson-plan.md` in this repo
is the frozen application of that system to the observability syllabus, and its
structure is deliberately visible: each of the nine inputs is a numbered
section with the decisions that were actually made.

The interplay is where the system earns its keep. The **learning goal**
("walk a failed order from dashboard number to logged exception and state the
root cause") determines the **assessment evidence** (the correlate lab's four
quadrants and the exit-check query-writing task). The **learner profile**
("fast experimenters, confident when numbers move under their clicks") tells
the **learning activities** to be crank-look-hypothesise rituals rather than
reading. The **prior knowledge** ("they know SQL WHERE, so teach LogQL as a
WHERE") decides the **teacher decisions** (Prometheus is the reference reader,
not Grafana — a composition layer does not teach queries). And the
**accessibility** input ("no colour-required reading", "the sampling wait is a
teaching beat") protects the **lesson sequence**'s most fragile moment from
becoming a silent failure. Each input exists in the plan because each input
had to justify a real decision, and the decisions are checkable in the running
app.

### 25.1 lesson-plan, exercises, solutions

Three files, three audiences, one standard.

**`lessons/lesson-plan.md`** is the system as reasoned above: the goal, the
ten-lesson sequence with its build-order rationale, the assessment evidence per
lesson, the learner profile, the prior-knowledge assumptions, the activity
ritual, the output constraints (zero magic, clickable over screenshotty, active
voice), the accessibility supports, and the teacher's dials (which lessons are
swappable, where depth is cuttable, how the drill is graded).

**`lessons/exercises.md`** is the learner-facing worksheet. Ten exercises plus
a timed exit check. Every exercise names the page, the button, and the
checkpoint, and the checkpoints are *producible* — they ask for the Loki query
that returns only `level=ERROR` lines for `flow=metrics`, the expression for
"failures per second by `card_declined`", the four decisions in
`trace_decisions` and what each means for Jaeger visibility, and the taxonomy
that maps `error_code` to hypothesis. The exit check is the assessment
framework's "all of this is subtractable to ten minutes" distillation.

**`lessons/solutions.md`** is the answer key and the instructor's failure-mode
guide. It contains the exact expected evidence (the verified count shapes of
`trace_decisions`, the exact safe/leak log lines, the alert/`alert_events`
loop, the incident taxonomy table), the live-demo golden path for a
one-hour instructor with no spare time, and the "this is expected, not broken"
notes (the sampling delay, the empty-state windows) that keep a demo from
collapsing into a debugging session in front of thirty people.

The unifying rule — carried from the rest of the build — is that every answer
in `solutions.md` was **verified against the running stack** during the
verification pass in Part VI, not reasoned into existence. A lessons package
whose answers have never been executed would fail the same "zero magic"
standard the labs hold themselves to.

---

# PART VI — VERIFICATION

## 26. Verification: the test plan and its results

"Works" is a claim; verification is the ledger. This section records what was
checked, in what order, and what the observed state was. Every number is from
a live run of the stack described in Part II (host `localhost`, ports as in
section 6).

**Stage A — build integrity.**

1. `mvn -q -B compile` with Java 21 (`ms-21.0.7`) — clean, no warnings that
   stop the build.
2. `docker compose build app` — the multi-stage Maven image builds and the run
   image layers without missing-jar errors.
3. `docker compose up -d` — nine services reach `healthy`/`started`; app
   healthcheck passes against its internal actuator.

**Stage B — page surface (every route above must be 200 and render real data).**

| Route | First attempt | After fixes |
|-------|---------------|-------------|
| `/` (home), `/keystone`, `/logs`, `/metrics`, `/traces` | 200 | 200 |
| `/correlate`, `/sampling`, `/alerts`, `/hygiene`, `/incident`, `/scale` | 200 | 200 |
| `/traces/list` | **500** (EL1004E, 24.4) | 200 |
| `/incident` | **500** (EL1007E, 24.6) | 200 |
| `/sampling` | **404** (24.5, missing mapping) | 200 |
| `/metrics/stats`, `/sampling/stats`, `/alerts/fragment`, `/hygiene/evidence`, `/correlate/evidence` | 200 | 200 |

The three broken routes are the *entries it was worth having a test plan to
find*: page-200 plus fragment-200 is the contract (24.5), Thymeleaf
expressions crash loudly (24.4, 24.6), and the fixed versions were witnessed
by the same sheet's re-run.

**Stage C — the single-request truth.** `POST /keystone/run` (mode
`success`), then:

- Log: Loki query `{service="app"} |= "<trace_id>"` returns exactly one line
  carrying `event=checkout.completed`, `flow=keystone`, `userId`, and the MDC's
  `trace_id`/`span_id`.
- Metric (immediately after, over a warm scrape):
  `checkout_total{outcome="success"}` incremented by 1;
  `checkout_duration_seconds_bucket` gained an entry.
- Trace after `decision_wait` (~30 s from the first keystone smoke test, ~10 s
  + pipeline from later config): Jaeger lists a trace with root
  `POST /keystone/run` and children `payment.gateway.charge`, `db.order.insert`,
  `db.stock.update`, plus `SELECT postgres.customers` and
  `SELECT postgres.products`.

The `trace_id` seen in the log line and the `trace_id` seen in Jaeger were the
same string — the correlation claim, verified directly rather than asserted.

**Stage D — the alert loop.**

1. `POST /alerts/stress` (16 forced-failure checkouts) and `POST
   /metrics/traffic` with `mode=fail&n=16`.
2. After `for: 30s`, the Prometheus API reports `FailedCheckoutRateHigh`
   `state=firing`, one alert instance, `severity=critical`.
3. Alertmanager logs show dispatch to `shop-webhook`; its initial deliveries
   returned **500** (the Jackson bug, 24.3), and after the controller fix a
   retry succeeded.
4. `SELECT ... FROM alert_events` returns one `FailedCheckoutRateHigh | firing`
   row with labels JSONB `{alertname, team=checkout, severity=critical}` — the
   loop is rule → pending → firing → HTTP POST → table row, and the table row
   makes it externally verifiable in `psql`.

**Stage E — sampling and decisions.** `POST /sampling/generate`
(`mix=fail&n=20`), then:

```
SELECT reason, kept, count(*) FROM trace_decisions GROUP BY 1,2;
 error  | t | 11
 slow   | t |  5
 dropped| f |  3
 sampled| t |  1
```

which is the policy's shape (errors kept, slow kept, remainder probabilistic)
observed as real row counts, and `/sampling/stats` renders those aggregates.

**Stage F — hygiene.** `POST /hygiene/log` in both modes, then the evidence
fragment fetches from Loki: safe line shows
`email=[email] password=[redacted]`; leak line shows
`email=alice@example.com password=supersecret-123` — the exact teaching artefact,
retrieved live.

**Stage G — Grafana.** `/api/health` ok; three datasources provisioned
(`prometheus`, `jaeger`, `loki`); both dashboards ("Observability Shop —
Overview", "SLO, Alerts & Sampling") present via `/api/search`.

**Stage H — the incident drill.** `POST /incident/generate` (20 checkouts,
even-index failures), then `GET /incident/solve?orderId=…` for a failing order
renders its facts + trace; `POST /incident/answer` with the correct hypothesis
(`error_code` ↔ verdict per the taxonomy) sets status `solved`; a deliberately
wrong pick is rejected with the "read the span logs again" hint. Both branches
verified.

**Stage I — bootstrap hygiene.** A cold `docker compose down && docker compose
up -d` completes with the same healthy state; the DB is seeded
(`orders`/`trace_decisions`/`alert_events` present, `postgres` role exists).

The failures ledger of Part IV and this ledger are the same document: the test
plan found the 500s and the 404; the fixes became claims; the claims became the
lesson package's answer key. That loop — build, exercise, break, teach — is the
method, recorded honestly.

---

# PART VII — BEYOND

## 27. Honest limitations and tradeoffs

Being candid about what this build is *not* matters as much as what it is.

1. **The shop is a simulation at the service of the pipeline, not a service
   at the service of the shop.** Request rates are deliberately low (a handful
   per click), the "payment gateway" is a fake that sleeps, and the "database"
   is one model table. Metrics and rates are therefore *shapes*, not scale; the
   lessons read the shapes correctly precisely because the author resists any
   implication that these numbers are production throughput.
2. **Time constants are the course's most fragile scenic element.** The
   enforced windows (scrape interval, `decision_wait`, `for: 30s`) are the
   *point* of the sampling and alerting lessons, but they are also the top
   reason a live demo can look broken. The lesson-plan's pacing rules budget
   for them, and the UI copy names them; neither guarantees a learner won't
   read a buffering system as a failing one.
3. **Log redaction is call-site-only by design.** `SafeLog` covers the demo's
   known secrets; real production redaction needs structured-aware masking,
   key-management-sensitive reprocessing, and audit. The lab's claim is
   deliberately scoped: redaction belongs at the call site *as a first
   principle*, not "this regex list is sufficient".
4. **Tail sampling's probabilistic tail is heuristic.** 40% of background
   traffic plus always-keep-for-error/slow/vip is a defensible teaching policy,
   not a tuned production policy; production pushes would use composite
   policies and error-count budgets. The collector config's comment says so,
   and the lab says so.
5. **The webhook store is pedagogically simple by specification.** A real
   incident-responder integration would sink alerts into a PagerDuty-style
   service with acknowledge/escalate semantics. Storing rows in Postgres is the
   right envelope for "follow the loop to the end" without sinking an hour
   into tool integrations that teach nothing about observability itself.

## 28. Where to go next

If this build is a checkpoint rather than a finish line, the natural extensions
are the ones that keep the "system, not prompt" discipline:

- **A composite-policy collector.** Extend `tail_sampling` with an `and`/`not`
  composite that keeps "errors only when the span count > 100" or "latency >
  300ms only for vip" and watch `/sampling` render the richer decision reasons
  (`reason` column learns new vocabulary).
- **A second flow.** A new business flow (refunds, returns) exercises the same
  three pillars with new spans/metric names; the whole lesson system generalises
  without new scaffolding.
- **SLO burn-rate alerting.** The `SLO, Alerts & Sampling` dashboard already
  graphs error budget; a burn-rate rule (multi-window) next to
  `FailedCheckoutRateHigh` would extend Lesson 7 with the industry's sharpest
  alert pattern.
- **Error-budget budgeting in records.** Connect `alert_events` to the SLO
  dashboard so the learner can see "the tool that fires alerts is the same
  tool that tracked the budget" — the loop, closed wider.
- **A workshop variant.** Package `lesson-plan` for a 3-hour live workshop with
  a printed golden path from `solutions.md`, since everything in it is already
  verified.

---

## Appendices

### Appendix A — Service, port, and container reference

| Context name | Container | Purpose | Host port / internal |
|--------------|-----------|---------|----------------------|
| app | `obs-app` | Spring Boot 4 + OTel agent + web UI | host **8083**; internal 8080; management 8081 |
| db | `obs-db` | PostgreSQL 16, seeded schema | host **5434**; internal 5432 |
| otel-collector | `obs-otel` | OTLP in, tail sampling, export to Jaeger | host 4317 (gRPC), 4318 (HTTP) |
| jaeger | `obs-jaeger` | Trace storage + UI | host 16686 |
| prometheus | `obs-prometheus` | Scrape + rules + query API | host 9090 |
| alertmanager | `obs-alertmanager` | Routing + webhook | host 9093 |
| loki | `obs-loki` | Log store + LogQL | host 3100 |
| promtail | — | Ship app stdout → Loki, label `service="app"` | — |
| grafana | `obs-grafana` | Dashboards (admin/admin) | host 3000 |

### Appendix B — Metric name reference

| Metric | Labels | Meaning |
|--------|--------|---------|
| `checkout_total` | `outcome`, `application` | Every finished checkout |
| `checkout_failure_total` | `reason`, `application` | Failed checkouts by cause |
| `checkout_duration_seconds` | (histogram) | Checkout latency, buckets + percentiles |
| `trace_sampling_decision_total` | `reason` | Sampling decisions by reason |
| `stock_level` | `product` | Current stock |
| `http_server_requests_seconds` | `status`, `application` | HTTP latency (agent/Micrometer) |
| `up` | `job`, `instance` | Scrape health (infra alert) |
| `ALERTS` | various | Prometheus alert state snapshot |

All the shop business metrics carry `application="observability-shop"`.

### Appendix C — Query reference (the ones the lessons teach)

- Rate: `rate(checkout_total{application="observability-shop"}[1m])`
- Errors by reason:
  `sum by (reason)(rate(checkout_failure_total{application="observability-shop"}[1m]))`
- p95:
  `histogram_quantile(0.95, sum by (le)(rate(checkout_duration_seconds_bucket{application="observability-shop"}[1m])))`
- Failure ratio (alert):
  `(sum(rate(checkout_failure_total{application="observability-shop"}[2m])) / clamp_min(sum(rate(checkout_total{...}[2m])), 1)) > 0.10`
- LogQL by level: `{service="app"} |= "level=ERROR"`
- Hygiene evidence: `{service="app"} |= "LEAK-DEMO"` and
  `{service="app"} |= "email=[email]"`

### Appendix D — Endpoint reference (the app's own surface)

| Route | Purpose |
|-------|---------|
| `GET /` | Home card grid |
| `GET /keystone` / `POST /keystone/run` | Three-pillar lab |
| `GET /logs` | LogQL lab |
| `GET /metrics` / `POST /metrics/traffic` / `GET /metrics/stats` | RED lab |
| `GET /traces` / `GET /traces/list` / `GET /traces/{id}` | Trace lab + detail |
| `GET /correlate` / `GET /correlate/evidence?orderId=` | Correlation lab |
| `GET /sampling` / `POST /sampling/generate` / `GET /sampling/stats` | Sampling lab |
| `GET /alerts` / `GET /alerts/fragment` / `POST /alerts/stress` | Alert loop lab |
| `GET /hygiene` / `POST /hygiene/log` / `GET /hygiene/evidence` | Redaction lab |
| `GET /incident` / `POST /incident/generate` / `GET /incident/solve` / `POST /incident/answer` | Incident drill |
| `GET /scale` | Scale epilogue |
| `POST /internal/alert` | Alertmanager webhook receiver |
| `GET /actuator/prometheus` | Scrape endpoint (internal 8081) |

### Appendix E — Source file map

```
docker-compose.yml                  composition, env, ports, healthchecks
docker/                             per-service images + configs (baked in)
  db.Dockerfile, app.Dockerfile     postgres seed; multi-stage maven + agent
  otel-collector/otel-collector-config.yml    tail_sampling policies
  prometheus/ (prometheus.yml, alert-rules.yml)
  alertmanager/alertmanager.yml     webhook -> app /internal/alert
  grafana/                          datasources + dashboard provisioning
db/init.sql                         schema + seed (orders, trace_decisions, alert_events)
src/main/java/com/example/observability/
  config/ObsProps.java              OBS_* env binding (+ sampler slow-ms/keep-ratio)
  domain/ShopService.java           checkout + probe, spans, insertOrder
  domain/OrderLookup.java           recentFailures/byId, legacy-id exclusion
  domain/AlertStore.java            alert_events inserts
  obs/BusinessMetrics.java          counters, timer, histogram config
  obs/SafeLog.java                  call-site redaction
  obs/TraceCtx.java                 trace context fields for templates
  obs/TraceDecider.java             app-side sampling mirror
  client/{PrometheusClient,LokiClient,JaegerClient,HttpJson}.java
  web/*Controller.java, ObsInterceptor.java   10 labs + webhook + MDC
src/main/resources/templates/       layout, pages, partials/, traces/detail.html
src/main/resources/static/css/      styles.css
src/main/resources/application.yml  ports, actuator, metrics tags, obs props
src/main/resources/logback-spring.xml          LogstashEncoder + MDC fields
lessons/                            lesson-plan.md, exercises.md, solutions.md
README.md, linkedin-post.md, education.md
```

### Appendix F — Commands quick reference

```bash
# One-shot everything
docker compose up -d && open http://localhost:8083

# Rebuild only the app after a code change
docker compose build app && docker compose up -d app

# Read the internal scrape endpoint (actuator is NOT published)
docker exec obs-app wget -qO- http://localhost:8081/actuator/prometheus

# Watch rule/alert state directly from Prometheus
curl -s 'localhost:9090/api/v1/alerts'
curl -s 'localhost:9090/api/v1/rules'

# The webhook's evidence, directly
docker exec obs-db psql -U postgres -d postgres -c \
  "SELECT alert_name, status, labels FROM alert_events ORDER BY id DESC LIMIT 5;"

# Sampling decision ledger, directly
docker exec obs-db psql -U postgres -d postgres -c \
  "SELECT reason, kept, count(*) FROM trace_decisions GROUP BY 1,2;"

# Local (non-container) compile for development
JAVA_HOME=.../ms-21.0.7/Contents/Home mvn -q -B compile

# Round-trip jaeger UI trace lookup (kept traces only, after decision_wait)
open http://localhost:16686
```

---

*End of education log. Every claim above was verified against the running
stack at the time of writing; the verification commands are in this file and
in `lessons/solutions.md`.*