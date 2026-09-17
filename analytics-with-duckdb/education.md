# Education Log — Analytics for Not-So-Big Data with DuckDB Course

> Documentation of every step taken to build this course, written for "future
> me" (and anyone else) to learn from and reuse.

## 1. Project Overview

**Goal:** Turn the NDC Oslo 2025 talk *"Analytics for not-so-big data with
DuckDB"* (David Ostrovsky) into a structured, runnable educational resource: a
Spring Boot 4 analytics API that embeds a real DuckDB engine over a seeded
"medium data" dataset, plus lesson plans, exercises and verified solutions.

**Tools used:**
- **Web search / transcript fetch** — identify the talk, pull and analyse the
  transcript (a subagent produced section notes: medium/dark data, the
  embedded/standalone × OLTP/OLAP 2×2, TPC-H demos, CLI basics, extensions,
  comparisons vs Postgres/SQLite/Snowflake/Spark)
- **Maven 3.9 + JDK 24** (OpenJDK) — build and test the app
- **Spring Boot 4.1.1 + Spring JDBC** — application platform (plain
  `JdbcTemplate`, no ORM)
- **DuckDB v1.3.1** — the embedded engine: JDBC driver (Maven Central
  `org.duckdb:duckdb_jdbc:1.3.1.0`) *and* the CLI container (same version,
  see below)
- **Docker / docker compose** — the CLI helper container + shared volume

**Deliverables:**

| File / Folder | Description |
|---|---|
| `OLD.txt` | the 9-input lesson-planning framework (Learning Goal … Teacher Decisions) |
| `Dockerfile` | DuckDB CLI v1.3.1 (arch-aware) with the seed script baked in |
| `docker-compose.yml` | one `duckdb` helper container: shared volume, healthcheck, no ports |
| `.env` | local override for the shared data directory (macOS `~/Documents` TCC workaround) |
| `README.md` | course overview, run instructions, curl + CLI examples |
| `lesson_plans.md` | 6 lesson plans + integration project on the 9-input framework |
| `exercises.md` | per-lesson exercises + integration rubric |
| `solutions.md` | verified sample answers (live numbers from v1.3.1) |
| `education.md` | this document |
| `analytics.http` | VS Code REST Client — every lesson as a runnable request |
| `app/` | Spring Boot 4 analytics API (`/analytics/*`, `/database/info`) + tests |

## 2. The Source Material

1. **`OLD.txt`** — the lesson-planning framework: Learning Goal, Lesson
   Sequence, Assessment Evidence, Learner Profile, Prior Knowledge, Learning
   Activities, Output Requirements, Accessibility & Supports, Teacher
   Decisions. Tagline: *"A lesson plan is a system, not a prompt."*

2. **The talk** — *Analytics for not-so-big data with DuckDB*, NDC Oslo 2025.
   Main threads, mapped to lessons:
   1. *Medium data* (Gartner ~1–100 GB) and *dark data* (logs/events nobody
      reads) → Lesson 1.
   2. *Embedded vs standalone × OLTP vs OLAP*; "DuckDB is a library + a file,
      not a server" → Lesson 2.
   3. *Vectorized columnar execution*, TPC-H lineitem at ~32 ms, `EXPLAIN`
      → Lesson 3.
   4. Analytical SQL patterns: joins, `GROUP BY`, `ORDER BY`, `PIVOT`, time
      slicing → Lessons 3–4.
   5. Logs as the canonical dark data → Lesson 5.
   6. One portable file, `COPY`/Parquet, extensions (`parquet`, `httpfs`,
      `shellfs`, `postgres_scanner`, `sqlite_scanner`, MotherDuck),
      single-machine utilization, and honest comparisons → Lesson 6.

## 3. Design Decisions (and the lesson each serves)

| Lesson | Where it lives in this course |
|---|---|
| 1 | The dataset IS the talk's "medium data": `seed/seed.sql` header states 50k orders / ~250k lines / 300k events, and `/database/info` proves it. The `events` table is the "dark data we already write" object |
| 2 | `DuckDbConfig` uses a `SingleConnectionDataSource` because in-memory DuckDB gives every *new* connection a brand-new empty engine — a concrete consequence of "embedded". The container runs the CLI (no ports), not a server |
| 3 | `AnalyticsService.revenueByCategory` + the `EXPLAIN ANALYZE`-backed `/analytics/explain` endpoint: learners see `HASH_GROUP_BY`/`HASH_JOIN`/`TABLE_SCAN` and a 5 ms total on 224k completed lines |
| 4 | `topCustomers` (4-table join + `TOP_N`) and `revenueTrend` (one query, `date_trunc` day or month via a `bucket` param — 365 vs 12 rows) |
| 5 | `logLevels` — one-line `GROUP BY` over 300k events with a deterministic ~1% ERROR/WARN and ~0.1% FATAL distribution; the CLI drill-down per service |
| 6 | `v_completed_orders` view (stored query, not data), file portability (copy the `.duckdb`, open the copy), `COPY … TO 'x.parquet'` exercise, and the explicit "when NOT to use it" trade-offs |

Cross-cutting decisions:

- **Version pinning is a hard rule.** DuckDB `.duckdb` files are forward- but
  not backward-compatible. Latest CLI is v1.5.x, but the newest JDBC on Maven
  Central is `1.3.1.0` — so CLI (Dockerfile `ARG DUCKDB_VERSION=v1.3.1`) and
  JDBC (`app/pom.xml` `<duckdb.version>1.3.1.0</duckdb.version>`) are both
  pinned to v1.3.1. Learners upgrading one side and not the other will get a
  readable error, and the README says so.
- **`seed/seed.sql` is the single source of truth.** Baked into the CLI image,
  mounted nowhere; run by the app's `DataInitializer` on every start; fully
  idempotent (`IF NOT EXISTS` + row-count guards); *deterministic* (randomness
  is `MOD(abs(HASH(i)))`) so exercises have stable answers per engine version —
  solutions quote live numbers from the v1.3.1 file.
- **No ORM.** Plain `JdbcTemplate` keeps the SQL front and centre — the
  teaching point is the analytical SQL and the engine.
- **One connection is a deliberate architecture artifact,** documented in
  `DuckDbConfig`'s header comment (mirrors "it is just a file you open").

## 4. Platform Facts Resolved Along the Way

| Question | Finding |
|---|---|
| Current stable Boot? | **4.1.1**; `java.version` 17 in the pom, built on JDK **24** (default shell `java` is 27-ea — unsupported) |
| JDBC driver for DuckDB? | `org.duckdb:duckdb_jdbc:1.3.1.0` (Maven Central); the only published version path — do not mix with a newer CLI |
| YAML `jdbc:duckdb:` URL? | Unquoted it breaks SnakeYAML ("mapping values are not allowed here") — always quote in `application.yml` |
| DuckDB `range(n)`/dates? | `range` needs **INTEGER** (UBIGINT rejected); `DATE + BIGINT` rejected → `CAST(i % 365 AS INTEGER)` |
| `get_setting('threads')`? | Does NOT exist in v1.3 (`Catalog Error: Scalar Function with name get_setting does not exist`). Use the `duckdb_settings()` **view**: `SELECT CAST(value AS INTEGER) FROM duckdb_settings() WHERE name='threads'` |
| `EXPLAIN ANALYZE` output shape? | Returns `(explain_key, explain_value)` **row pairs**; the plan text is in the value column(s) — the service skips the first column (`columns > 1 ? 2 : 1`) |
| jsonPath on filtered arrays? | `$[?(@.level=='INFO')].count` + a scalar matcher throws `ClassCastException: net.minidev.json.JSONArray cannot be cast to Comparable` — tests now parse the body with `JsonPath` and assert plain `long`s |
| MockMvc on Boot 4? | `@AutoConfigureMockMvc` from `org.springframework.boot.webmvc.test.autoconfigure` + `spring-boot-starter-webmvc-test` (carried over from sibling courses) |
| Docker CLI on Apple Silicon? | Hardcoding `linux-amd64` fails in emulation (`qemu-x86_64: Could not open '/lib64/ld-linux-x86-64.so.2'` / exit 255) → Dockerfile selects `arm64` from `TARGETARCH` (BuildKit) or `uname -m` |
| Bind-mounting under `~/Documents`? | Docker Desktop on macOS cannot create mount source paths under the TCC-protected Documents folder ("operation not permitted"). Data and seed are now: data dir = `${DUCKDB_DATA:-./data}` in compose; seed baked into the image (`COPY app/src/main/resources/seed /init`) instead of a second bind |

## 5. Verification

**Unit/integration tests — in-memory DuckDB (no Docker):**
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -f analytics-with-duckdb/app/pom.xml test
```
Result: **7 tests, 0 failures**:
- `databaseInfoReportsEngineAndSeedSizes` — engine version contains "1.3",
  `threads > 0`, `orders == 50000`, `orderLines > 200000`, `events == 300000`,
  `tables` is an array.
- `revenueByCategoryCoversAllFourCategories` — 4 categories, ordered, numbers.
- `revenueTrendBucketsByMonthAndByDay` — month → 12 rows, day → 365 rows,
  invalid bucket → 400.
- `topCustomersAreRankedBySpendAndWindowedByLimit` — length == limit, spend is
  a number; `limit=0` → 400.
- `ordersSplitBetweenCompletedAndCancelled` — 2 statuses present.
- `logLevelsShowRareEventsAmidstInformationLogs` — INFO > 200k, ERROR/WARN in
  (2400, 3200) ≈1%, FATAL > 200.
- `explainReturnsPlanForKnownMetricAndRejectsUnknownOnes` — known metric
  returns a plan containing `SCAN`; unknown metric → 400.

Early failures (all fixed): `/database/info` 500 from the missing
`get_setting`; EXPLAIN assertion seeing `analyzed_plan` key rows instead of
plan text; the jsonPath `ClassCastException`.

**Live smoke test — Docker container + host app on the same file:**
1. `docker compose up -d --build` → image builds the arch-correct CLI, the
   entrypoint creates + seeds `analytics.duckdb`, healthcheck goes **healthy**
   (`SELECT count(*) FROM orders` → 50000).
2. CLI queries in the container confirmed the seed: 50,000 orders, 249,551
   lines, 300,000 events; log levels INFO 293,835 / WARN 3,090 / ERROR 2,751 /
   FATAL 324.
3. Started the app with
   `--duckdb.url=jdbc:duckdb:/Users/paulbegley/duckdb-analytics-data/analytics.duckdb`
   (the `.env` `DUCKDB_DATA` target) and verified `/database/info` reports
   **v1.3.1**, 8 threads, 12.7 GiB max memory and the same 50k/249551/300k —
   i.e. the app and the CLI container share one file.
4. Endpoints verified live: revenue-by-category (electronics
   153,862,689.09 …), revenue-trend month (12 rows, Aug highest
   14,548,731.83), top-customers (Customer 40 / Argentina / 521,950.68),
   log-levels, and `EXPLAIN` plans showing `Total Time: 0.0053s` with
   `HASH_JOIN`/`TABLE_SCAN`/`HASH_GROUP_BY`/`TOP_N` nodes.

## 6. Teaching Material

`lesson_plans.md` applies the 9 inputs from `OLD.txt` to the talk's six themes
plus a culminating integration project ("build a metric end-to-end").
`exercises.md` mixes SQL drills, plan-reading (predict-then-check), the
view-vs-table demo, and honest trade-off questions. `solutions.md` quotes
**live, verified numbers** from the v1.3.1 seed (FATAL by service: gateway 122 >
checkout 105 > cart 97; top month Aug 2025; top day 2025-07-31; weekday ranking
Wednesday > Tuesday > Friday; revenue by country: Argentina 8,888,806.48 …) —
stable per engine version thanks to the deterministic seed.

## 7. Quick Reference — Commands

```bash
# Database / CLI container (from analytics-with-duckdb/)
docker compose up -d --build --wait       # seeds data/<file>, becomes healthy
docker compose exec duckdb duckdb /data/analytics.duckdb   # interactive REPL
docker compose down                       # keeps the data file

# Tests — in-memory DuckDB, no Docker
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -f app/pom.xml test

# Run the app against the shared file (default URL = ../data/analytics.duckdb)
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -f app/pom.xml spring-boot:run

# Examples
curl -s  localhost:8084/analytics/revenue-by-category
curl -s  localhost:8084/analytics/log-levels
curl -sG localhost:8084/analytics/explain --data-urlencode 'metric=revenue-by-category'
curl -s  localhost:8084/database/info
```

## 8. Lessons Learned

1. **Pin everything to one DuckDB version, and say why.** The CLI/JDBC split
   across compatible versions injects a dead subtle bug; a commented `ARG` +
   a `duckdb.version` property plus a README note make the constraint visible.
2. **An embedded engine changes app architecture unexpectedly.** "Wait, opening
   a *new* connection gives me an *empty* database?" forced the
   `SingleConnectionDataSource` choice — a great teachable moment that fell out
   of real integration, not a diagram.
3. **Test the plan, not just the numbers.** `EXPLAIN ANALYZE` returns
   `(key, value)` rows in DuckDB — asserting on plan *text* requires reading the
   real output shape first (the "contains SCAN" fix) and clarifying which column
   is which.
4. **Deterministic seeds pay off twice.** The `MOD(abs(HASH(i)))` choices keep
   seeds fast, idempotent, and stable so exercises and solutions cite the same
   numbers — verified against the live engine rather than guessed.
5. **macOS + Docker has sharp edges for teaching repos.** `~/Documents`-TCC
   mount failures were worked around with a configurable `DUCKDB_DATA` default
   (`./data`) and baking the seed into the image — portable for every other OS,
   functional here.
6. **Reuse beats redo.** The `OLD.txt` framework, Boot 4 test conventions and
   the `x.http` (VS Code REST Client) pattern carried straight over from the
   `eighth-api-rules` and `seven-database-laws` courses.

## 9. Where to Go Next

- [ ] Add a PIVOT lesson (the talk highlights it) as an extension of Lesson 4,
      exercised via the CLI with the `pivots` sample.
- [ ] Add a Parquet day: `COPY (SELECT …) TO 'data/analytics.parquet'` +
      `read_parquet` labs and an exercise analysing event logs from Parquet.
- [ ] Add an extensions lab (e.g. `sqlite_scanner` reading an external SQLite
      DB, `httpfs` reading a public Parquet URL) for Lesson 6.
- [x] Wire the DuckDB container + app into the root `Makefile` suite (`make
      start` / `status` / `test` now cover all three courses; `ANALYTICS_URL`
      is derived from the course `.env` so the container and app share one
      file even where the default `./data` mount is redirected).
- [ ] Script the video→course pipeline (transcript → theme list → app scaffold
      → docs), now proven three times.