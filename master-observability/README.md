# master-observability — Observability Shop

A hands-on observability lab that turns the playlist
*"Mastering Observability Engineering Made Easy!"* (The Serious CTO) into
labs you click. One command starts a boutique online shop instrumented with
OpenTelemetry; every lab reads live evidence from the same stack the industry
runs: Prometheus, Jaeger, Loki, Alertmanager, and Grafana.

```
docker compose up -d        # 9 services, one command
open http://localhost:8083  # the lab home
```

Everything below was verified on a live stack (macOS, Docker Desktop).

---

## What you get

A Spring Boot 4 checkout service that emits the three pillars on every request:

- **Metrics (Prometheus):** `checkout_total{outcome}`,
  `checkout_failure_total{reason}`, `checkout_duration_seconds` (histogram),
  `trace_sampling_decision_total`, `stock_level` — the R.E.D. method on a shop.
- **Traces (Jaeger):** every checkout is a distributed trace
  (`POST /keystone/run` → `payment.gateway.charge` → `db.order.insert` →
  `db.stock.update`) sent by the OTel agent to a collector that **tail-samples**
  before export.
- **Logs (Loki):** structured JSON with `trace_id`, `span_id`, `userId`, and
  `flow` via MDC, shipped by promtail, queryable in Grafana Explore.

The web UI drives it all: buttons generate controlled traffic (success / mixed /
fail / slow) and the pages render live evidence queried from each tool, annotated
fields, references, and the exact query that produced every number.

## Labs (all under `localhost:8083`)

| Lab | Page | You learn |
|-----|------|-----------|
| Keystone | `/keystone` | One action → log + metric + trace at once |
| Logs | `/logs` | Structured fields vs labels, LogQL levels |
| Metrics | `/metrics` | Counters, histograms, p95, RED method |
| Traces | `/traces` | Trace/span/service model, Jaeger UI |
| Correlate | `/correlate` | `trace_id` joins logs & traces → root cause |
| Sampling | `/sampling` | Head vs tail sampling decisions on real traffic |
| Alerts | `/alerts` | Rule → Alertmanager → webhook → `alert_events` table |
| Hygiene | `/hygiene` | Redact secrets *before* they hit Loki (safe/leak) |
| Incident | `/incident` | On-call drill: read error code, hypothesize, solve |
| Scale | `/scale` | Prometheus targets, what this stack becomes at scale |

Course materials (9-input lesson plan, learner exercises, instructor solutions):
[`lessons/`](lessons/lesson-plan.md).

## Stack (docker-compose)

| Service | Image/config | Host port |
|---------|--------------|-----------|
| `app` | Boot 4 + OTel agent (`docker/app.Dockerfile`) | **8083** (:8080) |
| `db` | PostgreSQL 16 + seed (`db/init.sql`, `docker/db.Dockerfile`) | 5434 |
| `otel-collector` | tail_sampling policy (`docker/otel-collector/`) | 4317, 4318 |
| `jaeger` | all-in-one (traces backend) | 16686 |
| `prometheus` | scrape config + alert rules | 9090 |
| `alertmanager` | webhook receiver (`docker/alertmanager/`) | 9093 |
| `loki` + `promtail` | log aggregation; `service_name=app` label | 3100 |
| `grafana` | 3 datasources + 2 provisioned dashboards | 3000 (admin/admin) |

> Management/actuator metrics live on the app's internal `:8081` — not
> published to the host (verified via `docker exec obs-app`).

### The data flow

```
browser ──> app(:8083) ──> Postgres(orders, trace_decisions, alert_events)
   │ OTEL agent (http/protobuf)
   └──> otel-collector(:4318) ──> tail_sampling ──> Jaeger(:16686)
            │ Prometheus scrape ──> alertmanager(:9093) ──webhook──> app /internal/alert
            └─ app JSON logs ──> promtail ──> Loki(:3100) ──> Grafana Explore
```

Links to `:16686`, `:9090`, and `:3000` are baked into the lab pages.

## Quick demo (5 minutes)

1. `docker compose up -d` then `open http://localhost:8083`
2. `/keystone` — run `mixed`; watch the log line, `checkout_total` counter, then
   (after the sampler's `decision_wait` ≈ 10 s, allow ~half a minute end-to-end)
   the trace in Jaeger.
3. `/correlate` — open the evidence for a `card_declined` order: span →
   exception message → root cause.
4. `/alerts` — `stress`; `FailedCheckoutRateHigh` fires in ~30 s; find the row
   in `alert_events`.
5. `/incident` — generate, then solve an order by reading its `error_code`.

## Rebuild the app

```bash
docker compose build app && docker compose up -d app
```

## Repo map

```
docker/            per-service images + configs (baked in; see TCC note below)
  otel-collector/  tail_sampling *.yml
  prometheus/      prometheus.yml, alert-rules.yml
  alertmanager/    alertmanager.yml (webhook -> app /internal/alert)
  grafana/         datasources + dashboards provisioning
  db.Dockerfile / app.Dockerfile
db/init.sql        schema + seed (orders, trace_decisions, alert_events; legacy ids)
src/main/          Spring Boot app (web/, domain/, obs/)
lessons/           lesson-plan.md, exercises.md, solutions.md
```

**Why configs are baked into images, no bind mounts:** macOS TCC denies
container-initiated access to host-mounted paths for daemons; baking configs
keeps `docker compose up -d` clean on Intel/Apple Silicon macOS and Linux.

## Troubleshooting

- **Jaeger looks empty right after traffic:** tail sampling buffers spans for its
  `decision_wait` (config ≈ 10 s, ~half a minute end-to-end) — reload; this is
  the Lesson 6 lesson, not a bug.
- **Measures never appear in Grafana:** generate traffic, wait a scrape (`15s`)
  interval, and confirm `docker compose logs prometheus`.
- **`/incident` solve says wrong hypothesis:** reread the failing span's logs;
  the drill expects the mapping in `lessons/solutions.md`.
- **Ports already taken (8080/5432/etc.):** the compose file publishes the app
  on **8083** and the DB on **5434** to sidestep common conflicts; override in
  `docker-compose.yml` if your host has those too.

## Course materials

- `lessons/lesson-plan.md` — the 9-input lesson system behind the course
  (goal, sequence, assessment evidence, learner profile, prior knowledge,
  activities, output requirements, accessibility, teacher decisions).
- `lessons/exercises.md` — learner-facing labs with checkpoints.
- `lessons/solutions.md` — instructor answer keys + live-demo golden path.