# Analytics for Not-So-Big Data with DuckDB

An educational course that turns the NDC Oslo 2025 talk **"Analytics for
not-so-big data with DuckDB"** (by David Ostrovsky) into a runnable Spring
Boot 4 application that embeds an actual DuckDB database, plus lesson plans,
exercises and verified solutions.

> Tagline from the source framework: **"A lesson plan is a system, not a
> prompt."**

## The 6 lessons covered

| # | Lesson | Short version |
|---|--------|---------------|
| 1 | Medium data & dark data | The ~1–100 GB window where an embedded engine earns its keep; logs are the "dark data" you already have |
| 2 | Embedded & columnar — no server | DuckDB is a library + one file, not a daemon; SQLitized, vectorized, parallel |
| 3 | Vectorized `GROUP BY` at 5 ms | A 224k-row join aggregates in single-digit milliseconds; `EXPLAIN ANALYZE` shows why |
| 4 | Joins, order & time buckets | 3-table joins, `date_trunc` day/month buckets, `TOP_N` — analytical SQL patterns |
| 5 | Dark data in practice — logs | Find the "dangerous 1%" in 300k events with a one-line `GROUP BY`, and drill down |
| 6 | Beyond the file | Views vs tables, one portable file, `COPY`/`parquet`, extensions, and when *not* to use DuckDB |

## Repository layout

```
analytics-with-duckdb/
├── OLD.txt            # the lesson-planning framework the course is built on
├── Dockerfile         # DuckDB CLI v1.3.1 (arch-aware) + the seed script baked in
├── docker-compose.yml # one "duckdb" helper container + shared volume +
│                      # healthcheck (the DB itself is the file on that volume)
├── entrypoint.sh      # create the DB file + seed it once + stay alive
├── .env               # local override for the shared data directory (see below)
├── README.md          # this file
├── lesson_plans.md    # 6 lesson plans + integration project on the OLD framework
├── exercises.md       # per-lesson exercises + integration rubric
├── solutions.md       # sample answers (verified against the running app on v1.3.1)
├── education.md       # education log: how this course was built
├── analytics.http     # VS Code REST Client — every lesson as a runnable request
└── app/               # Spring Boot 4.1.1 app: JDBC + DuckDB, no ORM
    └── src/main/
        ├── java/com/example/duckdbanalytics/
        │   ├── config/DuckDbConfig.java     # one shared connection = one engine
        │   ├── config/DataInitializer.java  # runs seed.sql every start (idempotent)
        │   ├── service/AnalyticsService.java# the taught queries (plain SQL)
        │   ├── web/AnalyticsController.java # /analytics/...
        │   ├── web/DatabaseController.java  # /database/info
        │   └── model/ + exception/          # records + one error shape
        └── resources/
            ├── application.yml               # port 8084, duckdb.url property
            ├── application-test.yml          # in-memory engine for tests
            └── seed/seed.sql                 # single source of truth for the data
```

## The core idea: the database is one file

DuckDB is **embedded** — there is no server daemon and no port to connect to.
"The database" is a single portable file (`analytics.duckdb`) that both
actors open and read/write:

- the **Spring Boot app** (host side) through the JDBC driver, and
- the **DuckDB CLI container** (Docker) through `duckdb /data/analytics.duckdb`.

`docker-compose.yml` mounts a directory into the container at `/data`, and the
default mount source is `./data`:

```yaml
volumes:
  - ${DUCKDB_DATA:-./data}:/data
```

Set `DUCKDB_DATA` (in a git-ignored `.env`, or the shell) if you need the
shared directory elsewhere — e.g. macOS Docker Desktop cannot bind-mount
TCC-protected paths like `~/Documents`, so this repo's local `.env` points the
mount at `~/duckdb-analytics-data` while keeping `./data` as the default for
everyone else.

The app defaults to the same file:

```yaml
duckdb.url: ${DUCKDB_URL:"jdbc:duckdb:../data/analytics.duckdb"}
```

(`app/../data` = the repository `data/` directory, the same one the container
mounts.) See **Run the app and CLI side by side** below for the exact
commands.

## Run it

Requirements: JDK 17+ (tested on 24), Maven 3.9+, Docker. DuckDB CLI and the
JDBC driver are **both pinned to v1.3.1** — DuckDB database files are forward-
but not backward-compatible, so the versions must match.

```bash
# 1. Database (creates + seeds data/analytics.duckdb, stays alive)
cd analytics-with-duckdb
docker compose up -d --build --wait        # healthcheck = SELECT count(*) FROM orders

# 2. Tests (in-memory DuckDB — NO Docker needed)
export JAVA_HOME=$(/usr/libexec/java_home -v 24)
mvn -qf app/pom.xml test                   # 7 tests: engine info, aggregates, plan

# 3. Run the app against the shared file (the default URL is relative to app/)
cd app
mvn spring-boot:run                        # http://localhost:8084
```

Stop the helper container with `docker compose down` (the data file survives).
Remove the database entirely with `docker compose down` + `rm -rf data`.

### Run the app and CLI side by side (the lesson-2 demo)

```bash
# container seeding the shared file, in a second terminal
docker compose exec duckdb duckdb /data/analytics.duckdb -c "SELECT count(*) FROM orders"

# the app, opening the SAME file via JDBC from the host
cd app && mvn spring-boot:run               # then:
curl -s http://localhost:8084/database/info
```

If `DUCKDB_DATA` points somewhere other than `./data`, start the app with an
explicit URL (system property or `DUCKDB_URL` env var — the YAML default
resolves the placeholder from either), e.g.:

```bash
cd app && mvn spring-boot:run \
  -Dspring-boot.run.arguments="--duckdb.url=jdbc:duckdb:/Users/you/duckdb-analytics-data/analytics.duckdb"
```

### Try it yourself

```bash
# Lesson 3 — a 5 ms GROUP BY over 224k completed lines
curl -s http://localhost:8084/analytics/revenue-by-category

# Lesson 4 — the plan behind that number (HASH_JOIN, TABLE_SCAN, HASH_GROUP_BY)
curl -sG http://localhost:8084/analytics/explain --data-urlencode 'metric=revenue-by-category'

# Lesson 4 — revenue bucketed per month, then per day
curl -s 'http://localhost:8084/analytics/revenue-trend?bucket=month'
curl -s 'http://localhost:8084/analytics/revenue-trend?bucket=day'

# Lesson 5 — "dark data": find the dangerous 1% in 300k log events
curl -s http://localhost:8084/analytics/log-levels

# Lesson 6 — the engine + dataset facts under the hood
curl -s http://localhost:8084/database/info
```

Same requests, grouped per lesson and with expected responses, live in
[`analytics.http`](./analytics.http).

## What's under the hood

- **Spring Boot 4.1.1** with `spring-boot-starter-web` + `spring-boot-starter-jdbc`,
  plain `JdbcTemplate` — no ORM. The teaching point is analytical SQL.
- **One connection, one engine** (`DuckDbConfig`): in memory mode every *new*
  JDBC connection is a brand-new empty database, so the app shares a
  single `SingleConnectionDataSource` — a direct consequence of "embedded".
- **`seed/seed.sql` is the single source of truth**: baked into the CLI image,
  run by `DataInitializer` on every app start, fully idempotent, and
  **deterministic** (hashes of row index), so every run produces the same
  numbers and the exercise answers don't drift. Medium-sized data by the talk's
  definition: 30 countries, 500 customers, 10 products, 50,000 orders over the
  365 days of 2025 (≈10% cancelled), ~250k order lines, 300,000 log events.
- **Version lock**: CLI + JDBC on v1.3.1 everywhere, because the `.duckdb`
  file format only moves forward. See `Dockerfile` and `app/pom.xml`
  (`duckdb.version` property).

## Learning order

1. Read `lesson_plans.md` — the 9-input OLD framework applied per lesson.
2. Do the matching exercises in `exercises.md` (SQL, code-reading, plan-reading).
3. Check your work against `solutions.md` (verified against the running app).
4. Finish with the integration project in `exercises.md`.
5. Hands-on-first alternative: run `analytics.http` top-to-bottom once, then
   reopen it after each lesson to see the reason behind each request.