# Lesson plans — Practical Applications of DuckDB

_Companion to `OLD.txt` ("a lesson plan is a system, not a prompt"). The
source lecture is the Developer Voices "Getting Started with DuckDB" episode
with Simon Aubury and Ned Letcher (co-authors of *Getting Started with
DuckDB*). Each session below names the system inputs (goal, learners, prior
knowledge, evidence) and then the sequence + activities. The app endpoints in
`app/duckdb_app/` are the activities; the plan explains the why.*

## Unit learning goal

> Learners can take a real file on disk (messy CSV, nested JSON, parquet
> folder) and get it into a queryable analytical state with DuckDB — sniffing,
> freezing, and validating each step — and can safely shuttle data between
> pandas/polars/Arrow and SQL.

## Learner profile

Data-curious learners who already write SQL by hand and are comfortable with
Python. Some come from R (see `r/`). Most have never used an embedded
analytical engine and assume "a database means a server". Time-bounded
(2-4 hours); strong preference for seeing results instantly.

## Prior knowledge

- Writing `SELECT ... GROUP BY` by hand (assumed).
- Basic pandas/polars vocabulary (assumed for the interop session).
- *Not* assumed: `read_csv` options, hive partitioning, duckdb-wasm,
  DuckDB's extension model — all introduced live.

---

## Session 1 — Wrangling: get data in, fast (level: understand/analyze)

- **Goal**: sniff a messy CSV, freeze the guess, and query a nested JSON
  graph — without a staging database.
- **Evidence (EES)**: the sniff infers the fitbit schema (5 columns: Date,
  Time, Heart Rate (bpm), Steps, Calories) and 200 rows; the frozen rebuild
  produces `read_csv(..., auto_detect := true)` with explicit options;
  the JSON graph returns per-user degree counts.
- **Prior knowledge activated**: `SELECT` / `count(*)`, file reading in
  Python.
- **Sequence**: Fitbit CSV (Simon's 70,000-file story) → sniff → freeze →
  JSON social graph (Ned's 70M-line Twitter export) → parquet round trip →
  hive-partitioned folder with `EXPLAIN` pruning proof.
- **Activities**: run `/wrangle/csv/{name}`, `/wrangle/csv/{name}/frozen`,
  `/wrangle/graph/{name}`, then generate your own files with
  `app/scripts/generate_samples.py` and repeat.
- **Accessibility**: every endpoint returns a JSON-safe view (schema, head,
  count) so "did it work?" is visible without a GUI; the frozen-CSV cell is
  paste-ready SQL.

## Session 2 — The relational API and interop (level: analyze)

- **Goal**: compose queries as lazy relations and prove interchange between
  pandas, polars, and Arrow — one result, three dialects, back into SQL.
- **Evidence (EES)**: a pipeline composes without materialising (source is a
  relation; `explain()` shows the deferred plan); the same rows come back via
  `.df()`, `.pl()`, and `.arrow()`; a registered DataFrame answers SQL.
- **Prior knowledge activated**: DataFrame method chaining; `JOIN`.
- **Sequence**: "SQL doesn't compose, relations do" → lazy filter/aggregate →
  peek before materialising → three-dialect round trip →
  `con.register(...)` back into SQL ("scope magic" tip).
- **Accessibility**: the three-dialect result is tabular; the tip about magic
  variables is stated as a caveat, not a gotcha.

## Session 3 — Inspection before execution (level: analyze)

- **Goal**: read the engine's mind with `DESCRIBE`, `SUMMARIZE`, `EXPLAIN`,
  and settings before trusting a query.
- **Evidence (EES)**: an `EXPLAIN` plan contains the `GROUP`; `SUMMARIZE`
  profiles a column (min/max/avg/quartiles); `duckdb_settings()` reports
  threads/memory/order.
- **Prior knowledge activated**: indexes-free analytical querying; what a
  query plan is.
- **Sequence**: `DESCRIBE` schema → `SUMMARIZE` profile → `EXPLAIN` named
  metrics → engine facts vs the (absent) server. **Known issue**: on the
  pinned 1.5.5, `SUMMARIZE` returns min/max/avg as text columns and
  `duckdb_settings()` may expose `value` rather than `setting_value` — the
  lesson is to treat inspect results as strings until typed.

## Session 4 — Extensions (level: analyze/challenge)

- **Goal**: install-on-demand extensions; tell first-party from community,
  and degrade gracefully when one is unavailable.
- **Evidence (EES)**: `spatial` computes great-circle-agnostic distances from
  Head Office to the four other places; `h3` returns cells *or* a graceful
  "unavailable" note (platform/version-dependent — expected in some runners).
- **Prior knowledge activated**: nothing beyond "a database has functions".
- **Sequence**: core extension (spatial, everywhere) → community extension
  (h3, dependent) → the failure mode as a *teaching moment*.
- **Accessibility**: the demo uses the dataset's own `places` (5 Australian
  sites) so geography is familiar and the output meaningful.

## Session 5 — DuckDB in the browser (level: expose/challenge)

- **Goal**: see the same engine run fully client-side via duckdb-wasm.
- **Evidence (EES)**: the embedded 12-row employees CSV answers a
  `GROUP BY job_title` query in the browser with no server.
- **Prior knowledge activated**: nothing; it is a drop-in playground.
- **Sequence**: load lib → register a tiny CSV in-memory → query → note the
  WASM build ships its *own* DuckDB version (teaching: version pinning).
- **Accessibility**: a textarea with a working default query; results render
  inline.

## R reference sessions (optional, no R in this stack)

- `r/duckplyr_lesson.R` — dplyr grammar targeting DuckDB's native API.
- `r/dbplyr_lesson.R` — dplyr over DBI; `show_query()` reveals the generated
  SQL.
- Both read the **same** `data/analytics.duckdb` — reinforcing the "one file
  shared by every client" theme.

---

## Assessment evidence (unit-wide)

| what | where | pass bar |
|---|---|---|
| Sniff + freeze | `/wrangle/csv/*` + monkeypatch your own CSV | inferred count = 200; frozen SQL is explicit |
| JSON graph | `/wrangle/graph/*` | degrees/vertices returned; nested `follows[]` read without a pipeline |
| Parquet fidelity | `/wrangle/parquet/*` | round-trip row count equals source |
| Hive + pruning | `/wrangle/hive/*` | partition columns auto-discovered; EXPLAIN shows pruning |
| Lazy composition | `/relations/api` | source is a relation; nothing materialised too early |
| Three-dialect interop | `/relations/interop`, `/relations/polars` | same rows via df/pl/arrow; a registered frame answers SQL |
| Inspect-first | `/plans/*` | EXPLAIN contains GROUP; SUMMARIZE profile sane |
| Extensions | `/extensions/*` | spatial returns distances; h3 either cells or honest note |

## Teacher decisions baked in

- Run `app/scripts/generate_samples.py` before class so `data/files/`
  exists — several wrangle endpoints read the files it writes.
- Pytest currently carries known reds (see `tests/` and the engine-quirk
  fixups); the notebooks/tests that matter for class are the structural ones
  (DESCRIBE, EXPLAIN, relations, parquet round trip, spatial).
- The h3 failure-on-macOS is *part of the lesson*, not a bug to hide.
- WASM ships its own DuckDB — mention the pinning caveat when the browser
  numbers differ from the CLI.