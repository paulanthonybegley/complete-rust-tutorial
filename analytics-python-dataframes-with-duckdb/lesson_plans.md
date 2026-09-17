# Lesson plans — Analytics from Python DataFrames with DuckDB

_Companion to `OLD.txt` ("a lesson plan is a system, not a prompt"). The unit
is built on the premise that a pandas user should not have to leave Python to
get real SQL power: DuckDB queries the DataFrame that is already in memory.
Each session below names the system inputs (goal, learners, prior knowledge,
evidence) and the sequence + activities. The endpoints in
`app/duckdb_edu/` (and their pytest counterparts in `app/tests/`) are the
activities.*

## Unit learning goal

> Learners can run DuckDB SQL **directly against their in-memory pandas
> DataFrames**, choose between scope magic and explicit `register()`,
> introspect any table with DESCRIBE/SUMMARIZE/EXPLAIN, join DataFrames with
> SQL, and export the result to parquet — without loading anything into a
> server.

## Learner profile

Python-first analysts who live in pandas; comfortable with function calls and
DataFrames, newer to SQL grouping semantics. They want answers about *their*
data, in *their* process. Time-bounded (1-2 hours); prefer examples over
theory.

## Prior knowledge

- pandas: DataFrames, column access, `merge()` (assumed).
- Basic Python (assumed).
- *Not* assumed: DuckDB at all, `GROUP BY`, `date_trunc`, or `EXPLAIN` —
  each is introduced on the endpoint that needs it.

## Session structure

| lesson | endpoint(s) | one-line hook |
|---|---|---|
| 1. Scope magic vs `register()` | `/magic`, `/registered` | "DuckDB just ran SQL over a pandas variable" |
| 2. Inspection | `/tables/{t}/describe`, `/tables/{t}/summarize`, `/explain/{metric}` | "programmatic alternatives to eyeballing" |
| 3. Joins | `/joins` | same result as `pd.merge`, no transposing |
| 4. Export | `/parquet/{table}` | `COPY ... TO (FORMAT PARQUET)` round trip |
| course queries | `/queries/*` | four real analytical patterns |
| concurrency | `/threads` | threads → separate connections to one file |

---

## Lesson 1 — SQL on your DataFrame (level: understand)

- **Goal**: run SQL over an existing pandas DataFrame, two ways.
- **Evidence (EES)**: `/magic` returns exactly the 3 rows pandas would have
  produced for `salary > 125000 ORDER BY salary DESC` (and asserts `sql ==
  pandas`); `/registered` returns the job-title counts that sum to 200.
- **Prior knowledge activated**: pandas column/`merge` instincts; the idea of
  a "table".
- **Sequence**: scope magic (name a local DataFrame, run SQL on it) →
  explicit `register()` → the unregister caveat.
- **Accessibility**: both endpoints return JSON so "the answers match
  pandas" is visible side by side.

## Lesson 2 — Inspect before you trust (level: analyze)

- **Goal**: use DESCRIBE, SUMMARIZE and EXPLAIN instead of eyeballing.
- **Evidence (EES)**: DESCRIBE lists the 6 `employees` columns (salary
  INTEGER); SUMMARIZE profiles quantity (min 1, max 4, null% 0.0) and
  distribution shape (6 rows, one per column); EXPLAIN on
  `avg-salary-by-job` contains `GROUP` and `employees`; unknown names → 400.
- **Prior knowledge activated**: the cohort's pandas `dtypes` habit.
- **Sequence**: schema → profile → plan → engine settings.
- **Accessibility**: the three views are returned as parallel JSON views of
  the same table, so comparison is one click.

## Lesson 3 — Joins: SQL over two DataFrames (level: analyze)

- **Goal**: `JOIN` two in-memory DataFrames and prove it matches pandas.
- **Evidence (EES)**: `/joins` books-only result matches `pd.merge(...)` row
  for row (the endpoint returns an equality flag); names are exactly
  "Clean Code" / "Germinal".
- **Prior knowledge activated**: `pd.merge(on=...)`.
- **Sequence**: register both frames → JOIN → compare with pandas.
- **Accessibility**: the books filter keeps the joined result small enough
  to eyeball (≈2,391 rows in the seed but the endpoint shows the equality
  proof, not the whole table).

## Lesson 4 — Export to parquet (level: analyze)

- **Goal**: push a table out with COPY and read it back.
- **Evidence (EES)**: `/parquet/products` writes the file and round-trips
  `read_parquet` to the same **10** rows.
- **Prior knowledge activated**: CSV exports in pandas.
- **Sequence**: COPY ... TO (FORMAT PARQUET) → read back → count.
- **Accessibility**: the round-trip count is the "did it lose anything"
  signal.

## Course queries (level: analyze/challenge)

- **Goal**: four patterns the cohort will actually reuse.
- **Evidence (EES)**: avg-salary-by-job returns 10 rows descending;
  sales-by-category returns 4 categories descending; top-products honours
  `LIMIT`; revenue-trend buckets by day/week/month (`date_trunc`) across the
  180-day window (week ≈ 27 buckets).
- **Prior knowledge activated**: `GROUP BY`, `ORDER BY`, `ROUND`, `LIMIT`.
- **Sequence**: aggregate by one thing → join + aggregate → limit → time
  bucket.
- **Accessibility**: each returns a small ordered table — ideal for the "did
  my answer match" self-check.

## Concurrency (level: challenge)

- **Goal**: show what threads (not processes) mean for an embedded DB.
- **Evidence (EES)**: `/threads` opens **3** connections to the same file,
  each reads `count(*) FROM sales`, all agree (12,000) and run concurrently.
- **Prior knowledge activated**: threads vs processes intuition from
  Python's GIL discussions.
- **Sequence**: one file → many connections → consistent reads.
- **Accessibility**: the response states `all_threads_agree` explicitly.

---

## Assessment evidence (unit-wide)

| what | where | pass bar |
|---|---|---|
| Scope magic matches pandas | `/magic` + test_scope_magic | `sql == pandas`, 3 rows |
| register() counts sum to 200 | `/registered` + test_registered | sum(n) = 200 |
| DESCRIBE/SUMMARIZE/EXPLAIN | `/tables/*`, `/explain/*` | schema intact; profile sane; plan has GROUP |
| Join equals merge | `/joins` + test_join_dataframes | equality flag true; books only |
| Parquet fidelity | `/parquet/*` + test_to_parquet | 10 rows round-trip |
| Query patterns | `/queries/*` + 4 query tests | ordered, limited, bucketed |
| Concurrency | `/threads` + test_threads | 3 threads, 12,000 rows each, agree |

## Teacher decisions baked in

- Use the pinned venv (`duckdb==1.5.5`) — seed values are hash-derived and
  can shift with the DuckDB version, which has produced red tests when the
  local driver drifts. Structural tests (DESCRIBE/EXPLAIN/categories) hold;
  numeric/version tests need the pin.
- Run `uvicorn duckdb_edu.main:app` from `app/` for live demos; the Docker
  CLI side-car is optional and shares the same file.
- Unlock the cohort's transfer test: give them their *own* DataFrame and ask
  them to answer a question with a JOIN they write.