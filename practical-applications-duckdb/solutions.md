# Solutions — Practical Applications of DuckDB

Verified against the pinned DuckDB **v1.5.5** and the current
`app/duckdb_app/seed/seed.sql` + `app/scripts/generate_samples.py`
(samples use fixed seeds `Random(2026)` / `Random(4242)`), so these numbers
are stable. Unless noted, queries run against the app's endpoints.

## Session 1 — Wrangling

**1.01** **200 rows**, **5 columns**: `Date, Time, Heart Rate (bpm), Steps,
Calories`. The sniffer survives US dates, 12% blank heart-rate cells, and
quoted commas (`"12,431"`).

**1.02** The frozen version makes the guess explicit —
`read_csv(path, delimiter := ',', header := true, auto_detect := true)`
(plus the heard guess for the whitespace/treat-missing options). Freezing
means the pipeline survives file drift: what the sniffer *guessed* becomes
what the query *declares*.

**1.03** From the generated `followers.jsonl` (60 users, 0-5 follow
targets each), the most-followed users each have **5 followers** — the
top rows show `followed_count = 5` (a small tie at the top because the
graph is small; the sort breaks it with the followed `user_id`).

**1.04** In 1.5.5, `UNNEST(data.follows) AS u(user_id)` fails to parse on a
list of structs; the working idiom is scalar-transforming the list first:

```sql
SELECT u.user_id, count(*) AS followers
FROM read_json_auto('followers.jsonl') AS data,
     UNNEST(list_transform(data.follows, f -> f.user_id)) AS u(user_id)
GROUP BY 1 ORDER BY 2 DESC, 1;
```

`list_transform` maps each `{user_id, since}` struct to its `.user_id`
scalar, and then `UNNEST` flattens that scalar list into rows — a normal row
source. (The seeded graph reaches **53 distinct** targets of the 60 users.)

**1.05** A `category=.../*.parquet` folder is one table because DuckDB's
`read_parquet(path, hive_partitioning := 1)` discovers the `category` column
from the directory layout and adds it as a normal column. The `EXPLAIN` for
`WHERE category = '...'` shows the partition filter applied *before* reading
rows — i.e. partition pruning, the whole point of hive layout.

## Session 2 — Relational API & interop

**2.01** A **relation** — `source` is a `duckdb.DuckDBPyRelation`; nothing
runs until `.df()` / `.fetchmany()` (the endpoint shows `explain()` still
present in deferred form). "SQL doesn't compose; relations do."

**2.02** **pandas** (`.df()`), **polars** (`.pl()`), and **Arrow**
(`.arrow().read_all()`).

**2.03** `register()` makes the frame a *named* table in the connection, so
the SQL reference is stable and re-runnable. Scope magic ("a python variable
named like a table resolves too") is convenient but silent — a rename or a
not-yet-assigned variable breaks the query at runtime, not at parse time.

## Session 3 — Inspection

**3.01** `quantity`: min **1**, max **4** (formula `1 + MOD(HASH(i*3),4)`),
null% **0.0**. `unit_price`: a `DECIMAL(10,2)` — profile shows min/max/avg
and a null% of 0. Remember: `SUMMARIZE` on 1.5.5 renders numeric stats as
text — read them as strings until cast.

**3.02** The `EXPLAIN` plan contains **GROUP_BY** and the scanned relation
(`employees`/`sales` sources) — proof the aggregation is a pushdown, not a
Python-side loop.

**3.03** From `/info`: `duckdb_version` (v1.5.5), settings (`threads`,
`max_memory`), and the `main` schema table list (`employees, products,
sales, places`). All are properties of the *file*, with no daemon or port.

## Session 4 — Extensions

**4.01** Closest to Head Office (Brisbane) is **Brisbane Depot** (same city,
small distance); the furthest is the southern **Gold Coast Depot**. Distances
use `ST_Point(lon, lat)` + `ST_Distance`, `ORDER BY km_to_hq`.

**4.02** It returns cells **or** a graceful note. `spatial` is a core
extension (available everywhere); `h3` is a community extension that must be
installed per DuckDB version + platform, so on some machines (e.g. macOS
arm64 without a cached build) `INSTALL h3` fails and the endpoint reports
`ok: false` with an explanatory note — the honest failure is the lesson.

## Session 5 — Browser (wasm)

**5.01** The default `GROUP BY job_title` query returns the per-title head
counts of the embedded 12-row employees CSV (the single-title count is the
one with the most employees). Exact numbers depend on the embedded CSV — the
point is that a full engine ran in-browser with no server.

**5.02** The wasm build ships **its own DuckDB version**, independent of the
v1.5.5 pin, so a query that behaves differently in-browser vs the CLI is a
*version* difference, not a bug to chase.

## Back to the data

Verified against the seeded tables (staff employees=80, sales=6,000 over a
90-day window):

**6.01** Revenue leader is **MacBook Pro ≈ $4,845,561** (price 2,499 vs the
next product's 199) — electronics dominates at **$5.24M** of the ~$5.5M total
([electronics 5,235,618.99; home 141,572.50; books 95,861.14; grocery
28,443.80]).

**6.02** By units, the picture flattens: the winner on **total units** is not
the laptop — it is one of the low-price movers (compute
`SUM(s.quantity)` per product on `sales`). Verdict: price-driven revenue
tells a different story than volume, which is exactly the lesson for
reporting.

**6.03** Changing a header/file format makes the *declared* frozen query
fail loudly (good) or, if you kept it frozen, the new file is still read
correctly because the options no longer depend on guessing. That is the
sniff-then-freeze contract: sniff is for the first 100 rows, freeze for the
next million.