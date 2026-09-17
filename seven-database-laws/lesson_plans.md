# Structured Lesson Plans: The 7 Database Laws of Senior Backend Developer

> Applied from the YouTube video **"7 Database Laws of Senior Backend Developer"**.
>
> Core tagline from `OLD.jpg`: **"A lesson plan is a system, not a prompt."**

Every lesson below is built with the 9 design inputs extracted from
[`OLD.txt`](./OLD.txt):
**Learning Goal**, **Lesson Sequence**, **Assessment Evidence**, **Learner
Profile**, **Prior Knowledge**, **Learning Activities**, **Output
Requirements**, **Accessibility & Supports**, and **Teacher Decisions**.

The companion application lives in [`app/`](./app) — a Spring Boot 4
e-commerce API (customers, products, orders) backed by PostgreSQL, whose code
is the "worked example" for each law. Every lesson lists the exact files to
open and the tests that prove the behaviour.

| # | Law (from the video) | App files to read | Proof tests |
|---|----------------------|-------------------|-------------|
| 1 | Start with the queries | `repository/OrderRepository.java`, `repository/ProductRepository.java`, `web/StatsController.java` | `OrderApiTest#monthlySalesAggregateIsGroupedByDatabase` |
| 2 | Ask what happens if the data is wrong | `service/OrderService.java`, `repository/ProductRepository.java`, `model/Product.java` | `OrderServiceTransactionTest` (all 3) |
| 3 | Don't replace the database before finding the bottleneck | `src/main/resources/schema-indexes.sql`, `repository/ProductRepository.java` | `OrderApiTest#catalogueFiltersByCategoryAndStock` |
| 4 | Understand how it scales | `application.yml` (HikariCP), `repository/OrderRepository.java` | `OrderApiTest#ordersAreListedByCustomer` |
| 5 | Know when one database is enough | whole `app/` design, `docker-compose.yml`, H2/Postgres split | `DbLawsApplicationTests` |
| 6 | Don't confuse fast with durable | `application.yml`, `schema-indexes.sql`, `OrderService.java` | `OrderApiTest#insufficientStockAnswers409AndRollsBack` |
| 7 | Think about what happens after deployment | `docker-compose.yml`, `schema-indexes.sql`, `.env` | smoke run in README |

---

## Lesson 1: Start With the Queries

**Learning Goal:** Learners design a schema from the access patterns (the
queries the product will actually run) instead of from a box-and-arrow entity
diagram, and can justify every index and join by naming the query it serves.

**Lesson Sequence:**
1. Warm-up: brainstorm the pages of an e-commerce site (catalogue, order list,
   checkout, finance dashboard) and list one query each page must answer.
2. Read the repository layer: `repository/OrderRepository.java` and
   `repository/ProductRepository.java`. For each query method, answer:
   "which UI screen or report does this serve?"
3. Cross-check the schema comments: `model/Customer.java`,
   `model/Order.java`, `model/Product.java` name the query behind each index.
4. Hands-on: complete Exercise 1 in `exercises.md`.

**Assessment Evidence:** Given two candidate fields (e.g. `product.rating` and
`order.shipmentQuotedDate`), the learner states which query would scan, which
query would use an index, and sketches the access-pattern-first approach.
- Assessment answer key: `solutions.md` → Lesson 1.

**Learner Profile:** Junior backends up to ~1 year of experience, comfortable
with Java and basic SQL (SELECT with joins), new to schema design.

**Prior Knowledge:** JPA `@Entity` basics, what a foreign key is, what an index
is at a conceptual level.

**Learning Activities:** Access-pattern brainstorm, query-to-column trace
(read a query, find the indexed column it uses), paired schema sketch.

**Output Requirements:** A one-page "access patterns" document for the demo app
listing ≥6 real queries and, next to each, the table/column that serves it.

**Accessibility & Supports:** Provide the SQL-to-JPA glossary (`findBy…`
→ `where …`); allow dictation of the access patterns; extra time for the
trace activity.

**Teacher Decisions:** Keep this lesson read-only — no new code. The moment of
payoff is seeing that `idx_orders_customer_id` is *explained by* the
"orders for a customer" screen, not by the entity diagram.

---

## Lesson 2: Ask What Happens If the Data Is Wrong

**Learning Goal:** Learners design transactions and constraints that keep the
database correct when two requests race or a request is half-wrong, and can
prove rollback with a test.

**Lesson Sequence:**
1. Scenario: last item in stock, two customers check out at the same second.
   What breaks if you just `stock--` in Java?
2. Read `service/OrderService.java` — one `@Transactional` boundary around
   stock decrements + order insert.
3. Read the atomic guard in `repository/ProductRepository.java`:
   `decrementStockIfAvailable` only succeeds when `stock >= quantity`.
4. Run and read `OrderServiceTransactionTest` — the failing-line test proves
   the earlier decrement is undone.
5. Hands-on: complete Exercise 2.

**Assessment Evidence:** Learner explains why a retried checkout cannot
oversell (conditional UPDATE + rollback), and predicts the state of both
products after a 2-line order fails on line 2.
- Assessment answer key: `solutions.md` → Lesson 2.

**Learner Profile / Prior Knowledge:** Same as Lesson 1, plus a working
knowledge of what a database *transaction* is called in their framework.

**Learning Activities:** Race-condition whiteboard, reading the rollback test,
"fix the naive stock bug" drill.

**Output Requirements:** Written answers: (a) why optimistic locking
(`Product.@Version`) exists, (b) why the demo stores a snapshot of
product name/price in `order_items`, (c) why the unique email index is a
backstop.

**Accessibility & Supports:** Timeline diagram of the two checkouts; recordable
verbal explanation of rollback; extra time on the whiteboard activity.

**Teacher Decisions:** Emphasize the *question* ("what happens if the data is
wrong?") over the answer — every constraint in the app exists because one of
the learners' scenarios answered "it breaks". Mention compensating
transactions (outbox / saga) as the answer when a single ACID transaction
spans services.

---

## Lesson 3: Don't Replace the Database Before Finding the Bottleneck

**Learning Goal:** Learners treat slow queries as a diagnosis problem: measure
first, build the index the hot query needs, and only *then* decide whether a
new database is warranted.

**Lesson Sequence:**
1. Pitch: "catalogue page is slow — let's move to a NoSQL store". List what we
   lose by leaving Postgres before we know *why* it is slow.
2. Add an index as the first treatment: read `schema-indexes.sql` next to the
   `@Index` annotations in `model/*.java`.
3. Run `EXPLAIN ANALYZE SELECT … FROM products WHERE category = 'grocery';`
   before and after `idx_products_category` (planned in `schema-indexes.sql`)
   and read the plan change from `Seq Scan` to `Index Scan`.
4. Hands-on: complete Exercise 3.

**Assessment Evidence:** Given a slow query log line, the learner states the
two cheapest checks (is the WHERE column indexed? is `EXPLAIN` showing a seq
scan?) before proposing a new database.
- Assessment answer key: `solutions.md` → Lesson 3.

**Learner Profile / Prior Knowledge:** Same as Lesson 1; must be able to run
`psql` against the container (*see README*) or observe the plan in the test
database.

**Learning Activities:** Plan-comparison reading, "which index helps this
query?" drill, bottleneck checklist.

**Output Requirements:** A saved `EXPLAIN ANALYZE` output for the category
query with the plan before/after the index, plus one paragraph justifying
"optimize → index → only then consider a new store".

**Accessibility & Supports:** Pre-written EXPLAIN commands in the README;
screenshot-friendly output; permit working in pairs.

**Teacher Decisions:** The headline to repeat: replacing the database is the
*lobotomy*, indexing is the *eyeglasses*. Recommendation: teach `pg_stat_statements`
as the lazy developer's bottleneck finder.

---

## Lesson 4: Understand How It Scales

**Learning Goal:** Learners classify a workload as read-heavy, write-heavy, or
data-size-bound and choose the scaling lever that matches (indexes, connection
pool, read replicas, partitioning) instead of guessing.

**Lesson Sequence:**
1. Characterize the demo app: many small reads (orders per customer), few big
   writes (checkouts), growing `order_items`.
2. Read the pool settings in `application.yml` (HikariCP
   `maximum-pool-size`, `minimum-idle`, `connection-timeout`). Work out which
   symptom each setting treats.
3. Read `repository/OrderRepository.java`: eager `items` fetch via
   `@EntityGraph` avoids the N+1 — a scaling win that needs no new database.
4. Hands-on: complete Exercise 4.

**Assessment Evidence:** Learner classifies 3 given workloads (social feed,
event log, product catalogue) and proposes one specific change *within* the
existing database for each before any new store.
- Assessment answer key: `solutions.md` → Lesson 4.

**Learner Profile / Prior Knowledge:** Same as Lesson 1; comfort reasoning
about request rates and row counts conceptually.

**Learning Activities:** Workload-classification cards, pool-math (10 app
instances × how many connections?), N+1-spotting in repository code.

**Output Requirements:** A one-page "scale decision" worksheet for the app:
workload type, current lever, next lever, and the signpost that says "time for
a replica/partition".

**Accessibility & Supports:** Provide the classification table as a handout;
verbal-only answers accepted; extra time on the pool-math exercise.

**Teacher Decisions:** Stress that "scales" is about *the shape of the
workload*, not a hype word. Recommended arcs: read-heavy → read replica;
write-heavy → batch + partitioning; data-size → partitioning/data lifecycle.

---

## Lesson 5: Know When One Database Is Enough

**Learning Goal:** Learners recognize that a single Postgres instance is the
right answer for most apps and can name the concrete event that should trigger
adding a second store (cache → search → analytics → …) rather than adding it
pre-emptively.

**Lesson Sequence:**
1. Interview the "one database" assumption in this app: entities, reads,
   writes, and reports all live in one Postgres.
2. Read `docker-compose.yml` — everything the app needs is one container.
3. Walk the evolution staircase: one DB → add Redis for hot reads → add
   OpenSearch for search → offload analytics to a warehouse. For each step,
   name the query that stopped being cheap.
4. Hands-on: complete Exercise 5.

**Assessment Evidence:** Given a feature request (e.g. full-text search on
products), learner explains why Postgres `tsvector` is tried *before* a search
server, and states the measurable signal (query latency at scale) that would
justify the move.
- Assessment answer key: `solutions.md` → Lesson 5.

**Learner Profile / Prior Knowledge:** Same as Lesson 1; bonus for anyone who
has operated a caching layer.

**Learning Activities:** Evolution-staircase mapping, "should we add Redis
now?" debate (two-minute positions), single-DB defense exercise.

**Output Requirements:** A written decision log for 3 hypothetical features
(such as caching, search, analytics) recording: tried in Postgres first? what
signal triggered the new store?

**Accessibility & Supports:** Provide the staircase diagram; allow drawn
answers; group discussion for the debate round.

**Teacher Decisions:** This is the anti-hype lesson. Keep the default answer
"one database" and force learners to *earn* the second store with a measured
signal. Mirror note: the app literally runs on H2 in tests and Postgres in
production with zero code changes — one persistence contract, two dialects.

---

## Lesson 6: Don't Confuse Fast with Durable

**Learning Goal:** Learners distinguish latency symptoms from durability
guarantees, explain what Postgres WAL + fsync provide, and know when a fast
default (e.g. H2 in-memory, `synchronous_commit=off`) is acceptable versus
dangerous.

**Lesson Sequence:**
1. Thought experiment: "the app responds in 1 ms — great?" Challenge: what
   happens to *committed* data on process kill / host power loss?
2. Read `application.yml`: the tutorial runs `ddl-auto: update` for zero
   setup — identify what guarantee that weakens (schema drift,
   reproducibility), and note the WAL answer in `schema-indexes.sql` /
   README: Postgres writes to the WAL and fsyncs, then the data pages.
3. Contrast H2-in-memory (tests) vs Postgres (prod): both fast, only one
   durable. Check `application-test.yml`.
4. Hands-on: complete Exercise 6.

**Assessment Evidence:** Learner answers "is an in-memory cache durable?" with
a definition of durability, explains the WAL's role in one sentence, and lists
one test-only scenario where fast > durable.
- Assessment answer key: `solutions.md` → Lesson 6.

**Learner Profile / Prior Knowledge:** Same as Lesson 1; the ACID acronym is a
plus but not required.

**Learning Activities:** "What does the WAL do?" explainer, fast-vs-durable
sorting game (items: Redis, H2-mem, Postgres with fsync, Postgres with
`synchronous_commit=off`), scenario cards.

**Output Requirements:** A two-column "fast vs durable" table for 6
technologies the learner has met, with a one-line verdict on each.

**Accessibility & Supports:** Pre-scripted demo SQL showing a committed row
survives a restart; recorded explainer video option; glossary of fsync/WAL.

**Teacher Decisions:** Keep it practical: durability is a *contract*, latency
is a *feeling*. The teaching lever is the H2-vs-Postgres profile split in this
very project.

---

## Lesson 7: Think About What Happens After Deployment

**Learning Goal:** Learners plan the day-after: managed service where
possible, verified backups, flyway-style schema control, and
observability — so "it works on my machine" becomes "it recovers in
production".

**Lesson Sequence:**
1. Ask: the demo works locally. What happens if the VM dies, a schema change
   is deployed, or the dashboard slows down?
2. Read `docker-compose.yml`: healthcheck, volume, restart strategy — the
   "managed service" minimum.
3. Read `schema-indexes.sql` and the `application.yml` comment: the
   `ddl-auto: update` shortcut should become `validate` + versioned SQL
   (Flyway/Liquibase) before any real deploy.
4. Read the README smoke-run list: boot, curl the endpoints, check the
   container health.
5. Hands-on: complete Exercise 7.

**Assessment Evidence:** Learner writes (or speaks) a 10-point pre-deploy
checklist covering backups, migration safety, healthchecks, and monitoring
for the demo app.
- Assessment answer key: `solutions.md` → Lesson 7.

**Learner Profile / Prior Knowledge:** Same as Lesson 1; a basic idea of
container healthchecks helps.

**Learning Activities:** "Disaster drill" (DB volume deleted → restore),
deploy-checklist sprint, reading the docker `environment` mapping.

**Output Requirements:** A deploy checklist for this app with ≥10 actionable
items, each tagged backup / migration / health / observe.

**Accessibility & Supports:** Checklist template provided; allow audio
recording of the checklist; pair-share before individual write-up.

**Teacher Decisions:** Anchor on concrete artifacts in this repo (`docker-compose.yml`,
`schema-indexes.sql`, README smoke-run). Make the point that backups are only
real when you have practiced a restore.

---

## Integration Project: Diagnose & Harden This App Like a Senior

**Learning Goal:** Learners apply Laws 1–7 to the contributed app code as a
single culminating review, producing a written "senior code review" of the
database layer.

**Learning Sequence:**
1. Pick two laws. For each, find a real behaviour in the code (an index, a
   transaction, a profile) and a real gap (an access pattern with no index, a
   missing durability check).
2. Propose one concrete improvement per gap and *state the test you would
   write* to prove it (mirroring `OrderServiceTransactionTest`).
3. Deliver the review.

**Assessment Evidence:** The written review correctly maps code → law, names a
gap with evidence (a method or line), and proposes a verifyable change.
- Assessment answer key: `solutions.md` → Integration.

**Learner Profile:** Same as all lessons (integration, so working in pairs is
encouraged).

**Prior Knowledge:** All seven lessons.

**Learning Activities:** Code audit, paired review, test sketch.

**Output Requirements:** ≤2-page markdown code review with a per-law table,
plus one drafted test method per proposed change.

**Accessibility & Supports:** Template review structure provided; verbal
review allowed; extra time.

**Teacher Decisions:** This is the summative task — grade on *evidence*, not
volume. Insist every claim cites a method/line from `app/`.