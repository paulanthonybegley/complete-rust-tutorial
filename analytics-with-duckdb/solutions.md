# Solutions: Analytics for Not-So-Big Data with DuckDB

Answers were verified against the app and the shared file on **DuckDB v1.3.1**
(CLI + JDBC pinned to the same version): `mvn -f app/pom.xml test` (7/7 green,
in-memory) plus live queries through `/analytics/*` and the container CLI.
Row counts and rankings come from the deterministic seed — they are stable for
a given engine version because every "random" choice is a hash of the row
index. If you run a different DuckDB version, hashes may shift; the *shape* of
every answer (which query, which node, which comparison) stays the same.

---

## Lesson 1: Medium Data & Dark Data

**1.1 — the seed promises and `/database/info` reports:**
30 countries, 500 customers, 10 products, 50,000 orders, ~250,000 order lines
(actual: **249,551**), 300,000 events. The endpoint confirms exactly that:

```json
{"orders":50000,"orderLines":249551,"events":300000,"tables":["country","customer","events","order_lines","orders","product","v_completed_orders"]}
```

**1.2 — the line (one valid ordering):** 3 MB invoice CSV → the course's
smallest databases → **the ~40 MB monthly log** and **`analytics.duckdb`** are
the *medium* ones (roughly the talk's 1–100 GB band; ours sits at the low end —
"not-so-big"), → 2 TB telemetry is *big*. Signals that matter: query latency
ad hoc, storage/copy cost, and how many machines it needs. Medium = interesting
queries are possible on one machine.

**1.3 —** Free answer. The course's own example is the `events` table: written
by every request, read by nobody until Lesson 5. A useful answer names a real
unread table (e.g. request logs) and the columns needed to answer a real
question (timestamp, service, status code, duration).

## Lesson 2: Embedded & Columnar — No Server

**2.1 — the 2×2:**

| | OLTP | OLAP |
|---|---|---|
| **Embedded** | SQLite (a file, one writer, transactional row writes) | **DuckDB** (a file or in-memory, columnar, fast analytics) |
| **Standalone** | Postgres / MySQL (a server, connections, row writes) | A warehouse (Snowflake et al.): OLAP as a service |

**2.2 —** In-memory mode, *every new JDBC connection is a brand-new empty
database* (the `DuckDbConfig` comment). The app therefore uses one
`SingleConnectionDataSource` — one shared connection = one engine = one
database — instead of a pool. `docker-compose.yml` exposes no ports because
there is no server to connect to: the container is the **CLI** (plus the seed
script and a healthcheck that opens the file), and "the database" is the file
on the shared volume.

**2.3 —** Two processes — the app's JDBC connection and the container CLI — both
reach the same bytes through the same file; no network hops in between. Opening
a *new in-memory engine* (`duckdb -c "SELECT count(*) FROM orders"`) fails with
`Catalog Error: Table with name orders does not exist!` — proven evidence that a
fresh engine has none of our data, which is exactly why the app shares one
connection.

## Lesson 3: Vectorized `GROUP BY` at 5 ms

**3.1 —** Tables: `order_lines`, `orders`, `product`. Joins:
`orders.id = order_lines.order_id` (filtered to `status='completed'`) and
`product.id = order_lines.product_id`. Aggregation:
`SUM(ol.quantity * ol.unit_price)` grouped by `pr.category`, ordered by revenue.

**3.2 —** Plan for `revenue-by-category` (`Total Time: 0.0053s`):
below `EXPLAIN_ANALYZE` is `PROJECTION`, then `ORDER_BY`, then two
`PROJECTION`s — the real work is underneath:
- `HASH_GROUP_BY` — `Groups: #0`, `Aggregates: sum(#1)` → 4 rows
- two `HASH_JOIN` nodes — conditions `product_id = id` and `order_id = id`
- three `TABLE_SCAN` nodes — `order_lines` (249,548 rows), `orders`
  (44,992 after `status='completed'`), `product` (10 rows)

224,508 completed lines in, 4 rows out, a few milliseconds. That is the columnar
story: read only the needed columns, aggregate in vectorized batches.

**3.3 —** `top-customers` plan uses `PERFECT_HASH_GROUP_BY` (per-customer
counts), then `TOP_N` (`Top: 5`, `Order By: #1 DESC`) — the engine keeps a
5-slot heap instead of sorting all 500 customers. `log-levels` reads one column
of one table into a `HASH_GROUP_BY` → nearly empty plan, single-digit ms.

## Lesson 4: Joins, Order & Time Buckets

**4.1 —** #1 is **Customer 40** (Argentina) at **521,950.68**, #2 is Customer
390 at 518,436.15 — a gap of **3,514.53**. The `TOP_N` node does the
"keep only top N" work.

**4.2 —** `?bucket=month` → **12 rows**; `?bucket=day` → **365 rows**. Highest
month: **August 2025** (`14548731.83`); highest day: **2025-07-31**
(`598642.81`). Weekday SQL and verified top three:

```sql
SELECT dayname(o.order_date) AS weekday, ROUND(SUM(ol.quantity*ol.unit_price),2) AS revenue
FROM orders o JOIN order_lines ol ON ol.order_id = o.id
WHERE o.status = 'completed'
GROUP BY 1 ORDER BY revenue DESC;
```

Wednesday (24,408,228.75) > Tuesday (23,757,242.39) > Friday (23,383,455.97).

**4.3 —** The query (most completed orders per country):

```sql
SELECT co.name, count(*) AS n
FROM orders o
JOIN customer c  ON c.id  = o.customer_id
JOIN country co  ON co.id = c.country_id
WHERE o.status = 'completed'
GROUP BY co.name ORDER BY n DESC;
```

Verified top three: **Argentina 2,454**, France 2,078, Canada 1,966. With an
`ORDER BY … LIMIT 3`, DuckDB plans a `TOP_N` (no full sort of all 30 countries).

## Lesson 5: Dark Data in Practice — Logs

**5.1 —** `/analytics/log-levels` returns INFO **293,835** (~97.9%), WARN
**3,090** (~1%), ERROR **2,751** (~0.9%), FATAL **324** (~0.1%). The needle is
**FATAL** — by the seed rules in `seed.sql` (`MOD(abs(HASH(i)),…)` buckets:
1000→FATAL, 100→ERROR, 50→WARN) it is expected at ~0.1%, i.e. a known rare
signal, not noise. Even one FATAL per service per day is worth an alert.

**5.2 —** Verified drill-downs (container CLI):

```sql
SELECT service, count(*) FROM events WHERE level='FATAL' GROUP BY service ORDER BY count(*) DESC;
```

| service | FATAL | ERROR | WARN |
|---------|-------|-------|------|
| gateway | **122** | 883 ( 3rd) | 1,028 |
| checkout| 105 | 914 (2nd) | **1,085** |
| cart    | 97 | **954** (1st) | 977 |

So: most FATAL → **gateway**; most ERROR → **cart**; most WARN → **checkout**.

**5.3 —** Example that the numbers justify: alert when FATAL/day exceeds a
small single digit, or when ERROR rate per service exceeds ~1%. Both are
one-line `GROUP BY` over the table the app *already* writes — dense with the
talk's "dark data you already have" point.

## Lesson 6: Beyond the File

**6.1 —** **(b) the base tables change; the view is untouched.** A view stores
the *query*, not rows; every read recomputes it from the base tables.
`/database/info` lists it in `tables`
(`information_schema.tables`) even though nothing separate exists on disk for
it.

**6.2 —** The file is only a few MB; copying it and running
`SELECT max(order_date) FROM orders` in the copy returns **2025-12-31** —
the whole database travelled as one portable file with no server, no export,
no restore. That IS the talk's "it is just a file" model.

**6.3 —** `COPY (…) TO '/data/categories.parquet'` writes a parquet file;
`read_parquet` reads it back. It gives you (any two): a compressed,
columnar exchange format; analytics over files that never touch the live DB;
interop with the ecosystem (Spark/Polars/Pandas, the `parquet`/`httpfs`
extensions); and "the database is the query, the files are the data".

**6.4 —** Recommended: (a) **a row store** (Postgres/SQLite) — DuckDB is
OLAP, not a high-rate OLTP writer; (b) the file is **analytics.duckdb** with
several years appended — or `parquet` partition per month (case c's cousin);
(c) **DuckDB over the CSV/Parquet directly** (`read_parquet`), no loading
step; (d) **a warehouse** (Snowflake et al.) when concurrency, governance and
scale outgrow one machine — DuckDB's `motherduck` extension is the middle
step. One caveat to name: embedded engines are single-process, so
concurrent-writer scenarios and multi-node scale are where the standalone
options win.

## Integration Project: Build a Metric End-to-End

**Verified worked example (revenue by country):**

1. **SQL** — prototyped in CLI:
   ```sql
   SELECT co.name, ROUND(SUM(ol.quantity*ol.unit_price),2) AS revenue
   FROM order_lines ol
   JOIN orders o     ON o.id = ol.order_id AND o.status='completed'
   JOIN customer c   ON c.id = o.customer_id
   JOIN country co   ON co.id = c.country_id
   GROUP BY co.name ORDER BY revenue DESC;
   ```
   Top row: **Argentina 8,888,806.48**, then France 7,660,336.02 and UAE
   7,155,522.12 (Argentina also leads completed order *count*) — hash-derived
   totals are stable per engine version.
2. **Plan** — dominated by the two `HASH_JOIN`s feeding one `HASH_GROUP_BY`
   (roughly the Lesson 3 shape); a `TOP_N` appears if you `LIMIT` the result.
3. **App** — mirror `topCustomers`: a `CountryRevenue` record, an
   `AnalyticsService` method using the query above, a `/analytics/revenue-by-country`
   `@GetMapping` — always `ORDER BY … DESC`.
4. **Test** — a green case asserting `$.length()` matches the country count
   (30), `$[0].revenue` is a number, and `$[0].revenue >= $[1].revenue`
   (ordering). `mvn -f app/pom.xml test` → BUILD SUCCESS.

Grading guidance: SQL runs and orders correctly (30%); plan reading names a
concrete node + row count (20%); app code mirrors the pattern and returns
ordered rows (30%); test asserts shape and ordering and is green (20%).