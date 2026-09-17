# Solutions — Analytics from Python DataFrames with DuckDB

Verified against the pinned DuckDB **v1.5.5** and
`app/duckdb_edu/seed/seed.sql` (idempotent, hash-derived). These numbers
are stable *on that pin* — if the driver drifts, hash-derived seed values and
`version()` checks change, which is exactly why `requirements.txt` pins
`duckdb==1.5.5`.

Seed totals: **200 employees** (10 job titles, 4 departments), **10
products**, **12,000 sales** across a **180-day** 2026 window (quantities
1-4), plus the denormalised `v_sales_detail` view.

## Lesson 1 — SQL on a DataFrame

**1.01** **3 rows** (the `salary > 125000` top-3, `ORDER BY salary DESC`).
Yes — the response carries `matched_rows == 3` and asserts `sql` equals
`pandas`, which is the endpoint's whole trust device.

**1.02** The **Data Scientist** title leads the `registered_employees`
count (from `GROUP BY job_title`); all counts **sum to 200**.

**1.03** (1) **Scope magic** — DuckDB finds a local pandas variable by name
and queries it as a table (`SELECT ... FROM employees`), and (2) **explicit
`register(name, df)`**, which creates a named table in the connection.
`register()` survives a rename of the variable because the *name*, not the
variable, is what SQL references.

## Lesson 2 — Inspection

**2.01** Columns: `id, name, job_title, department, salary, hire_date`.
`salary` is **INTEGER** (range 50,000-144,000).

**2.02** `quantity`: min **1**, max **4**, null% **0.0**. `unit_price` is a
`DECIMAL(10,2)` snapshot with null% 0.0 (12,000 non-null). Remember that on
1.5.5 the SUMMARIZE min/max/avg stats come back as **strings** — read them
as text until cast.

**2.03** The plan contains **GROUP_BY** and scans `employees` — proof the
average is computed as a pushdown inside DuckDB, not by a Python loop doing
groupby after pulling all rows.

**2.04** **400** — the `guard` wrapper turns the unknown-table `ValueError`
into `HTTP 400` so a client knows it asked a wrong question, not that the
server broke. (404 would conflate "bad params" with "missing page".)

## Lesson 3 — Joins

**3.01** Only **Clean Code** and **Germinal** (the two `books` products) can
appear, and the endpoint's equality flag confirms the SQL `JOIN` matched
pandas `merge` row-for-row on `sold_on, name, quantity, unit_price`.

## Lesson 4 — Export

**4.01** **10** — `COPY (SELECT * FROM products) TO ... (FORMAT PARQUET)`
writes the file, and `read_parquet(...)` counts it back as 10. Row-for-row
parquet is a lossless round trip here.

## Course queries

**5.01** **10** job titles (one per seeded title). On the pinned seed the
top earner on average is **Data Scientist ≈ $105,632**.

**5.02** **electronics** leads, **≈ $8,246,092**, then home (≈ $296.6k),
books (≈ $151.7k), grocery (≈ $72.9k) — the laptop price swamps every other
category.

**5.03** MacBook Pro is #1 by revenue (**≈ $7.6M** from ~3,050 units), but
"which product the cohort buys" is a *volume* question: by `SUM(quantity)` a
small-ticket item leads — **Organic Coffee ≈ 3,152 units** (revenue ~$28k) —
with Desk Lamp ~3,049, Kettle ~3,011 and MacBook ~3,050 in close company.
Price ≠ popularity: the most-purchased product contributes barely a rounding
on revenue. Worth saying out loud in any report.

**5.04** **≈ 27** weekly buckets (180 days ÷ 7 ≈ 25.7, plus the partial
first/last weeks round to 27) — check `len`/distinct buckets from the
endpoint.

## Concurrency

**6.01** **3** threads each open their **own connection to the same file**;
all report `count(*) FROM sales = 12,000`, so `all_threads_agree` is True.
That is the "embedded = many connections, one file" story.

## Transfer

**6.02** Register the roster as `roster`, then
`SELECT team, AVG(score) FROM roster GROUP BY team` — the pandas
`df.groupby("team").score.mean()` must match `diff()`-style equality, the
same trust device as the course endpoints.

**6.03** With sales starting 2026-01-01, **180 days span Jan-Jun** →
`bucket=month` returns **6** monthly buckets (one per month). It is the same
`date_trunc('month', sold_on)` query, a different resolution — the point of
the parameterised bucket.