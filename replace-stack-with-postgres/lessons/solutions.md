# Solutions — "One Postgres to rule your whole stack"

Answer key. Verified against the seeded schema on this repo's stack (Postgres 16.15 + PostGIS + pgvector
+ pg_trgm; Postgres 16 policy behaviour: an RLS-violating statement errors and **aborts the current
transaction**). Give to learners only after Lesson N's assessable work.

Difficulty tags match the worksheet: **(C)** Check, **(P)** Practice, **(A)** Apply.

---

## Lesson 1 — Foundation: ACID + extensibility

**1-1 (C)**
- a) `SQL`
- b) `Docker`
- c) `concepts`

**1-2 (C)**
- a) Mongo-style flexible documents → **none of these directly** (it's `JSONB`, a *built-in* type, not an
  extension) — this is the intended trap. The four listed cover: b) `postgis`, c) `vector`, d) `pg_trgm`.
- b) `postgis`
- c) `vector`
- d) `pg_trgm`

**1-3 (P)**
```sql
\dt              -- tables in public
SELECT extname, extversion FROM pg_extension ORDER BY 1;
```
One-sentence version: the engine ships a generic storage/transaction core and lets you bolt on new types,
operators and index *access methods* — so "documents" (`JSONB`), "queue" (a table + `SKIP LOCKED`) and
"time-series" (partitioned tables + BRIN) are all just data shapes over one ACID core.

**1-4 (A)** Marked on the capstone; expected evidence: a data-shape line, an access-pattern line, and a
durability/consistency line (the table + index that *would* hold it).

---

## Lesson 2 — Documents: JSONB + GIN

**2-1 (C)** **B — `@>` (containment) on a GIN index.** (`?` checks a top-level key, `->` extracts — and
GIN, not btree, is what makes nested `jsonb` fast.)

**2-2 (C)** The **GIN index first** — the planner turns `attributes @> '...' ` into an index lookup that
returns row ids, then touches the table rows only for those ids.

**2-3 (P)**
```sql
-- a) Android phones
SELECT name, price FROM products
WHERE attributes @> '{"os":"Android"}'::jsonb;          -- Pixel 8, Galaxy S24

-- b) brands
SELECT attributes->>'brand' AS brand, count(*) AS n
FROM products GROUP BY 1 ORDER BY n DESC;               -- Apple 3, then singles

-- c) RAM >= 16 (value is a text key, cast it)
SELECT name, attributes->'specs'->>'ram_gb' AS ram
FROM products WHERE (attributes->'specs'->>'ram_gb')::int >= 16;
-- MacBook Air M3 (16), ThinkPad X1 (32), Dell XPS 13 (16)

-- d) stretch: array containment via the same @>
SELECT name FROM products
WHERE attributes @> '{"specs":{"ports":["HDMI"]}}'::jsonb;  -- LG UltraFine 27
```

**2-4 (P)** The GIN index `idx_products_attributes` maps every key/value pair to its row id, so a nested
predicate never scans all rows; confirm with:
```sql
SELECT indexname, indexdef FROM pg_indexes WHERE tablename = 'products';
```

**2-5 (A)** Risk: unconstrained shape — a 10-deep document with a surprise array, or a `"price"` string in
one row and a number in another, silently corrupts app logic. Mitigation shown by the app: all queries
are containment/JSONPath-based and the seed data is consistent; a real mitigation would add
`NOT VALID` CHECK constraints on shape (`jsonb_typeof`) — exactly the "the type for the job" caveat the
`/acid` page raises.

---

## Lesson 3 — Work: queues with SKIP LOCKED

**3-1 (C)** Two workers can both `SELECT` the same `pending` row before either updates it, so both claim
it and the job runs twice (no at-least-once safety).

**3-2 (C)** Claim sequence: `SELECT id WHERE status='pending'` → `FOR UPDATE SKIP LOCKED` → `LIMIT 1`
(inside a CTE) → `UPDATE ... SET status='running'`.

**3-3 (P)**
```sql
WITH next AS (
  SELECT id FROM jobs
  WHERE status = 'pending'
  ORDER BY created_at
  LIMIT 1
  FOR UPDATE SKIP LOCKED          -- lock THIS row, skip rows other workers hold
)
UPDATE jobs SET status = 'running', attempts = attempts + 1, started_at = now()
FROM next WHERE jobs.id = next.id
RETURNING id, kind, attempts;
```
Run twice: the first claims the oldest pending row; the second usually returns **0 rows** — workers
(3 threads, 150 ms poll) already drained the seeded pending jobs or another between your two runs claimed
the row, and the 3 idle-seeded pending jobs finish quickly. Watch `/queue` flip `pending → running → done`.

**3-4 (P)**
- a) `max_attempts` on the row; the seed uses **3** (column default is `3`).
- b)
  ```sql
  SELECT status, count(*) FROM jobs GROUP BY status ORDER BY status;
  ```
- c) The worker calls `queue.fail()` when attempts are exhausted: payload `"fail": true` → safer/retry
  loop that **stops** at `max_attempts`. It does not retry forever — that's the "dead-letter" behaviour
  (reads `QueueService.fail`/`QueueWorker`). The exact detail: `wantsToFail` is `payload.fail == true`
  (a JSON boolean — `"yes"` is falsey) and `fail()` leaves the row `failed` when `attempts >=
  max_attempts`.

**3-5 (A)** ACID gives: atomicity (a crash never leaves a half-claimed job), isolation (SKIP LOCKED rows
are invisible to other workers), and durability (a worker crash after `pending→running` is a row you can
requeue — the retry counter is *recovery evidence*). An in-memory list loses all of these on restart.

---

## Lesson 4 — Finding: full-text, fuzzy, semantic

**4-1 (C)** a) trigram `word_similarity` b) `tsvector`/`tsquery` c) HNSW vector `<=>`.

**4-2 (P)**
```sql
SELECT title, ts_rank(search_vector, websearch_to_tsquery('english', 'postgres jsonb')) AS rank
FROM articles
WHERE search_vector @@ websearch_to_tsquery('english', 'postgres jsonb')
ORDER BY rank DESC;

SELECT ts_headline('english', body, websearch_to_tsquery('english', 'postgres jsonb'),
                   'StartSel=<mark>, StopSel=</mark>, MaxWords=25')
FROM articles WHERE search_vector @@ websearch_to_tsquery('english', 'postgres jsonb');
```

**4-3 (P)** `word_similarity(query, text)` — the **first** argument is the (short, misspelled) query, the
second is the candidate text. The unrepaired query returns ~`0.25` for the "pgvector" articles; the
swapped form matches the "tsvector" full-text articles much higher. So:
```sql
SELECT word_similarity('tsvetor', title) AS correct_order  -- query first
FROM articles ORDER BY 1 DESC;
```

**4-4 (P)**
```sql
SELECT title, 1 - (embedding <=> text_embedding('the vector search filters by tag and author')) AS sim
FROM documents ORDER BY embedding <=> text_embedding('the vector search filters by tag and author')
LIMIT 5;

SELECT title, 1 - (embedding <=> text_embedding('the vector search filters by tag and author')) AS sim
FROM documents WHERE tag = 'ai'
ORDER BY embedding <=> text_embedding('the vector search filters by tag and author')
LIMIT 5;
-- top hit for the phrase is the "Hybrid vector and relational filter" document (~0.84)
```
The `<=>` operator is cosine **distance**; `1 - distance` is similarity. Function `text_embedding(text,
dims=32)` is the PL/pgSQL trigram-hash embedder seeded in `init.sql`.

**4-5 (A)** The vector result passes through the same SQL predicate engine as the rest of the row, so both
the similarity ranking and the author/tag/date filter happen in one scan through one index — the hybrid
plane of the `/vector` page. A two-system approach ships the candidate list over the network, then
re-validates against a second store — two round trips, two cache layers, and a consistency hole between
them.

---

## Lesson 5 — Space & time

**5-1 (C)** BRIN = "one min/max per disk block, skipping whole blocks". GiST = "bounding box, discard
far-away rows first".

**5-2 (P)**
```sql
-- a) within 4 km of (51.5074, -0.1278) → 19 shops
SELECT count(*) FROM coffee_shops
WHERE ST_DWithin(location, ST_MakePoint(-0.1278, 51.5074)::geography, 4000);

-- b) 3 nearest (k-NN via the GiST index)
SELECT name, round((ST_Distance(location, ST_MakePoint(-0.1278, 51.5074)::geography)/1000)::numeric, 2) AS km
FROM coffee_shops
ORDER BY location <-> ST_MakePoint(-0.1278, 51.5074)::geography
LIMIT 3;   -- Department of Coffee (~0.15 km) etc.

-- c) stretch: best-rated within 2 km, rating first then distance (<-> wins ties)
SELECT name, rating,
       round((ST_Distance(location, ST_MakePoint(-0.1278, 51.5074)::geography)/1000)::numeric, 2) AS km
FROM coffee_shops
WHERE ST_DWithin(location, ST_MakePoint(-0.1278, 51.5074)::geography, 2000)
ORDER BY rating DESC, location <-> ST_MakePoint(-0.1278, 51.5074)::geography;
```

**5-3 (P)**
```sql
SELECT count(*) FROM events
WHERE device = 'device-42'
  AND occurred_at >= '2025-03-01' AND occurred_at < '2025-04-01';

EXPLAIN (ANALYZE, BUFFERS)
SELECT count(*) FROM events
WHERE occurred_at >= '2025-03-01' AND occurred_at < '2025-04-01';
-- Partition pruning: only events_2025_03 is scanned ("of 7 partitions, matching 1")
```
With `SET enable_seqscan = off;` and a device filter in addition to the range, the plan shows
`Index Scan ... Using idx_events_brin` on the pruned partition (or a bitmap join over blocks) — the BRIN
index stores per-block min/max and skips blocks whose ranges cannot match. `pages_per_range = 32` was the
switch in `init.sql`.

**5-4 (A)** The seeding only creates partitions through `2025-06` (plus `events_default`). New months are
hand-written DDL — a real time-series system auto-creates partitions (e.g. a scheduled `pg_cron`-style
branch or `partition pruning` on a far-future default). The honest "/caveats" ceiling: massive write
volume and self-managing retention are better served by a purpose-built store.

---

## Lesson 6 — Trust at scale

**6-1 (C)** `REFRESH ... CONCURRENTLY` takes a lighter lock: readers keep their old snapshot while the
new data replaces rows wholesale; plain `REFRESH` locks out concurrent readers. The unique index
(`idx_mv_daily_sales` on `(ordered_on, product)`) lets Postgres match old rows to new rows during the
hot swap.

**6-2 (C)** Visibility is decided by `owner_id` against the session's `app.uid`
(`USING (owner_id = current_setting('app.uid', true)::int)`), set per transaction by
`SET LOCAL app.uid = ...`. "FORCE ROW LEVEL SECURITY" is on so it bites even for the `demo_app` role.

**6-3 (P)**
```sql
SELECT product, SUM(units * unit_price) AS revenue
FROM mv_daily_sales
GROUP BY product ORDER BY revenue DESC LIMIT 1;
```
b) The `orders` version aggregates 1,100 rows on each call; the MV version reads a hand-sized
precomputed table with a unique index — that's the whole argument: stored aggregation for repeated
dashboard queries.

**6-4 (P)**
```sql
BEGIN;
SET LOCAL app.uid = 2;
SELECT count(*) FROM private_notes;   -- 4 (bob owns 4 notes)
SET LOCAL app.uid = 3;
SELECT count(*) FROM private_notes;   -- 3 (carol owns 3)
SET LOCAL app.uid = 2;
INSERT INTO private_notes (content, owner_id) VALUES ('borrowed', 3);  -- ERROR
ROLLBACK;
```
The error "new row violates row-level security policy" means: the `WITH CHECK` clause evaluated
`owner_id (3) = current_setting('app.uid') (2)` and refused the row. In PG 16 the statement **aborts the
transaction** — the following commands would error with "current transaction is aborted" until you
`ROLLBACK`. That abort is the point: RLS is not an app-layer filter that silently drops rows; the write
is *rejected at the engine*.

**6-5 (A)**
- a) `ALTER MATERIALIZED VIEW mv_daily_sales OWNER TO demo_app;` — `REFRESH` and `REFRESH ... CONCURRENTLY`
  require the *owner* of the matview, not just `SELECT`-granted roles. Grants control reads/writes; the
  refresh mutates the stored snapshot itself, which is ownership territory. (Try `REFRESH ... CONCURRENTLY`
  as `demo_app` first to see the "must be owner" error.)
- b) `RlsService.addNote` runs its `SELECT` snapshot **before** the risky INSERT, then catches the
  violating write and returns the snapshot plus a "rejected" flag — so a DB that wiped the transaction
  state cannot make the page 500 (see `rls/RlsService.java` and the `partials/rls` view).

---

## Capstone (Lesson 7) — scoring rubric applied

| Criterion | What "full marks" looks like | Points |
|---|---|---|
| Mechanic stated | "sessions cache → unlogged table + expires_at sweep, btree on (expires_at)" | 2 |
| Index/operator exact | the exact `CREATE INDEX` / operator named, matches mechanics | 1 |
| Table + seed or demonstrable SELECT | runs against this repo; a real table + 10 rows or a proven query | 3 |
| Live, explainable | `EXPLAIN (ANALYZE, BUFFERS)` output or a count proof the replacement works | 2 |
| Honest limit + `/caveats` rule | a /caveats line quoted and applied to *their* choice | 2 |
| Commit | merged change is in the repo | 1 |

**Sanity-check queries for the idea bank:**
- sessions/cache shape: btree on `expires_at` + `DELETE ... WHERE expires_at < now()` — data volume stays
  tiny.
- geofence alert: `ST_DWithin` over `coffee_shops` inside a 10 s poll is the same loop as the queue.
- dead-letter audit: `SELECT * FROM jobs WHERE status='failed' AND attempts >= max_attempts;` (+ the
  `/rls`-style `count`).
- dashboard rollup: a `REFRESH MATERIALIZED VIEW CONCURRENTLY` mirrored on a per-day `orders` rollup.

**Closure message to the class:** you now know the ten-minute pitch of "delete the service" *and* the
three honest exceptions — the same SQL you wrote to prove it is the SQL you would ever need to revert on
a platform that can't make a given trade-off. That is the whole point of the `/caveats` page.