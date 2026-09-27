# Solutions & Instructor Notes — Observability Engineering Made Easy

Instructor-facing: expected evidence, answer keys, and the "why" behind each
lab. Learners work from `exercises.md`; this file is the answer sheet + the
failure-mode guide for demoing live.

Everything below was verified against a running stack
(`docker compose up -d`, app on `:8083`).

---

## Lesson 1 — Keystone (expected evidence)

- **Log line** (one, logged by `ShopService`):
  `event=checkout.completed flow=keystone userId=… orderId=#600x … trace_id=… span_id=…`.
- **Child spans:** `payment.gateway.charge`, `db.order.insert`, `db.stock.update`
  under root `POST /keystone/run` (plus `SELECT postgres.customers`,
  `SELECT postgres.products`). Verified in Jaeger within ~half a minute
  end-to-end (config `decision_wait: 10s` + delivery).
- **Instant vs buffered pillar:** the **log** is instantly structured; the
  **trace** lags because `tail_sampling` buffers spans for its `decision_wait`
  (`decision_wait: 10s` in the collector config) before returning a decision.
  That delay *is* the lesson: sampling deepens
  in Lesson 6.
- Demonstrate-beat: run `fail` mode and watch `checkout_failure_total` gain a
  `reason=…` label while the trace marks error spans.

---

## Lesson 2 — Logs (labels vs fields + LogQL)

- **Labels** are the facet included in the stream selector (e.g. `service_name`,
  `level` — promtail scrape); **fields** ride in the JSON body (`trace_id`,
  `userId`, `flow`, `event`, `duration_ms`, `orderId`).
- MDC → JSON works because `logback-spring.xml` ships the MDC via the default
  LogstashEncoder fields (`trace_id`, `span_id` from the OTel agent's MDC
  propagation; `userId`/`flow` set explicitly in `ShopService`).
- **Answer query:** `{service_name="app"} |= "level=ERROR" |= "flow=metrics"`.
- Failure mode: if a learner's Loki Explore shows no rows, confirm the stream
  label is `service_name` (promtail `service_name` label) — not `service`.
  Steaming: `docker compose logs promtail`.

---

## Lesson 3 — Metrics (RED queries)

- R.E.D. mapping:
  - **Rate** → `rate(checkout_total[1m])`
  - **Errors** → `sum(rate(checkout_failure_total[5m]))`
  - **Duration** → `histogram_quantile(0.95, sum(rate(checkout_duration_seconds_bucket[5m])) by (le))`
- The `slow` traffic cap (15) is a UI guard so learners don't spam the latency
  histogram; explain it, don't remove it.
- p95/p50 rows read `…` until a few seconds of `slow` traffic fills buckets
  (Prometheus needs two scrapes). Re-generate and wait ~30 s.
- Checkpoint answer:
  `sum(rate(checkout_failure_total{reason="card_declined"}[5m]))` — the
  **Error** letter.
- Grafana "Observability Shop — Overview" mirrors these panels on the `prometheus`
  datasource.

---

## Lesson 4 — Traces (model + Jaeger)

- Expected trace: root `POST /keystone/run` → `payment.gateway.charge`
  (external call), `db.order.insert` (insert), `db.stock.update` (update).
- Error traces: the failing span owns `error.status`, and its **logs** hold the
  `exceptionMessage` (the correlation hook for Lesson 5).
- `trace_id` vs `order_id`: `order_id` is a business key (same across workflows);
  `trace_id` is a *request identity* spanning every downstream hop, so it
  reconstructs the full path even when several orders share components.
- Jaeger data appears only after the collector's tail-sampling decision — this
  is expected, not a bug.

---

## Lesson 5 — Correlation (root cause evidence)

- One row per real failed order (`orders.status='failed'`, `error_code` set).
  Legacy demo rows (`trace_id` `legacy-%`) are excluded on purpose.
- Evidence assembly: trace by `trace_id` from Jaeger → Loki logs filtered by the
  same `trace_id` → failure rate ≥ a few seconds of `checkout_failure_total` →
  root-cause hint read from the failing span's `exceptionMessage`.
- Expected pattern:
  - `card_declined` → logs on `payment.gateway.charge` carry a declined/insufficient message.
  - `timeout` → `payment.gateway.charge` log shows gateway timeout.
  - `fraud_check` → `fraud` span flags the order.
- Only-in-logs: the exact `exceptionMessage` and `reason`; only-in-trace: the
  hop order + per-hop durations. This asymmetry is the answer to the checkpoint.

---

## Lesson 6 — Sampling (decision table)

- Policy (collector `tail_sampling`, mirrored by `TraceDecider`):
  `always_keep` on error → `latency` keep for slow → `probabilistic` remainder →
  default drop.
- Expected `trace_decisions` after a 20-request fail mix (verified live):
  `error kept ≈ 11`, `slow kept ≈ 5`, `sampled kept ≈ 1`, `dropped ≈ 3`
  (counts vary with the random tail/prob sampling — the *shape* is the answer). Extended with n: reasons include `dropped` which never reach Jaeger.
- Checkpoint math: 1000 req/s, keep errors+slow, drop 80% of the rest → round to
  an order of magnitude (errors+slow, +sample of remainder). Tradeoff: cost/IO/latency
  of the backend vs risk of losing a rare-but-expensive trace.

---

## Lesson 7 — Alerts (loop + `for:`)

- Rule `FailedCheckoutRateHigh`: failure ratio
  `> 0.10` sustained **30 s**; `ShopInstanceDown`: `up == 0` for **2 m**.
- Expected after stress: alert state flips pending→firing after `for: 30s`;
  Alertmanager `group_by ["alertname","team"]`, `group_wait 5s`, posts to
  `http://app:8080/internal/alert` → `alert_events` row
  (labels JSONB: `alertname`, `team=checkout`, `severity=critical`).
- Verified failure mode earlier: webhook 500 / `alert_events` empty because Boot 4's
  Jackson 3 (`tools.jackson`) can't coerce into `com.fasterxml.jackson.JsonNode`;
  fixed by binding `tools.jackson.databind.JsonNode` — do not "fix" by pinning
  Jackson 2 globally.
- Checkpoint: with ratio back to zero the alert returns to `inactive` after
  ~`for` + one evaluation interval. `for:` prevents flapping from a single spike
  triggering a page.

---

## Lesson 8 — Hygiene (safe vs leak)

- **Safe** line (verified in Loki): `name=Alice Adeyemi email=[email]
  password=[redacted]`.
- **Leak** line: `email=alice@example.com password=supersecret-123`.
- Mechanism: `obs/SafeLog.java` regexes applied at the call site; deliberately
  no logback masking provider (a custom `MessageJsonProvider` duplicates the
  `message` field on logstash-logback-encoder 8.1 — read the javadoc in
  `SafeLog` / `logback-spring.xml`).
- Lesson: redaction belongs where the log is written; a dashboard cannot un-ship
  a wire-format log. Merging the **safe** call is the answer; "mask in Grafana"
  is explicitly wrong.

---

## Lesson 9 — Incident drill (taxonomy)

Hypothesis map in `IncidentController`:

| `error_code` | hypothesis | verdict |
|--------------|-----------|---------|
| `card_declined` | gateway | correct |
| `insufficient_funds` | gateway | correct (both are payment-rejections) |
| `timeout` | timeout | correct |
| `fraud_check` | fraud | correct |
| anything else | any | wrong (drill explains) |

- Correct solve flips the order to `solved`; wrong verdict keeps it failed and
  the drill points back at the span logs. Re-running generate is allowed for
  more evidence (that's good on-call behavior).
- Drill pacing: read `error_code` → open solve → pick verdict. Expect
  ~2–3 minutes first time, under 1 minute on repeat.

---

## Lesson 10 — Scale (surfaces to inspect)

- `/scale` reads Prometheus `up` targets: live count, who they belong to, which
  instance a second replica would pair with. Expect `observability-shop`,
  `otel-collector`, `postgres-exporter`-style targets (matches `scrape_configs`).
- Stack shapes that scale: an agent sending OTLP to a central collector on
  `:4318` (HTTP) — port 4317 is gRPC; the agent defaults to HTTP, and a mismatch
  shows as "Connection reset" in app logs. Configs are baked into images (macOS
  TCC forbids bind-mounted config for container-run daemons) — a deliberate
  portability tradeoff.
- Table: data source per Grafana panel — Prometheus (RED/stock), the Jaeger
  datasource (trace button), Loki via Explore (log drill-down).

---

## Final exit check answer key

1. `rate(checkout_failure_total{reason="timeout"}[5m])`.
2. `payment.gateway.charge`, `db.order.insert`, `db.stock.update`.
3. rule → Alertmanager → webhook → `alert_events` (DB).
4. `reason=dropped` = the tail sampler dropped it; it never reaches Jaeger and
   is invisible to `GET /traces` — by design, per the sampling policy.

## Live-demo golden path (if time is short)

`/keystone` (mixed) → wait 30 s → `/correlate` on a `card_declined` order →
stress `/alerts` → read `alert_events` → solve one `/incident` order.