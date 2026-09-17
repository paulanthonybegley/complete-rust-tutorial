# Exercises: Analytics for Not-So-Big Data with DuckDB

Work with the app in [`app/`](./app) and the shared database at `data/`.
Answers are in [`solutions.md`](./solutions.md).

**Setup reminders**
- Database: `docker compose up -d --build` (healthcheck runs `SELECT count(*) FROM orders`).
- Tests (no Docker needed): `export JAVA_HOME=$(/usr/libexec/java_home -v 24)` then
  `mvn -f app/pom.xml test`.
- Run the API: `mvn -f app/pom.xml spring-boot:run` (port 8084), then use
  [`analytics.http`](./analytics.http) or curl.
- SQL on the shared file: `docker compose exec duckdb duckdb /data/analytics.duckdb`.

---

## Lesson 1: Medium Data & Dark Data

**Exercise 1.1 — size the dataset.** Read the header comment in
`app/src/main/resources/seed/seed.sql`. What are the promised row counts for
countries, customers, products, orders, order lines and events? Verify them
against `/database/info` (`curl -s localhost:8084/database/info`) and write
the numbers you actually see.

**Exercise 1.2 — sort the data line.** Put these on the "not-so-big → big"
line and justify each: a 3 MB CSV of this year's invoices, a ~40 MB log file
your service writes each month, 2 TB of product telemetry, the course's
`analytics.duckdb` file. Which two would the talk call *medium*?

**Exercise 1.3 — find dark data in your world.** Name one dataset your
application writes *today* that no report reads. For it, list the columns you
would want in a table to finally answer a question about it.

## Lesson 2: Embedded & Columnar — No Server

**Exercise 2.1 — draw the 2×2.** Fill in the matrix with one engine per
quadrant and a one-line note each:

| | OLTP | OLAP |
|---|---|---|
| **Embedded** | | |
| **Standalone** | | |

**Exercise 2.2 — read the connection decision.** In
`config/DuckDbConfig.java`, what happens when a *second* JDBC connection opens
in in-memory mode, and what does the app do instead? Why does
`docker-compose.yml` expose no ports?

**Exercise 2.3 — prove they share one file.** Run this in the container while
the app is up (Lesson 2): `docker compose exec duckdb duckdb
/data/analytics.duckdb -c "SELECT count(*) FROM orders;"`. Query a number from
`/analytics/revenue-by-category`. What is *between* the two processes? Then open
a *new* in-memory engine — `duckdb -c "SELECT count(*) FROM orders"` in the
container — and explain the `Error`/empty result.

## Lesson 3: Vectorized `GROUP BY` at 5 ms

**Exercise 3.1 — read the query.** Open `service/AnalyticsService.java`
(`revenueByCategory`). List the tables involved, the join conditions, and the
one line that does the aggregation.

**Exercise 3.2 — read a plan.** Run:
```bash
curl -sG localhost:8084/analytics/explain --data-urlencode 'metric=revenue-by-category'
```
Write down: the node right below `EXPLAIN_ANALYZE`, the two `HASH_JOIN`
conditions, the `HASH_GROUP_BY` aggregate, and the `Total Time`.

**Exercise 3.3 — plan for the other metric.** Run the same for `metric=top-customers`.
Name the node that avoids materialising a full sorted list. Then run
`metric=log-levels` — why is that plan almost empty?

## Lesson 4: Joins, Order & Time Buckets

**Exercise 4.1 — top givers.** Run:
```bash
curl -s 'localhost:8084/analytics/top-customers?limit=3'
curl -s 'localhost:8084/analytics/top-customers?limit=10'
```
Who is #1 overall, and by how much do #1 and #2 differ? Which *node* keeps only
the top N in the plan?

**Exercise 4.2 — flip the bucket.** Run `revenue-trend?bucket=month` and
`?bucket=day`. How many rows do you get? Which month had the highest revenue
and which single day? Rewrite the query shape yourself for a `weekday` grain
against the CLI and say which three weekdays lead. (Weekday hint:
`dayname(o.order_date)`.)

**Exercise 4.3 — join the countries.** Write the SQL that answers "which
country has the most completed orders?" against the CLI (hint: `orders` →
`customer` → `country`, `GROUP BY co.name`). State whether the result needs a
full sort of all countries or a `TOP_N`.

## Lesson 5: Dark Data in Practice — Logs

**Exercise 5.1 — read the histogram.** Run `curl -s
localhost:8084/analytics/log-levels`. Roughly what fraction are INFO? Which
level is the "needle" — and how do you know it is a needle and not noise, given
the seed rules in `seed.sql`?

**Exercise 5.2 — drill down by service.** Against the CLI run
```sql
SELECT service, count(*) FROM events WHERE level = 'FATAL' GROUP BY service ORDER BY count(*) DESC;
```
and the same for `ERROR` and for `WARN`. Which service is the top offender at
each level?

**Exercise 5.3 — decide.** Give one alert threshold the numbers justify (e.g. a
rate or a count per time window) and the one-line query that powers it.

## Lesson 6: Beyond the File

**Exercise 6.1 — view vs table.** `v_completed_orders` shows up in
`/database/info` `tables`. Read its definition in `seed.sql`. What changes on
disk when a new completed order is inserted: (a) the view, (b) the base tables,
(c) both, (d) neither — and why?

**Exercise 6.2 — portability.** Get the file size in the container
(`ls -la /data`). Copy `analytics.duckdb` to a second name and open the copy
with the CLI, running `SELECT max(order_date) FROM orders;`. What does this
prove about "the database"?

**Exercise 6.3 — COPY to Parquet.** Run against the CLI:
```sql
COPY (SELECT category, revenue FROM (
  SELECT pr.category, SUM(ol.quantity * ol.unit_price) AS revenue
  FROM order_lines ol JOIN product pr ON pr.id = ol.product_id
  GROUP BY pr.category))
TO '/data/categories.parquet';
```
Then `SELECT * FROM read_parquet('/data/categories.parquet')`. List two things
`COPY`/Parquet give you for "not-so-big data" workflows.

**Exercise 6.4 — the honest trade-offs.** For each scenario, name the engine
you would actually use and one caveat: (a) an app that writes 2,000 small
orders/sec, (b) a year of logs to analyse ad hoc, (c) a 5 GB CSV of events, (d)
a company-wide BI platform serving thousands of analysts.

## Integration Project: Build a Metric End-to-End

**Rubric** — deliver all four:

1. **SQL** (30%) — a new analytical query over the shared file, prototyped in
   the CLI first, returning *ordered* rows. Starter ideas: revenue by country,
   weekday revenue (you already have the SQL from Exercise 4.2), average lines
   per order, ERROR rate per service per day.
2. **Plan** (20%) — `EXPLAIN ANALYZE` the query and name the dominant node and
   its row count.
3. **App** (30%) — mirror the `topCustomers` pattern: a record in
   `model/`, a method in `service/AnalyticsService.java`, a `@GetMapping` in
   `web/AnalyticsController.java`, all returning ordered results.
4. **Test** (20%) — add a green `@Test` to `AnalyticsApiTest` asserting shape
   (array length, a top row's name/value, descending order) and run
   `mvn -f app/pom.xml test`.