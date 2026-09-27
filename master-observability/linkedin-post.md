# LinkedIn post — "I turned an observability playlist into a working lab"

> Suggested hook + body for the creator's timeline feed. Drop the blockquotes
> (they are staging notes, not post text) and paste the plain paragraphs.

---

**"I didn't write about observability. I built the whole stack so you can click
through every video in the playlist."**

The Serious CTO's *"Mastering Observability Engineering Made Easy!"* explains
logs, metrics, traces, correlation, sampling, alerting and log hygiene. Watching
is one thing. I wanted something you can **press buttons on** — so I built
`master-observability`: a Spring Boot 4 checkout shop that ships the three
pillars on every request, wired to the real pipeline in one
`docker-compose.yml`.

**The punchline is Live Evidence, not screenshots.** Every lab page reads its
answers from the actual tools:

- `/keystone` — one checkout becomes one structured log line, one
  `checkout_total` counter, and a distributed trace
  (`payment.gateway.charge` → `db.order.insert` → `db.stock.update`) in Jaeger
- `/correlate` — a `card_declined` order: trace, its Loki logs, the failure
  rate, and the root cause read from the failing span's exception
- `/sampling` — head vs tail sampling decisions actually logged per trace
  (`trace_decisions`: error→keep, slow→keep, sampled→keep, drop)
- `/alerts` — `FailedCheckoutRateHigh` fires through Alertmanager into a webhook
  that lands a row in `alert_events`: rule → on-call record, live
- `/hygiene` — safe vs leak: the same signup log redacted at the **call site**
  (`email=[email] password=[redacted]`) — because no dashboard un-ships a wire log
- `/incident` — an on-call drill: read the `error_code`, pick a hypothesis,
  solve the order, justify the verdict

**The part worth reading — what actually broke.** (Everyone's favorite track.)

- The OTel agent speaks **HTTP** by default — pointing it at the collector's
  gRPC port 4317 means "Connection reset" on every span. `:4318` fixed it.
- Tail sampling buffers traces for ~30 s `decision_wait`. The first time your
  demo page shows "no trace yet" is the *lesson*, not the bug.
- Boot 4 carries Jackson 3 (`tools.jackson`) — its `JsonNode` is not
  `com.fasterxml...`. The Alertmanager webhook returned 500 until I bound the
  right type.
- Thymeleaf has **no** `#lists.last()`. It returns an EL1004, and the iterator
  status variable only exists inside the `th:each` element.
- Postgres returns the whole row from `INSERT … RETURNING` — so KeyHolder's
  `getKey()` explodes with "multiple keys"; `keyHolder.getKeys().get("id")` wins.
- macOS TCC won't let Linux containers read bind-mounted configs — so every
  config (Prometheus, Alertmanager, collector, Grafana) is baked into its image.

And because "a lesson plan is a system, not a prompt", the repo ships a full
course on that 9-input framework (`lessons/`): learning goal, sequence,
assessment evidence, learner profile, prior knowledge, activities, output
requirements, accessibility, teacher decisions — plus learner exercises and an
instructor answer key, every lab verified against the running stack.

**Try it (all local, Docker only):** `docker compose up -d` →
http://localhost:8083 — a card grid of 10 labs you can finish in ~2 hours.
Jaeger on `:16686`, Prometheus `:9090`, Grafana `:3000` (admin/admin).

#observability #opentelemetry #prometheus #jaeger #loki #devops #teaching