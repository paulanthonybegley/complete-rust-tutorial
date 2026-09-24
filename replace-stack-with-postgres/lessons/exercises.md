# Exercises: "One Postgres to rule your whole stack"

Work in pairs (driver/navigator, rotate each lesson). Everything runs against the seeded lab DB:

```bash
docker exec -it postgres-stack-demo psql -U demo_app -d postgres
```

The app (if the repo is running) gives you a UI for each lesson's demo. Exercises tell you the page and
the table to use. Difficulty tags: **(C)** Check, **(P)** Practice, **(A)** Apply. Marks are shown where
the rubric counts them.

Never supply data-writes outside what the worksheet asks; `demo_app` is intentionally limited.

---

## Lesson 1 — Foundation: ACID + extensibility (`/acid`)

### 1-1 (C) Warm-up (pre-check), 2 pts
For each line, which piece of prior knowledge does it test? (write `SQL`/`Docker`/`concepts`)

a) `SELECT count(*) FROM products;`
b) `docker compose up -d --build`
c) "what a document database is for"

### 1-2 (C) Extensibility, 2 pts
The lab DB runs these four extensions: `postgis`, `vector`, `pg_trgm`, `hstore`.
Which one maps to each "replacement" claim?

a) Mongo-style flexible documents
b) geospatial queries
c) embedding/similarity search
d) fuzzy/trigram text search

### 1-3 (P) Prove the "one database" claim, 4 pts
Without writing any new code, list the *tables* in `public` and the *extensions* present in one repository
(`\dt` and one `pg_extension` query). Then, in one sentence, say why the same engine can host a document
store AND a queue AND a time-series DB (hint: `/acid` page).

### 1-4 (A) Delete a (toy) service, 4 pts
Pick one microservice in your side project that, on paper, could be a Postgres table + index. Write the
"everything it needed" shopping list (data shape, the access pattern, the failure guarantee). Save it —
you'll use it in the capstone.

---

## Lesson 2 — Documents: JSONB + GIN (`/jsonb`, table `products`)

Products look like `{"brand":"Apple","os":"iOS","storage_gb":256,"specs":{"cpu":"A16 Bionic","ram_gb":6}}`.

### 2-1 (C) Containment, 2 pts
Which operator checks "attributes contains this whole structure", and on what index type does it run?
(A) `?` on a btree index (B) `@>` on a GIN index (C) `->` on a GIN index (D) `@>` on a GIN index

### 2-2 (C) Which run first, 1 pt
Given `attributes @> '{"os":"Android"}'`, what does Postgres *scan* first: the index or the table?

### 2-3 (P) Three real queries, 6 pts
Write (and run) three queries:

a) All phones that run Android, with name and price.
b) Count of products grouped by `brand`.
c) Products with at least 16 GB of RAM — the RAM value is inside `specs.ram_gb`.
d) Stretch: the one product whose `specs.ports` array contains `"HDMI"`.

### 2-4 (P) Why the index matters, 3 pts
Explain in one sentence why a `WHERE` on a deeply nested JSON value is fast here *without* writing any
planner hints. Then confirm the index exists:

```sql
SELECT indexname FROM pg_indexes WHERE tablename = 'products';
```

### 2-5 (A) A Mongo-shaped habit, 4 pts
The JSONB column is unconstrained. Give one real risk of storing arbitrary JSON here (any shape, any
depth) and one way the app mitigates it (hint: the `/jsonb` search page returns only products whose
structure you successfully queried).

---

## Lesson 3 — Work: queues with SKIP LOCKED (`/queue`, table `jobs`)

### 3-1 (C) Why not just `SELECT ... WHERE status='pending'`, 2 pts
Two workers run the same "claim a pending job" SQL at once. Without `SKIP LOCKED`, what can happen?

### 3-2 (C) Put the pieces in order, 2 pts
Order these steps of a safe claim: `UPDATE ... SET status='running'` → `SELECT id WHERE status='pending'`
→ `FOR UPDATE SKIP LOCKED` → `LIMIT 1` (a CTE is handy here).

### 3-3 (P) Claim like the worker does, 5 pts
Write the complete "claim one pending job" statement that: grabs one `pending` row, skips rows another
worker has locked, marks it `running`, bumps `attempts`, and sets `started_at`. Use a `WITH ... ` CTE.
Run it twice — explain why the second run returns nothing (you already claimed the only pending row... or
did the 3 seeded `pending` jobs finish while you were typing? Check `/queue`).

### 3-4 (P) Failure has teeth, 4 pts
A job carries `payload: {"fail": true}`. Watch `/queue` — eventually the job ends `failed` with
`last_error` filled and `attempts` = `max_attempts`.

a) How many attempts does the lab give it, and where is that stored?
b) What query reports per-status counts? Write it.
c) Stretch: the demo worker retries forever? Why or why not (read `queue`'s `fail()` logic in
   `src/main/java/com/example/postgresstack/queue/`).

### 3-5 (A) The ACID argument, 3 pts
The `/queue` page shows "workers" counters with no code on your side. In one paragraph: what does ACID
give a job queue that a plain in-memory list cannot?

---

## Lesson 4 — Finding: full-text, fuzzy, semantic (`/search`, `/vector`; `articles`, `documents`)

### 4-1 (C) Who does what, 2 pts
Match tool → mechanic: `tsvector`/`tsquery` / trigram `word_similarity` / HNSW vector `<=>`

a) typo-tolerant "close enough" matching
b) dictionary-aware ranked English search
c) approximate nearest-neighbour embeddings

### 4-2 (P) Full text, 4 pts
Write a query returning `title` and `ts_rank` for the best articles matching `postgres jsonb`, and a
second query that *highlights* the match in the `body`. (`websearch_to_tsquery('english', ...)` is your
friend.)

### 4-3 (P) The unsymmetric trick, 4 pts
This SQL is a lie about which result set it produces. Run it, then swap the two arguments and run again:

```sql
SELECT word_similarity('tsvetor', title) AS a, word_similarity(title, 'tsvetor') AS b
FROM articles ORDER BY a DESC LIMIT 3;
```

Which direction finds the "tsvector" articles? Say which argument is the (short, misspelled) query.

### 4-4 (P) Semantic, 4 pts
Return the 5 documents nearest to the phrase "the vector search filters by tag and author", with their
cosine *similarity*. Use `embedding <=> text_embedding('...')` (distance) and `1 - distance` for
similarity. Then add a `WHERE tag = 'ai'` filter — same statement. Write both queries.

### 4-5 (A) Hybrid vs. two systems, 3 pts
In one paragraph, why does filtering the vector search by `author` inside one SQL statement beat
"search in one database, then join in another"?

---

## Lesson 5 — Space & time (`/geo`, `/timeseries`; `coffee_shops`, `events`)

### 5-1 (C) Which index, 2 pts
Match: GiST (PostGIS) / BRIN (events) — "stores one min/max per disk block, skipping whole blocks" /
"wraps a shape in a bounding box, discards far-away rows fast".

### 5-2 (P) Radius + nearest first, 5 pts
From `51.5074, -0.1278` (Charing Cross):

a) How many shops are within 4 km? (Geographically correct: use `ST_DWithin` with `ST_MakePoint`)
b) Return the 3 nearest by the k-NN operator `location <-> ST_MakePoint(...)::geography`. Note the
   direction `<->` gives you the nearest-first ordering.
c) Stretch: the 3 best-rated within 2km, rated first then distance — which operator wins?

### 5-3 (P) Date slicing that doesn't scan the world, 5 pts
a) Query the events between `2025-03-01` and `2025-03-31` for device `device-42`, count rows.
b) Run it under `EXPLAIN (ANALYZE, BUFFERS)` — how many partitions did the planner touch?
c) Turn on `SET enable_seqscan = off;` and re-run — do you see `Index Scan ... Using idx_events_brin`?
   Describe what the BRIN index skipped.

### 5-4 (A) When the ceiling appears, 3 pts
The month partitions are declared in `init.sql`. Name one *explicit* limitation this lab has that would
argue for InfluxDB/Prometheus-style systems (hint: `/caveats`, and think about who writes the partition
for the year 2026 after the seeded `events_default`).

---

## Lesson 6 — Trust at scale (`/analytics`, `/rls`; `orders`, `mv_daily_sales`, `private_notes`)

### 6-1 (C) Refresh, 2 pts
Why `REFRESH MATERIALIZED VIEW CONCURRENTLY` and not plain `REFRESH`? And why does it need a unique
index (`idx_mv_daily_sales`)?

### 6-2 (C) RLS: who can see what, 2 pts
`private_notes` has 4 policies (select/insert/update/delete). From which property of the *row* does RLS
decide visibility, and how do we know who the session is? (one clause + one GUC name).

### 6-3 (P) The dashboard is a view, 4 pts
a) Sell the whole April? Answer from `mv_daily_sales`: which product made the most money in the last 90
   days' top product — write the query on revenue `SUM(units*unit_price)`.
b) Stretch: `EXPLAIN` a query on `orders` vs the same one on `mv_daily_sales` — which is cheaper and why?

### 6-4 (P) Act as another user, 4 pts
In one transaction (`BEGIN; ...; ROLLBACK;`):

```sql
SET LOCAL app.uid = 2;
SELECT count(*) FROM private_notes;   -- bob
```

a) How many notes does bob (`app.uid = 2`) see? (alice is 1, bob is 2)
b) Try `app.uid = 3` — how many?
c) Return to `app.uid = 2` and `INSERT` a note owned by `3`. Expected: a hard error. That error is not a
   bug — it is the point. Explain what "new row violates row-level security policy" means.

### 6-5 (A) The two "actually" gotchas, 3 pts
a) `REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_sales` fails for `demo_app` (try it). One keyword in
   `init.sql` fixed the schema so the lab's `/analytics` refresh works. Which one, and why is "ownership"
   a different privilege than "SELECT"?
b) RLS aborted your transaction in 6-4. The app's `/rls` page survives it. What did the Java side have to
   change from "return after the failed INSERT" to "not 500"? (Answer via the course, or read
   `RlsService.addNote`.)

---

## Capstone (Lesson 7) — "delete a service", 10 pts

Pick a service from your Lesson 1-4(A) shopping list (or a new one) and *prove* the Postgres replacement
inside this repo:

1. State the mechanic + the exact index/operator (rubric 2+1).
2. Write the `CREATE TABLE` + index + a 10-row seed `INSERT`, OR a tested `SELECT` that demonstrates the
   replacement (3 pts).
3. Demo it live with an explainable plan (`EXPLAIN (ANALYZE, BUFFERS)` or a query-count proof) (2 pts).
4. State the honest limit with `/caveats` and the exact "when not" rule (2 pts).
5. Commit it as `seed`/`table`/`index` or a tested query in your clone (1 pt).

Idea bank (all safe from the seeded schema): an unlogged `sessions` cache with expiry sweep (Redis-y),
a geofence alert built on the 22 coffee shops, a "dead letter" query for `jobs` after `max_attempts`,
a per-day analytics rollup REST-able query for `/analytics`-style dashboards, a per-user notes
"watchlist" using the RLS pattern.