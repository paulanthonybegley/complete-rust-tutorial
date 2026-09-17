# education.md — framing notes (Analytics from Python DataFrames with DuckDB)

## Why this unit exists

Pandas learners vote with their fingers: when a question outgrows their
DataFrame chaining, they either bolt on a `for` loop or copy data into a
database they have to learn to operate. This unit removes the second hump —
DuckDB queries the DataFrame *already in memory*, so "just write SQL over
it" costs nothing to adopt. The lecture's four moves (scope magic,
inspection, joins, export) are the exact transfer skills a pandas user needs.

## Pedagogical spine (from OLD.txt: a lesson plan is a system)

- **Understand → analyze → challenge** across the six sessions, ending each
  in a JSON artifact (a plan, a profile, an equality flag).
- **EES**: every session's endpoint is mirrored by a pytest test — the
  instructor's "show it works" checklist is literally `app/tests/`.
- **"Same answer as pandas" as proof**: scope-magic, registered, and join
  endpoints all assert the SQL result equals the pandas result. That is the
  course's central trust device.
- **Determinism**: `seed.sql` is idempotent and hash-based; with the pinned
  `duckdb==1.5.5` the numbers below are stable.

## What NOT to teach first

- Do **not** teach `date_trunc` as theory — teach the *revenue-trend* query
  and let the bucketing appear.
- Do **not** start with parquet — the value of COPY is only felt after
  Lesson 1-3 made em-something-feel-like-a-database.
- Avoid promising exact statistical output of `SUMMARIZE` across DuckDB
  releases (schema-min/max/avg come back as strings on 1.5.5). Teach
  "profiles are text until typed."

## Known issues to acknowledge, not hide

- The suite's numeric/version assertions are **version-sensitive**: when the
  local driver drifts off the pinned 1.5.5, hash-derived seed numbers and
  `version()` checks turn red (a large chunk of the current pytest-red list
  is exactly this). The pin, not the code, is the fix — keep the venv on
  `duckdb==1.5.5`.
- `/parquet/{table}` writes to a fixed `/tmp/_duckdb_edu.parquet` — fine for
  a demo, brittle on locked-down machines (pass a temp path in exercises).
- There is no README/setup scaffold (this doc + `lesson_plans.md` are the
  first docs); the way to run is `uvicorn duckdb_edu.main:app` from `app/`.

## Grading posture

Reproduce > explain > extend. Lessons 1-4 *reproduce* the lecture's dotted
path; the course-query session asks them to *extend* the four patterns to a
new question; the concurrency lesson is the optional *challenge*.

## Running the unit

```bash
cd app
.venv/bin/pip install -r requirements.txt     # duckdb==1.5.5, pandas, fastapi...
.venv/bin/uvicorn duckdb_edu.main:app --port 8084
# verify: curl localhost:8084/magic        (scope-magic demo)
# CLI side-car (optional) shares the same file:
cd .. && docker compose up -d --build --wait
docker compose exec duckdb duckdb /data/analytics.duckdb
```

## Extension ideas

- Swap the seed `employees` for the cohort's own CSV → ask them to answer
  "avg salary by department" with a `register()` + SQL — the golden
  transfer test.
- Add a `date_trunc('month', ...)` variant and top-10 window in
  `revenue-trend` as a stretch exercise.
- Compare `/threads` against running the same query through `multiprocessing`
  to make the "one file, many connections" point concrete.