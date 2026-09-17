# Exercises — Analytics from Python DataFrames with DuckDB

Exercises mirror the app endpoints (`app/duckdb_edu/`) and the test suite
(`app/tests/test_*.py`). Levels follow the notebook ladder
(blank / rookie / experienced). Answers in `solutions.md`. Start the app
first: `cd app && .venv/bin/uvicorn duckdb_edu.main:app --port 8084`, then
hit the URLs.

## Lesson 1 — SQL on a DataFrame

**1.01 (blank)** GET `/magic`. How many rows come back for
`salary > 125000`? Does the endpoint report that SQL matched pandas?

**1.02 (blank)** GET `/registered`. Which job title has the most employees,
and do the counts sum to 200?

**1.03 (rookie)** Name both ways a pandas DataFrame becomes queryable by
DuckDB, and say which one survives a later variable rename.

## Lesson 2 — Inspection

**2.01 (blank)** GET `/tables/employees/describe`. Which 6 columns come
back? What type is `salary`?

**2.02 (blank)** GET `/tables/sales/summarize`. What are the min and max of
`quantity`? What is the null% of `unit_price`?

**2.03 (rookie)** GET `/explain/avg-salary-by-job`. Which two things inside
the plan prove DuckDB did the work?

**2.04 (rookie)** GET `/tables/nope/describe` — what status code, and why is
a 400 the "right" answer here?

## Lesson 3 — Joins

**3.01 (blank)** GET `/joins`. The result filters to category `books` — which
two product names can appear, and does the endpoint prove "sql == pandas"?

## Lesson 4 — Export

**4.01 (blank)** GET `/parquet/products`. How many rows does the parquet
round trip report?

## Course queries

**5.01 (blank)** GET `/queries/avg-salary-by-job` — how many job titles, and
which is paid most on average?

**5.02 (blank)** GET `/queries/sales-by-category` — which category leads in
revenue and by how much?

**5.03 (rookie)** GET `/queries/top-products?limit=3` — is MacBook Pro number
one by revenue? Now ask the *volume* question (`SUM(quantity)`): does the
same product win that race?

**5.04 (rookie)** GET `/queries/revenue-trend?bucket=week` — about how many
weekly buckets appear across the 180-day window?

## Concurrency (challenge)

**6.01 (rookie)** GET `/threads` — how many connections were opened, and do
they all agree on `count(*) FROM sales` (12,000)?

## Transfer (experienced)

**6.02 (experienced)** Load your own small pandas DataFrame (e.g. a class
roster: name, team, score). Register it, then answer "average score per
team" with SQL — and show the pandas `groupby` gives the same answer.

**6.03 (experienced)** Take the `/queries/revenue-trend` pattern and re-do it
with `bucket=month`. How many monthly buckets fit in the 180-day window?
(Recall the seed sales start on 2026-01-01.)