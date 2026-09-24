# Unit plan: "One Postgres to rule your whole stack"

*Built from the inputs in `OLD.txt` — this is a lesson system, not a prompt. Every section below is an
explicit design input. Change an input and the exercises or assessments below should change with it.*

The hands-on material is a single Spring Boot 4 + Thymeleaf + HTMX app where every feature page is a
live demo backed by real SQL, plus a Postgres 16 instance (PostGIS, pgvector, pg_trgm) seeded for the
lessons. See the repo `README.md` for how to start it.

Companion files:

- `lessons/exercises.md` — student worksheet
- `lessons/solutions.md` — answer key (give to students only after the assessable work is due)

---

## 1. Learning goal

> Learners can explain why one PostgreSQL instance can replace MongoDB, Redis/RabbitMQ, Elasticsearch,
> a vector database, a GIS service, a time-series database and a data warehouse — and can demonstrate
> each replacement with working SQL against a real schema.

Success looks like: a learner, given a table and a job to do, writes the correct index/keyword/operator
for the job (GIN + `@>`, `SKIP LOCKED`, `tsvector` + trigram, HNSW `<=>`, ST_DWithin/GiST, partitioning
+ BRIN, materialized view, RLS policy) and can name the service that replacement makes unnecessary.
The top-level "caveats" outcome is equally assessed: learners can state at least two cases where
Postgres is the wrong tool.

## 2. Learner profile

- **Who:** early-career engineers and CS undergraduates (mixed 2nd/3rd year), comfortable using git and
  Docker, and able to read basic SQL `SELECT`/`GROUP BY`/`JOIN`.
- **Strengths:** curious about architecture; already opinionated about tools (good for debate in Lesson 1).
- **Stretch areas:** no Postgres admin/ops exposure (indexes, `EXPLAIN`, extensions), no experience with
  non-relational data models, no security-privilege experience (`SET LOCAL`, grants, policies).
- **Pacing:** the class is ~18–24 learners. Sessions are 60 minutes with a hard 10-minute buffer for the
  "gotcha" moments that the demo deliberately surfaces.
- **Motivation:** most learners are building side projects; the "delete a microservice" narrative is the hook.

## 3. Prior knowledge (assumed and checked)

Assumed (pre-checked in Lesson 1's warm-up):

- Can write `SELECT ... WHERE ... GROUP BY ... ORDER BY`, and understand a primary key/foreign key.
- Can run `docker compose up -d`, read a terminal, and open a web page on `localhost:8080`.
- Knows what a message broker, a search engine, and a document database are *conceptually* (even if never
  used).

The system must not re-teach these. Lesson plans below assume them, and the Lesson 1 pre-check (see
"Assessment evidence") confirms them. If the pre-check fails for a subset, the "Accessibility & supports"
section lists the fix.

Watched/prior-art to reference: the app is inspired by The Coding Gopher's
[#](https://www.youtube.com/watch?v=TdondBmyNXc) video "I replaced my entire stack with Postgres". Watching
it once before Lesson 1 is optional and not required for the exercises.

## 4. Lesson sequence

| # | Lesson | Replaces | Demo app page | Time |
|---|--------|----------|---------------|------|
| 1 | Foundation: ACID + the extensibility argument | (the why) | `/acid` | 60m |
| 2 | Documents: JSONB + GIN | MongoDB | `/jsonb` | 60m |
| 3 | Work: job queues with `FOR UPDATE SKIP LOCKED` | Redis / RabbitMQ | `/queue` | 60m |
| 4 | Finding: full-text, typo-tolerant, and semantic search | Elasticsearch + vector DB | `/search`, `/vector` | 60m |
| 5 | Space & time: PostGIS + partitioning/BRIN | GIS + time-series DB | `/geo`, `/timeseries` | 60m |
| 6 | Trust at scale: materialized views + Row Level Security | warehouse + authz middleware | `/analytics`, `/rls` | 60m |
| 7 | Capstone: "delete a service" | all of the above | new code (see exercise C) | 90m |

Lesson 7 is a design-and-build task: each pair picks a feature from `/caveats`-safe territory and *proves*
a replacement in the app's schema with real SQL + a live query plan. It produces the unit's summative
assessment.

The sequence is ordered from "why this is safe at all" (ACID) to "gestalt arguments" (mixing queue +
search + analytics in one place) so that each lesson's demo reuses objects the previous lesson already
explained (`jobs`, `articles`/`documents`, `events`, `orders`).

## 5. Learning activities

Uniform activity rhythm per lesson ("**A–D**") so pacing is predictable:

- **A. Demo, 12m** — the instructor plays with the app page, and learners predict the SQL first, then
  the real query is shown; a one-line "what it replaced" is stated.
- **B. Pairs, 20m** — work `lessons/exercises.md` items for that lesson (Check + Practice) against the
  running Postgres. Pairs self-verify against SHOWED results rather than the answer key.
- **C. Whole-group, 10m** — the class compares solutions; the instructor surfaces the "gotcha" specific to
  that lesson (each is pre-built into the demo — see the per-lesson notes below).
- **D. Exit check, 5m** — the quick formative check is answered; the rest is homework.
- **Homework, 15–30m** — the Apply item for that lesson plus a preview of the next demo's SQL.

Extra dynamics: Lesson 1 opens with a "delete the service" whiteboard debate; Lesson 6 runs a
live "should this be a broker?" refusal drill driven by the `/caveats` content.

## 6. Assessment evidence

**Formative (every lesson):** the Exit check (one short SQL or a one-sentence prediction). Recorded
pass/fail only.

**Summative (Lesson 7 capstone):** a rubric-scored build:

1. Picks a service the team claims Postgres can replace for their side project (2 pts)
2. States the Postgres mechanic and writes the exact index/operator (3 pts)
3. Demonstrates with a live query and an `EXPLAIN (ANALYZE, BUFFERS)`-style plan (2 pts)
4. Names the honest limit + the "when not to" rule from `/caveats` (2 pts)
5. Commits a working change to this repo's schema (seed/table/index) or a tested query script (1 pt)

The whole "assessment evidence" for the unit IS the capstone; every earlier lesson's exit check simply
feeds the coachable evidence — none are graded to the capstone rubric.

## 7. Output requirements

- Deliverables: `lessons/exercises.md` (student) and `lessons/solutions.md` (answer key). Markdown, GitHub
  rendering, no HTML.
- Every code/output block fenced in ```sql / ``` and every student-runnable step must:
  - run against the seeded database; and
  - use the non-superuser role `demo_app` (psql: `docker exec -it postgres-stack-demo psql -U demo_app -d postgres`).
- Exercises must state the exact demo page and exact table(s) used, so a learner who is stuck on SQL can
  still make progress with the UI.
- No authorization or data-write steps outside of what `demo_app` is granted (`SELECT` on all, and the
  RLS-constrained inserts in Lesson 6).
- Answer-key SQL must have been executed against the seeded schema before release (it has; see
  `lessons/solutions.md` "verified" notes).
- Duration, difficulty (Check/Practice/Apply), and any marks are printed on each item.

## 8. Accessibility & supports

- **Reading/motion:** all demos are server-rendered HTML; the dark CSS theme is readable at WCAG-ish
  contrast; nothing is time-critical on the page (the queue page refreshes itself but never requires speed).
- **Scaffolding:** each exercise lists the demo URL and the table name first. Practice items begin in
  Check form, so a student who can "copy-check" can still enter Practice partially solved.
- **Language/precision:** SQL terms are defined inline on first use (e.g., "GIN = generalized inverted
  index"). No SSO/accounts are needed.
- **Pairs:** exercises instruct "driver/navigator" pairing; each learner rotates once per lesson.
- **Extra time:** the whole-group share (C) doubles as a re-teach; a learner can do homework items at own
  pace and both Apply items give alternate difficulty ("stretch" lines).
- **The one real hazard** is the RLS write exercise (Lesson 6 Practice 6-2): it intentionally triggers a
  DB error. The demo page catches it; running bare `psql` will hard-error by design — the worksheet warns
  the learner to expect that and reads it as the *evidence of success*.

## 9. Teacher decisions (defaults, adapt or override per class)

- **Grouping:** pairs chosen randomly each lesson (name picker). Capstone pairs are self-selected.
- **Debt vs. discovery:** in Lessons 1–3 the SQL is *revealed* after prediction; from Lesson 4 on the SQL
  is *elicited first* (worked from the prompt: "here is the tool, write the half-formed query").
- **Homework load:** Apply items are optional-but-encouraged until Lesson 7, where the score is required.
- **What the instructor must never skip** (the "gotchas" the demo installs on purpose; all are real issues
  encountered when the lab was built and are the richest teaching moments):
  1. Reserved-word GUC names bite — `SET LOCAL app.current_user` fails to parse (Lesson 6).
  2. `REFRESH MATERIALIZED VIEW CONCURRENTLY` needs ownership, not just table grants (Lesson 6).
  3. JSONPath's `?` (`attributes @?`) collides with prepared-statement placeholder parsers (Lesson 2).
  4. `word_similarity(a, b)` argument order is not symmetric (Lesson 4).
  5. An RLS-violating INSERT aborts the whole transaction in PG 16 (Lesson 6).
- **Environment:** students use their own machines with the repo checked out; the instructor's machine
  runs the shared DB via docker compose. Both are covered by "Run it" in the README.

---

## Per-lesson focus and the "gotcha" to lead the debrief with

| Lesson | Core mechanic | Pre-built gotcha for the debrief |
|--------|---------------|---------------------------|
| 1 | Extensibility: `CREATE EXTENSION` turns Postgres into other products | Types live in the DB, so "the wrong type for the job" is a real cost |
| 2 | JSONB containment `@>` + the GIN index; JSONPath | `@?` vs question-mark placeholder parsing |
| 3 | `FOR UPDATE SKIP LOCKED` claim loop, `attempts`/`max_attempts`, payload `"fail": true` rejection | Retry-exhaustion is a *failed* job, not a hung queue |
| 4 | `tsvector`/`tsquery` then `word_similarity` fallback; HNSW `<=>` cosine | `word_similarity(a,b)` is asymmetric |
| 5 | Partition pruning + BRIN `pages_per_range`; GiST bounding boxes | BRIN needs tuning (`pages_per_range`) or it's a worse scan |
| 6 | `REFRESH ... CONCURRENTLY`; `SET LOCAL app.uid` + policies | MV refresh requires ownership; RLS-violations abort the transaction |

The capstone (Lesson 7) deliberately reuses at least two of these gotchas, because knowing them is the
difference between "it runs" and "it runs in production".