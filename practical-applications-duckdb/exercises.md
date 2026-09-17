# Exercises — Practical Applications of DuckDB

Exercises mirror the app endpoints and the test suite
(`app/tests/test_*.py`). Levels follow the notebook ladder
(blank / rookie / experienced). Answers in `solutions.md`. Start the app
first (`uvicorn duckdb_app.main:app`) — every exercise is a real request.

Setup you need before class (once):

```bash
cd app && .venv/bin/python scripts/generate_samples.py   # creates data/files/
```

## Session 1 — Wrangling

**1.01 (blank)** Call `/wrangle/csv/fitbit.csv`. How many rows does the
sniffer infer? How many columns, and what are they?

**1.02 (rookie)** Call `/wrangle/csv/fitbit.csv/frozen`. Compare the `sql`
field to the sniffed `read_csv(...)`. What did *freezing* make explicit?

**1.03 (rookie)** Call `/wrangle/graph/followers.jsonl`. From the top rows,
which user is followed the most, and by how many?

**1.04 (experienced)** The JSON endpoint uses `UNNEST`. On DuckDB 1.5.5 the
plain `UNNEST(data.follows)` form fails to parse. Find the working expression
(for the record: `UNNEST(list_transform(data.follows, f -> f.user_id))`) and
explain what `list_transform` is doing in one sentence.

**1.05 (experienced)** Hit `/wrangle/parquet/sales`, then `/wrangle/hive`.
Why is a `category=.../*.parquet` folder "one table"? What does the `EXPLAIN`
in `/wrangle/hive/{category}` prove?

## Session 2 — Relational API & interop

**2.01 (blank)** `/relations/api` — is the pipeline you get back a DataFrame
or a *relation*? What word appears before anything is materialised?

**2.02 (blank)** `/relations/interop` — name the three dialects the same
result is served as.

**2.03 (rookie)** `/relations/polars` — the registered polars frame is
queried with SQL (`WHERE score > 10`). Why does `register` beat "scope
magic"?

## Session 3 — Inspection

**3.01 (blank)** `/plans/summarize/sales` — what is the min, max, and null%
of `quantity`? What is the profile for `unit_price`?

**3.02 (blank)** `/plans/explain/sales-by-category` — which two plan
keywords prove the aggregation is pushed into DuckDB?

**3.03 (rookie)** `/info` — list three engine "facts" that are about the
*file*, not a server.

## Session 4 — Extensions

**4.01 (blank)** `/extensions/spatial` — which place is closest to Head
Office? Which is furthest?

**4.02 (rookie)** `/extensions/h3` — does it return cells or a graceful
"note"? Explain why that depends on your machine's install.

## Session 5 — Browser (wasm)

**5.01 (blank)** Open `/wasm`. Run the default query. What does the embedded
`employees` CSV produce when grouped by `job_title`?

**5.02 (rookie)** The wasm build has its own DuckDB version. Why does that
matter for numbers you saw in the CLI? (One sentence.)

## Back to the data (cross-cutting)

**6.01 (rookie)** Using the seeded tables, which product generated the most
revenue in the 90-day sales window? Give the number.

**6.02 (experienced)** The top product dominates via its *price*, not its
quantity. Recount revenue per product, then recount per unit sold — which
product wins on total units? (Hint: `SUM(quantity)`.)

**6.03 (experienced)** Generate your own `fitbit.csv` (add a column, change a
header) and redo the sniff+freeze loop. What breaks, and what does freezing
then fix?