# Solutions: The 7 Database Laws of Senior Backend Developer

Answers were verified against the app: `mvn -f app/pom.xml test` (10/10 green,
H2) and a live smoke run against Postgres in Docker.

---

## Lesson 1: Start With the Queries

**1.1 — queries → schema (one valid complete mapping):**

| Screen / report | Query it runs | Served by |
|-----------------|---------------|-----------|
| Catalogue, filtered by category | `select * from products where category = ?` | `idx_products_category` |
| Catalogue, sorted by name | `select * from products order by name` | `idx_products_name` |
| Customer order history | `select * from customer_orders where customer_id = ? order by created_at desc` | `idx_orders_customer_id` |
| Finance dashboard | `select year(created_at), month(created_at), sum(total_amount) from customer_orders group by …` | `idx_orders_created_at` |
| Order detail with lines | `select * from customer_orders … + join order_items on order_id` | `idx_order_items_product_id`, `order_items` PK |
| Checkout (lookup product) | `select * from products where id = ?` | `products` PK |
| Login/order for an email | `select * from customers where email = ?` | `idx_customers_email` (unique) |

**1.2 —** `monthlySales()` serves the finance dashboard. It groups on
`year(o.createdAt), month(o.createdAt)`, so `created_at` must stay indexed
(`idx_orders_created_at`). `findWithItemsByCustomer_Id…` (and the other
`@EntityGraph(attributePaths = "items")` methods) eagerly fetch `items`;
without the graph, one order with *n* lines produces *n+1* queries
(1 for the order + 1 per line → N+1).

**1.3 —** Access pattern: `select * from products where lower(name) like '%kettle%'`.
The existing `idx_products_name` helps a prefix search (`kettle%`) but a
`%kettle%` pattern cannot use a B-tree index efficiently — you would either add
a trigram (`pg_trgm`) index or (per Law 5) benchmark `tsvector` full-text
before reaching for a search engine.

## Lesson 2: Ask What Happens If the Data Is Wrong

**2.1 — the write order inside `placeOrder`:**
1. `findOrCreateCustomer` → possible `INSERT INTO customers` (guarded by unique `idx_customers_email`).
2. For each item loop: `decrementStockIfAvailable` → `UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?`.
3. On `decrementStockIfAvailable() == 0` → **fail here**, after earlier lines already wrote.
4. `orderRepository.save(order)` (+ cascaded `order_items` inserts).

The line that fails after earlier writes is step 3 when a later item is
out of stock — that is exactly what the rollback test asserts gets undone.

**2.2 —** After the failed order (`A × 5`, `B × 2`, B only has 1):
- A stock = **10** (its decrement was rolled back),
- B stock = **1** (never decremented),
- order rows = unchanged (0 new `customer_orders`, 0 new `order_items`).

Confirmed by `OrderServiceTransactionTest#failingLineRollsBackEveryEarlierStockDecrement`.

**2.3 —** The `product_name`/`unit_price` snapshot means the invoice is
historical truth: if the catalogue renames "Gel Pen" to "Gel Pen Pro" or
reprice it tomorrow, every *past* invoice stays as the customer was actually
charged. Deriving the line from the live product would silently rewrite
history (Law 2: "what happens if the data is wrong").

## Lesson 3: Don't Replace the Database Before Finding the Bottleneck

**3.1 —** 1 → `idx_orders_customer_id`; 2 → `idx_orders_created_at`;
3 → `idx_products_category`; 4 → `idx_order_items_product_id`.

**3.2 —** Verified against the running container: the plan for
`WHERE category = 'grocery'` is
`Index Scan using idx_products_category on products (… Index Cond: ((category)::text = 'grocery'::text))`.
A non-existent category value still uses the index (an index scan that returns
0 rows is cheaper than a seq scan the optimizer cannot rule out). Plan type:
**Index Scan**.

**3.3 —** (1) Measure: find the actual slow query (`pg_stat_statements` /
`EXPLAIN ANALYZE`). (2) Treat the query: is the WHERE/ORDER BY column indexed?
Is it a seq scan you can turn into an index scan? (3) Only if the query is
already walk-able and still too slow for the workload, discuss a new store —
and name the access pattern that forces it (see `schema-indexes.sql`).

## Lesson 4: Understand How It Scales

**4.1 —** Read-heavy, with a modest write hot-spot:
- majority read: `findWithItemsByCustomer_IdOrderByCreatedAtDesc` (order
  history screen) and catalogue `search` (filter/paginate);
- majority write: `decrementStockIfAvailable` + `customer_orders` insert in
  `placeOrder` (every checkout). It's the write with the contention because it
  conditionally updates the same `products` rows under load.

**4.2 —** 5 instances × 10 connections = **50** connections. When all 10 are
checked out, the 6th request waits in HikariCP's queue; `connection-timeout:
5000` caps that wait (5 s) before `SQLTransientConnectionException`. `minimum-idle`
is the floor that keeps a few warm to avoid cold-start latency.

**4.3 —** The naive alternative is
`orderRepository.findById(orderId)` (no `@EntityGraph`); with LAZY
`items`, reading the lines issues 1 query per line → an order with 20 lines
costs **1 + 20 = 21 queries** (the N+1). The `@EntityGraph` version is **1
query** with a JOIN.

## Lesson 5: Know When One Database Is Enough

**5.1 —** Queries a separate store *could* serve but that still ride Postgres:
catalogue search (`ProductSpecifications`, one built query), report aggregates
(`StatsService`), order reads. They stay because at this data size (hundreds of
rows) the indexed queries are sub-millisecond — adding a store adds a
consistency problem (stale cache, dual-write) with zero measured payoff.
"One database is enough" until a *measured* signal says otherwise.

**5.2 —** (b) Redis cache → trigger: hot read still costing more than
acceptable at high QPS (e.g. catalogue filters). (d) search engine → trigger:
`LIKE '%…%'`/full-text quality or latency at scale. (c) analytics warehouse →
trigger: long-running aggregates interfere with OLTP. (a) read replicas →
trigger: read QPS exceeds a single primary even with caching.
(In practice (a) can come before (d); the non-negotiable is that each step
sits behind a measured trigger.)

**5.3 —** It proves the persistence contract is one interface (`JpaRepository`)
with swappable dialects: the same code passes on H2-in-memory and Postgres, so
you are not locked to an exotic store — and it keeps the "one database" default
cheap to run. What H2-in-memory does NOT give you: durability (see Lesson 6) —
H2-mem's contract is speed and test isolation (`create-drop`), never
surviving a restart.

## Lesson 6: Don't Confuse Fast with Durable

**6.1 —** 1. Redis → **fast** (volatile by default). 2. H2-in-memory →
**fast** (nothing hits disk). 3. Postgres `synchronous_commit = off` → fast at
the cost of durabilty; committed data can be lost on crash/power loss (survives
in-memory until the WAL is flushed) → best described as *neither* for a strict
durability contract. 4. Postgres default (fsync + WAL) → **durable** (and fast
enough for a small dataset). 5. MySQL `memory` table → **fast**, not durable
(engine explicitly keeps data in RAM).

**6.2 —** "Postgres first appends the change to the WAL and fsyncs it; the
database can therefore *reconstruct any committed change even if a data page
was never written to disk*." A commit is durable the moment the WAL record is
fsynced — the much-later data-page write can be replayed from the WAL, so a
power loss that loses dirty pages loses nothing that was committed.

**6.3 —** Unit/integration tests: `application-test.yml` deliberately uses
H2-in-memory with `create-drop`. There, *fast + isolated* beats durable —
there is nothing to recover, and `mvn test` must complete in seconds without
Docker (see README). Durability only starts to matter where a restart must not
lose committed work — i.e. Postgres in real deployments.

## Lesson 7: Think About What Happens After Deployment

**7.1 —** The healthcheck runs `pg_isready -U dblaws -d dblaws` every 5 s (10
retries). The named volume `db-data` (mapped to `/var/lib/postgresql/data`)
survives `docker compose down`; without it, deleting the container destroys all
data. The env vars (`POSTGRES_DB/USER/PASSWORD`, defaults `dblaws`) must match
`application.yml` (`jdbc:postgresql://localhost:5432/dblaws`, user/pass
`dblaws`) or the app cannot connect.

**7.2 —** `ddl-auto: update` (1) silently alters the schema at startup — no
versioning, no review, no VCS diff of the migration; (2) can destructively
change columns (precision/type changes, index rebuilds) in production with no
rollback plan and no record of *when* the schema changed. `schema-indexes.sql`
is the manual, Flyway-style answer: one versioned file that applies the exact
index set, so the schema becomes repeatable and reviewable instead of emergent.

**7.3 (sample checklist — 12 items, tags in brackets):**
1. Verify `docker compose ps` shows `(healthy)` — *health*
2. `docker compose down` then `up` and confirm data is still present (volume) — *backup/health*
3. Export a manual dump: `docker exec dblaws-postgres pg_dump -U dblaws -d dblaws > backup.sql` — *backup*
4. Practice a restore from `backup.sql` into a throwaway volume — *backup*
5. Replace `ddl-auto: update` with `validate` — *migration*
6. Add versioned migrations (Flyway/Liquibase) seeded from `schema-indexes.sql` — *migration*
7. CI runs `mvn test` (H2) on every PR — *health*
8. CI applies migrations to a throwaway Postgres and runs the app — *migration/health*
9. Export a health/readiness endpoint and wire it to the orchestrator — *health*
10. Forward Prometheus/JVM + DB metrics (pool wait time, index scans) to the dashboard — *observe*
11. Log slow queries (`pg_stat_statements`) and keep a latency alert — *observe*
12. Keep `connection-timeout` below the orchestrator's kill timeout — *health/observe*

## Integration: Diagnose & Harden This App

**I.1 sample (two laws chosen):**

- Law 1 — evidence: `OrderRepository#monthlySales` is grouped in SQL; gap:
  the catalogue's *name* search has no matching index discussion —
  `%kettle%`-style filters would seq-scan (see lesson 1.3).
- Law 2 — evidence: `OrderService#placeOrder` + `decrementStockIfAvailable`
  roll back atomically (proven in `OrderServiceTransactionTest`); gap: the
  customer upsert is `find-then-save`; a concurrent duplicate relies solely on
  `idx_customers_email` and would surface as a raw `DataIntegrityViolation`
  instead of a clean error.

**I.2 sample proposed changes + tests:**
- For the name search: add a `pg_trgm` index (or `tsvector`), then a test
  asserting the no-filter `/products` catalogue query (no predicates in
  `ProductSpecifications`) returns "Electric Kettle" for a `name ~ 'kettl'`.
- For the customer upsert: catch `DataIntegrityViolationException` on save and
  re-read by email; test with two concurrent `placeOrder` calls for the same
  new email asserting exactly one customer row and both orders created.