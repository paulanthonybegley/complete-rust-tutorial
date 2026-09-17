# education.md — framing notes (Practical Applications of DuckDB)

## Why this unit exists

Most learners meet DuckDB through a blog post ("it's SQLite for data") and
underuse it. This unit gives them the *practical* surfaces the "Getting
Started with DuckDB" authors push: messy-file wrangling, lazy relations,
inspect-before-execute, extensions, and a browser client. None of it needs a
server or a schema migration — which is exactly the point of the embedded
engine.

## Pedagogical spine (from OLD.txt: a lesson plan is a system)

- **Understand → analyze → challenge → build** across the five sessions
  (wrangle → relations → inspect → extensions → wasm).
- **EES**: every session ends in a JSON-visible artifact (schema, plan,
  counts, distances) — reproducible for grading.
- **One-file theme**: the same `analytics.duckdb` is opened by the CLI
  container, the FastAPI app, and the R lessons. "No daemon, no port."
- **Determinism**: `seed.sql` is idempotent and the samples use fixed
  random seeds (`Random(2026)`, `Random(4242)`), so exercises hold stable
  numbers (200 fitbit rows, 60 JSON users, 6,000 sales, 80 employees).

## What NOT to teach first

- Do **not** start with extensions or wasm. Wrangling and relations are the
  lesson; extensions and wasm are the payoff.
- Do **not** hand-wave the "10,000 rows fits in the engine" claim — run a
  query, show it is instant, then talk about why.
- Avoid promising exact `SUMMARIZE`/settings types on every DuckDB point
  release (the pinned 1.5.5 returns text-typed profiles) — teach
  "outputs are strings until cast".

## Known issues to acknowledge, not hide

- Several wrangle/API tests are currently **red**; the ownership is a known
  engine-quirk fixup (JSON `UNNEST` of `follows[]` needs
  `list_transform(f -> f.user_id)`; sniffer CSV column inference; the
  `client` fixture's `data_dir` is dead code). Class should rely on the
  endpoints that pass, and treat a red as a *lesson* about dialect drift.
- `generate_samples.py` must run first — `data/files/` does not ship
  populated.
- `h3` needs a one-time network install and often fails on macOS arm64 —
  that is the graceful-degradation demo.

## Grading posture

Reproduce > critique > build. Sessions 1-4 *reproduce* the episode's tricks;
the build (hive folder, your own sample, your own wasm query) is where the
discrimination happens.

## Running the unit

```bash
cd app
.venv/bin/pip install -r requirements.txt
.venv/bin/python scripts/generate_samples.py          # data/files/*.csv, *.jsonl
.venv/bin/uvicorn duckdb_app.main:app --port 8085      # unit app
# CLI side-car (optional, shares the same file):
cd .. && docker compose up -d --build --wait
docker compose exec duckdb duckdb /data/analytics.duckdb
```

## Extension ideas

- Bring a real CSV from class (a class roster, a fitness export) and redo
  Session 1 on it — the sniff/freeze loop is the transfer test.
- Point the R lessons at the same file and diff a `dbplyr` `show_query()`
  against the app's SQL.
- Replace the spatial demo's SQL with `ST_MakePoint` cartography and plot
  the places on a choropleth.