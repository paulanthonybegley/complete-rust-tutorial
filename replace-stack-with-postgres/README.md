# replace-stack-with-postgres

An educational **Spring Boot 4** + **Thymeleaf** + **HTMX** app that dismantles a modern "ten
microservices" stack down to a single PostgreSQL instance — feature by feature, live against a real
database. Every page is a small interactive demo backed by real SQL.

Inspired by The Coding Gopher's video
[*"I replaced my entire stack with Postgres..."*](https://www.youtube.com/watch?v=TdondBmyNXc).

## The stack it replaces

| Service being replaced | What replaces it in Postgres | Demo page |
|---|---|---|
| MongoDB / NoSQL document stores | `JSONB` + GIN (generalized inverted index) | `/jsonb` |
| Redis / RabbitMQ / Kafka (background jobs) | a plain table + `FOR UPDATE SKIP LOCKED` | `/queue` |
| Elasticsearch / Algolia | `tsvector` / `tsquery` + `pg_trigram` | `/search` |
| Pinecone / vector databases | `pgvector` + HNSW index | `/vector` |
| GIS / geocoding services | `PostGIS` + GiST index | `/geo` |
| InfluxDB / Prometheus-style time-series | range partitioning + BRIN index | `/timeseries` |
| Snowflake / data warehouses (dashboards) | materialized views + `REFRESH ... CONCURRENTLY` | `/analytics` |
| hand-written authz middleware | Row Level Security policies in the DB | `/rls` |
| (foundation) | ACID + extensible custom types | `/acid` |
| advertisement | honest limits | `/caveats` |

## Prerequisites

- Docker + Docker Compose
- JDK 21 (Spring Boot 4 baseline)
- Maven 3.9+

## Run it

```bash
# 1. start Postgres 16 + PostGIS + pgvector + pg_trgm (seeded on first boot)
docker compose up -d --build

# 2. start the app
mvn spring-boot:run

# 3. open
open http://localhost:8080
```

The Postgres container:

- runs `db/init.sql` on first boot, which creates every extension, table, index, policy and ~5 MB of
  seed data (~380k time-series events, 22 coffee shops, documents, articles, jobs, orders, notes)
- exposes `localhost:5432` (`postgres` / `postgres` superuser)
- the app connects as **`demo_app` / `demo_app`**, a normal, non-superuser role — essential so the
  Row Level Security demo actually bites

## What each demo does (and the SQL it runs)

- **`/jsonb`** — search/nest arbitrary JSON product attributes. Try JSON mode with
  `{"specs":{"cpu":"Apple M3"}}` (`@>` GIN) or JSONPath with `$.specs.ports[*] ? (@ == "HDMI")`.
  The app runs JSONPath through the equivalent `jsonb_path_exists()` so Spring JDBC's `?`-placeholder
  parser doesn't swallow the `@?` operator. Products are inserted as one JSONB row each.
- **`/queue`** — enqueue email/report/thumbnail jobs. Three worker threads claim rows with
  `FOR UPDATE SKIP LOCKED`; payloads with `"fail": true` retry up to `max_attempts`. The table
  auto-refreshes every 3s via HTMX.
- **`/search`** — ranked `tsrank` results with `ts_headline` highlighting; tick *fuzzy* and a typo
  (`tsvetor` vs `tsvector`) still matches via pg_trigram `word_similarity`, with "did you mean"
  suggestions. When `tsvector` finds nothing the UI switches to the trigram fallback.
- **`/vector`** — documents embedded with a PL/pgSQL trigram-hash "embedding". Query by phrase and
  filter by tag/author/date **in the same SQL** (the video's "hybrid search" point), ordered by the
  `<=>` cosine operator through the HNSW index.
- **`/geo`** — 22 London coffee shops as `geography(Point, 4326)`. Radius + nearest-first via
  `ST_DWithin` and the k-NN `<- >`, plus point-in-polygon presets via `ST_Within`.
- **`/timeseries`** — 380k+ events across 6 monthly range partitions. Run a range query and read the
  `EXPLAIN (ANALYZE, BUFFERS)` output: partitions are pruned and, with `enable_seqscan=off`, you can
  watch the BRIN index skip disk blocks. "Seed more events" appends chronologically.
- **`/analytics`** — dashboard served entirely from `mv_daily_sales`. "Refresh concurrently" runs
  `REFRESH MATERIALIZED VIEW CONCURRENTLY` without locking, and "Compare" times the raw GROUP BY
  against the stored MV.
- **`/rls`** — act as alice/bob/carol; each request sets `SET LOCAL app.uid` on its transaction, and
  the `SELECT * FROM private_notes` query returns *only that user's rows* because of row-level
  policies. Use the spoof field to watch the `WITH CHECK` policy reject (not silently drop) a row you
  don't own — the INSERT errors, which also aborts the transaction, so the demo explicitly licenses
  it. `mv_daily_sales` is `ALTER ... OWNER TO demo_app` so the app can `REFRESH ... CONCURRENTLY`.
- **`/acid` & `/caveats`** — the "why it works" (ACID + extensibility) and "when it doesn't" notes.

## Project layout

```
db/init.sql            schema + extensions + seed data (docker-entrypoint-initdb.d)
docker/Dockerfile      postgres:16 base + PostGIS + pgvector (from PGDG apt)
docker-compose.yml     db service + healthcheck + named volume
src/main/java/.../web  controllers (one per feature)
src/main/java/.../queue QueueService + multi-threaded SKIP LOCKED worker
src/main/resources/templates   Thymeleaf pages
src/main/resources/templates/partials   HTMX-swapped fragments
```

## Notes & caveats

- The pgvector demo uses a toy trigram-hash embedding so it needs no ML model or external API. It is
  for teaching the *mechanics* (HNSW, cosine, hybrid filters), not real semantics.
- HTMX is loaded from the unpkg CDN — see the `<head>` fragment in `layout.html` if you want to vendor it.
- Spring Boot 4 expects Java 17+; this project is built and tested on JDK 21.

## Teaching with this repo

A full unit built on this app's real schema ships in [`lessons/`](lessons/): a unit plan written with the
"lesson plan is a system" inputs (learning goal, sequence, assessment evidence, learner profile, prior
knowledge, activities, output requirements, accessibility, teacher decisions), plus a worksheet and a
verified answer key at `lessons/exercises.md` and `lessons/solutions.md`. Run the lab, hand out the
exercises, keep the solutions for the debrief.