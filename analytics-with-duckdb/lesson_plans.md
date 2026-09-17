# Structured Lesson Plans: Analytics for Not-So-Big Data with DuckDB

> Applied from the NDC Oslo 2025 talk **"Analytics for not-so-big data with
> DuckDB"** (David Ostrovsky).
>
> Core tagline from `OLD.txt`: **"A lesson plan is a system, not a prompt."**

Every lesson below is built with the 9 design inputs extracted from
[`OLD.txt`](./OLD.txt):
**Learning Goal**, **Lesson Sequence**, **Assessment Evidence**, **Learner
Profile**, **Prior Knowledge**, **Learning Activities**, **Output
Requirements**, **Accessibility & Supports**, and **Teacher Decisions**.

The companion application lives in [`app/`](./app) — a Spring Boot 4 analytics
API backed by an embedded DuckDB engine whose code and SQL are the "worked
example" for each lesson. Every lesson lists the exact files to open, the
endpoints to hit, and the tests that prove the behaviour.

| # | Lesson | App to read / run | Proof tests |
|---|--------|-------------------|-------------|
| 1 | Medium data & dark data | `resources/seed/seed.sql`, `service/AnalyticsService.java` (`logLevels`, `databaseInfo`) | `AnalyticsApiTest#logLevelsShowRareEventsAmidstInformationLogs`, `#databaseInfoReportsEngineAndSeedSizes` |
| 2 | Embedded & columnar — no server | `config/DuckDbConfig.java`, `Dockerfile`, `docker-compose.yml`, `web/DatabaseController` | `AnalyticsApiTest#databaseInfoReportsEngineAndSeedSizes` |
| 3 | Vectorized `GROUP BY` at 5 ms | `service/AnalyticsService.java` (`revenueByCategory`, `explain`), `web/AnalyticsController` | `AnalyticsApiTest#revenueByCategoryCoversAllFourCategories`, `#explainReturnsPlanForKnownMetricAndRejectsUnknownOnes` |
| 4 | Joins, order & time buckets | `service/AnalyticsService.java` (`topCustomers`, `revenueTrend`) | `AnalyticsApiTest#topCustomersAreRankedBySpendAndWindowedByLimit`, `#revenueTrendBucketsByMonthAndByDay` |
| 5 | Dark data in practice — logs | `seed/seed.sql` (events), `service/AnalyticsService.java` (`logLevels`) | `AnalyticsApiTest#logLevelsShowRareEventsAmidstInformationLogs` |
| 6 | Beyond the file | `seed/seed.sql` (`v_completed_orders`), `service/AnalyticsService.java` (`databaseInfo`), Dockerfile/CLI | `AnalyticsApiTest#databaseInfoReportsEngineAndSeedSizes` (view in `tables`) |

---

## Lesson 1: Medium Data & Dark Data

**Learning Goal:** Learners can define *medium data* (~1–100 GB per the talk),
recognize the *dark data* they already generate (logs, events, telemetry), and
justify the moment an embedded analytical engine is the right tool instead of a
full warehouse.

**Lesson Sequence:**
1. Warm-up: ask for the smallest and largest datasets learners have touched.
   Sort them along a line: SQLite → Postgres → *this course* → Snowflake/Spark.
   Where is "not-so-big data"?
2. Read the dataset definition in `resources/seed/seed.sql` (header comment):
   "~50k orders, ~250k order lines, ~300k logs". Check the claim with
   `curl -s http://localhost:8084/database/info` — 50,000 / 249,551 / 300,000.
3. Introduce "dark data": application logs the business never looks at. Read
   the `events` table definition in `seed.sql`; then run the "find the 1%"
   query in Lesson 5.
4. Hands-on: complete Exercise 1.

**Assessment Evidence:** Given three datasets (a photo feed, a year of app
logs, a national census), the learner classifies each as not-so-big / medium /
big and says which *signal* (query latency, cost, management burden) would push
them toward a warehouse.
- Assessment answer key: `solutions.md` → Lesson 1.

**Learner Profile:** Analysts and junior-to-mid developers comfortable with
basic SQL (`SELECT` with `GROUP BY`/`JOIN`), new to columnar/analytical
engines.

**Prior Knowledge:** What a relational table is; how an application writes rows
(OLTP); SQL grouping in any dialect.

**Learning Activities:** Dataset-sorting warm-up, reading seed SQL + engine
info, "which store for which size?" card sort.

**Output Requirements:** A one-page "data sizing" note: definitions of
not-so-big / medium / big, one example each, and the two signals that change
the answer.

**Accessibility & Supports:** Provide the sizing line as a handout; allow
voice notes instead of writing; pair-work for the card sort.

**Teacher Decisions:** Keep this lesson read-only — no code yet. The payoff
moment is `/database/info` agreeing with the seed header: "the dataset is
literally the definition of the talk's target."

---

## Lesson 2: Embedded & Columnar — No Server

**Learning Goal:** Learners explain what *embedded* means for a database (a
library + one file, no daemon, no port), map the 2×2 matrix
(embedded/standalone × OLTP/OLAP), and predict the *connection* consequences in
an app that embeds the engine.

**Lesson Sequence:**
1. Ask: "how do you talk to Postgres? to DuckDB?" — one is a server you
   connect to, the other is a file you open. Draw the 2×2: SQLite (embedded
   OLTP), Postgres (standalone OLTP), DuckDB (embedded OLAP), a warehouse
   (standalone OLAP).
2. Read `config/DuckDbConfig.java` — the comment that every *new* JDBC
   connection in memory mode is a brand-new empty database, and why
   `SingleConnectionDataSource` (one shared connection = one engine) replaces a
   connection pool.
3. Read `Dockerfile` + `docker-compose.yml`: the container is a **CLI**, not a
   database server — no port mapping, a shared volume, a healthcheck that runs
   `duckdb file -c "SELECT count(*) FROM orders"`.
4. Live demo: `docker compose exec duckdb duckdb /data/analytics.duckdb -c
   "SELECT version();"` while the app in the same repo reads the same file.
5. Hands-on: complete Exercise 2.

**Assessment Evidence:** Learner draws the 2×2 with one example per quadrant,
states what happens on a *second* connection in in-memory mode, and explains
why the compose file exposes no ports.
- Assessment answer key: `solutions.md` → Lesson 2.

**Learner Profile:** Same as Lesson 1; bonus for anyone who has run Postgres in
Docker (the contrast is the teaching lever).

**Prior Knowledge:** A rough idea that "databases" and "Docker containers"
exist; SQL basics from Lesson 1.

**Learning Activities:** 2×2 matrix drawing, "connection crime scene" (why
would queries return zero rows?), container inspect.

**Output Requirements:** The filled 2×2 diagram plus a 3-line explanation of
why the app shares a single connection.

**Accessibility & Supports:** Pre-drawn 2×2 template; verbal explanation
accepted; extra time for the container demo.

**Teacher Decisions:** The class-provable moment is *running the CLI inside the
container against the same file the Spring app reads* — embedded means both see
the same bytes, no network in between. Mention SQLite+Postgres links the talk
gives: SQLite is the single-user OLTP cousin, warehouses the OLAP servers.

---

## Lesson 3: Vectorized `GROUP BY` at 5 ms

**Learning Goal:** Learners read an analytical aggregate, know what
*columnar/vectorized* execution means at a conceptual level, and can use
`EXPLAIN ANALYZE` to see the plan and the real elapsed time behind an
endpoint.

**Lesson Sequence:**
1. Show the query behind the fastest endpoint
   (`service/AnalyticsService.java`, `revenueByCategory`): one `GROUP BY`,
   two joins, an `ORDER BY`. Ask: what would this cost on a row store with
   ~250k rows on the OLTP tables the app writes?
2. Run it: `curl -s localhost:8084/analytics/revenue-by-category` → 4 rows,
   4 categories. Then run the plan:
   `curl -sG localhost:8084/analytics/explain --data-urlencode 'metric=revenue-by-category'`
   → the near-top `Total Time: 0.0053s`.
3. Read the plan together: `TABLE_SCAN` on `order_lines`/`orders`/`product`,
   two `HASH_JOIN`s, one `HASH_GROUP_BY`, `PROJECTION`, `ORDER_BY`. Point at the
   row counts per node (224,508 completed lines in) and the elapsed time.
4. Repeat for `metric=top-customers` and note the subquery + `TOP_N` node.
5. Hands-on: complete Exercise 3.

**Assessment Evidence:** Given a multi-table aggregate, the learner predicts the
plan nodes (`TABLE_SCAN`, `HASH_JOIN`, `HASH_GROUP_BY`/`ORDER_BY`) and finds
the `Total Time` line in `EXPLAIN ANALYZE` output to judge cost before
optimizing.
- Assessment answer key: `solutions.md` → Lesson 3.

**Learner Profile:** Same as Lesson 1; must be able to run the app or the CLI
(*see README*).

**Prior Knowledge:** `GROUP BY`/`SUM` from any dialect; what a join is.

**Learning Activities:** Predict-then-check plan reading, node-order card sort,
"why is it fast?" whiteboard (columnar reads the single column, vectorized
SIMD batches).

**Output Requirements:** A saved `EXPLAIN ANALYZE` snippet for a metric with a
one-paragraph reading: the nodes, the row counts, and the total time.

**Accessibility & Supports:** Annotated plan handout mapping each box to plain
language; screenshot-friendly curl commands in README; work in pairs.

**Teacher Decisions:** Resist optimizing anything — this lesson is *seeing the
engine work*. The number to foreground is 224,508 rows in a few milliseconds;
the talk's 6M-row lineitem ~32 ms demo is the same lesson one scale larger.

---

## Lesson 4: Joins, Order & Time Buckets

**Learning Goal:** Learners write and read the three helpers analytical work
lives on — multi-table joins, ordering with a limit (the "top givers" pattern),
and slicing time with `date_trunc` — and can switch a query from one bucket to
another.

**Lesson Sequence:**
1. Read `topCustomers` in `service/AnalyticsService.java`: four-table join
   (customer → orders → order_lines → country), `GROUP BY`, `ORDER BY spend
   DESC`, `LIMIT`. Run `curl -s 'localhost:8084/analytics/top-customers?limit=3'`
   and find the top spinner by spend.
2. Read `revenueTrend`: the `date_trunc('%s', o.order_date)` in the `GROUP BY
   1` — one query, two buckets. Run `?bucket=month` (12 rows) and `?bucket=day`
   (365 rows); note the row *shape* is identical, only the grain changed.
3. Inspect the plan for `metric=top-customers`: `PERFECT_HASH_GROUP_BY` for the
   per-customer counts, then `TOP_N` — the engine stops sorting everything.
4. Hands-on: complete Exercise 4.

**Assessment Evidence:** Learner writes a `date_trunc` bucket query for a new
grain (e.g. by weekday) unaided, and identifies which node does the "keep the
top 5" work in `EXPLAIN ANALYZE`.
- Assessment answer key: `solutions.md` → Lesson 4.

**Learner Profile / Prior Knowledge:** Same as Lesson 3 plus comfort with
`JOIN` syntax.

**Learning Activities:** Query-rewrite drill (change bucket, change limit),
plan-node hunt, "which table joins to which?" diagram for the top-customers
query.

**Output Requirements:** Two one-line SQL answers — a new bucket of the trend
query and a "top N" join of the learner's choosing — each run against the CLI
or the app.

**Accessibility & Supports:** Query templates pre-filled; CLI commands provided
in README; extra time on the rewrite drill.

**Teacher Decisions:** The contrast to draw is OLTP access patterns (many small
lookups by key) vs this shape (a whole column, grouped). `TOP_N` vs a full
`ORDER BY` is the "don't sort what you can discard" hook.

---

## Lesson 5: Dark Data in Practice — Logs

**Learning Goal:** Learners turn the course's "dark data" — 300,000 event rows
— into decisions: aggregate by level, drill down by service, and check for
rare-but-deadly events, all with `GROUP BY` and the same engine from Lessons
3–4.

**Lesson Sequence:**
1. Revisit Lesson 1's "dark data": the app writes 300k `events` rows nobody
   looks at. Read the `events` seed: level + service columns, with
   `MOD(abs(HASH(i)), …)` shaping ~1% ERROR/WARN and ~0.1% FATAL.
2. Run the one-line aggregate:
   `curl -s localhost:8084/analytics/log-levels` → INFO dominates, ERROR/WARN
   are ~1%, FATAL a handful. The test
   `logLevelsShowRareEventsAmidstInformationLogs` asserts exactly this shape.
3. Drill down in the CLI — `GROUP BY service`, and a `WHERE level='FATAL'`
   count per service:
   `docker compose exec duckdb duckdb /data/analytics.duckdb -c "SELECT service, count(*) FROM events WHERE level='FATAL' GROUP BY service ORDER BY 2 DESC;"`
4. Hands-on: complete Exercise 5.

**Assessment Evidence:** Learner interprets a log-level histogram (which bucket
is the anomaly?), writes the two `GROUP BY` queries (by level, by level+service)
and explains one operational decision the numbers support.
- Assessment answer key: `solutions.md` → Lesson 5.

**Learner Profile / Prior Knowledge:** Lessons 1–4; the idea of an application
log as structured data.

**Learning Activities:** Histogram reading, drill-down query writing, "what
would you alert on?" decision sprint.

**Output Requirements:** A one-line SQL query that lists each service's count of
FATAL events, plus a one-sentence recommendation on one metric to alert.

**Accessibility & Supports:** Provide the query templates; allow pair work;
screenshot of the CLI output accepted.

**Teacher Decisions:** The talk's point to land: this data was *already being
written* — the analytics engine only needed the event table, no new pipeline.
`FATAL` count per service is the course's canonical "needle in dark data"
answer.

---

## Lesson 6: Beyond the File — Views, Portability & the Toolchain

**Learning Goal:** Learners distinguish a view (a saved query) from a table,
explain why one portable file is a feature, and sketch the toolchain around the
engine — `COPY`/Parquet, the extension ecosystem, and the honest cases where a
warehouse or a row store is still the right answer.

**Lesson Sequence:**
1. Read the `v_completed_orders` view in `seed.sql` (and its comment: "a view
   stores the query, not the data"). Confirm it appears as a "table" in
   `/database/info` `tables` — and note it recomputes each time it is read.
2. Portability demo in the CLI: the whole database is one file
   (`/data/analytics.duckdb`, ~a few MB). Copy it, open the copy elsewhere,
   run the same query. Check the size: `docker compose exec duckdb sh -c 'ls -la
   /data'`.
3. Tour the toolchain (the talk's second half): `COPY (query) TO 'x.parquet'`,
   the extension ecosystem (`parquet`, `httpfs`, `shellfs`, `postgres_scanner`,
   `sqlite_scanner`, MotherDuck) — every one of them *reads through the same
   engine*.
4. The honest counter-arguments: DuckDB is OLAP-on-one-machine; it is not a
   write-heavy OLTP store (your app still owns a row store), not a multi-node
   warehouse. Place it back on the Lesson 2 2×2.
5. Hands-on: complete Exercise 6.

**Assessment Evidence:** Learner states what changes on disk when a view's base
table gets one new row (nothing) versus a table (the row), names two things a
`COPY … TO 'x.parquet'` gives you, and answers "is DuckDB a good OLTP server?"
with the class's definition of embedded/OLAP.
- Assessment answer key: `solutions.md` → Lesson 6.

**Learner Profile / Prior Knowledge:** All prior lessons; a loose sense of what
a database "engine" vs "server" is.

**Learning Activities:** View-vs-table demo, file-copy portability check,
extension-ecosystem tour, "right tool for the job" debate.

**Output Requirements:** A short table: for 4 scenarios (write-heavy app DB,
year of logs, a 5 GB CSV to analyse, multi-PB enterprise analytics) — engine,
why, and any caveat.

**Accessibility & Supports:** Pre-scripted CLI commands; allow table-first
learners to fill the table before the discussion; recorded demo as an option.

**Teacher Decisions:** End on judgment, not hype: embedded analytical engines
do not replace OLTP stores or warehouses — they fill the not-so-big-data slot
in between. Reuse the Lesson 2 matrix so the course closes where it opened.

---

## Integration Project: Build a Metric End-to-End

**Learning Goal:** Learners ship a *new* analytical query — write the SQL,
EXPLAIN it, expose it through the app, and prove it with a test — applying
Lessons 1–6 to one feature.

**Learning Sequence:**
1. Pick a metric not already in the app (candidates: revenue by country,
   weekday revenue, average line per order, ERROR rate per service).
2. Get it right in the CLI against the shared file, then read its
   `EXPLAIN ANALYZE` and note the dominant node.
3. Add it to `AnalyticsService` + `AnalyticsController` mirroring an existing
   method (a record, a `jdbc.query(…)`, a `@GetMapping`), returning ORDERED
   rows.
4. Add an `AnalyticsApiTest` case asserting shape (length, ordering, a named
   field), run `mvn -f app/pom.xml test`.
5. Deliver a pull-style write-up: the metric, the plan reading, the test.

**Assessment Evidence:** The write-up includes the SQL, a correct plan reading,
and a green test proving the shape — with the metric visibly ranked (top 1 and
worst 1 named with numbers).
- Assessment answer key: `solutions.md` → Integration.

**Learner Profile:** Same as all lessons (integration, so pairs are
encouraged).

**Prior Knowledge:** All six lessons.

**Learning Activities:** CLI prototyping, code mirroring, plan reading, test
authoring.

**Output Requirements:** ≤2-page markdown note + a green test method in
`app/src/test/java/…/AnalyticsApiTest.java`.

**Accessibility & Supports:** Starter skeleton for the service method and test
provided; verbal walkthrough accepted; extra time.

**Teacher Decisions:** Grade on *evidence*: a specific number for the top and
bottom row, a specific node in the plan, a specific assertion in the test.
The "worked example" to mirror is `topCustomers` (record → query → endpoint →
test).