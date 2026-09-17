# Education Log — Seven Database Laws Course

> Documentation of every step taken to build this course, written for "future
> me" (and anyone else) to learn from and reuse.

## 1. Project Overview

**Goal:** Turn the YouTube video *"7 Database Laws of Senior Backend Developer"*
into a structured, runnable educational resource: a Spring Boot 4 e-commerce
application backed by PostgreSQL that demonstrates the laws, plus lesson plans,
exercises and verified solutions.

**Tools used:**
- **Tesseract OCR** (Homebrew) — extract the framework image `OLD.jpg` → `OLD.txt`
  (done in the earlier `eighth-api-rules` session; the text is reused here)
- **Web search / fetch** — analyse the video and pull its transcript
- **Maven 3.9.16 + JDK 24** (OpenJDK 24.0.1) — build and test the app
- **Spring Boot 4.1.1 + Spring Data JPA** — application platform
- **Docker / docker compose** — PostgreSQL 16 in a container (runtime DB)
- **H2** — in-memory database for tests (no Docker needed to run `mvn test`)

**Deliverables:**

| File / Folder | Description |
|---|---|
| `OLD.txt` | OCR extraction of `OLD.jpg` (9-input lesson-planning framework) |
| `docker-compose.yml` | Postgres 16 service: healthcheck, named volume, `.env` credentials |
| `.env` | Postgres credentials (defaults match `application.yml`) |
| `README.md` | Course overview, run instructions, curl + EXPLAIN examples |
| `lesson_plans.md` | 7 lesson plans + integration project, built on the 9-input framework |
| `exercises.md` | Per-lesson exercises + integration review rubric |
| `solutions.md` | Verified sample answers (including real EXPLAIN output) |
| `education.md` | This document |
| `db-laws.http` | VS Code REST Client — every lesson as a runnable request |
| `app/` | Spring Boot 4 e-commerce app (`customers`, `products`, `orders`) + tests |

## 2. The Source Material

1. **`OLD.jpg`** — the 9-input lesson-planning framework, OCR'd to `OLD.txt`:
   Learning Goal, Lesson Sequence, Assessment Evidence, Learner Profile, Prior
   Knowledge, Learning Activities, Output Requirements, Accessibility &
   Supports, Teacher Decisions. Tagline: *"A lesson plan is a system, not a
   prompt."*

2. **YouTube video** — *7 Database Laws of Senior Backend Developer*. The 7 laws,
   extracted from the transcript:
   1. Start with the queries (schema from access patterns, not entities)
   2. Ask what happens if the data is wrong (transactions, constraints,
      compensating transactions)
   3. Don't replace the database before finding the bottleneck (measure, index,
      only then consider a new store)
   4. Understand how it scales (read-heavy / write-heavy / data-size bound)
   5. Know when one database is enough (single DB → cache → search → analytics,
      each step triggered by a measured signal)
   6. Don't confuse fast with durable (latency ≠ durability; WAL + fsync; ACID)
   7. Think about what happens after deployment (managed services, backups,
      versioned migrations, observability)

## 3. Design Decisions (and the laws they serve)

| Law | Where it lives in this app |
|---|---|
| 1 | `repository/OrderRepository.java` (per-customer history, DB-side `GROUP BY` for monthly sales and top customers), `repository/ProductRepository.java` (one parameterised filter query); indexes declared on entities AND in `schema-indexes.sql` with a comment naming the query each serves |
| 2 | `OrderService#placeOrder` = one `@Transactional`; stock taken atomically via `UPDATE … WHERE stock >= :qty` (`ProductRepository#decrementStockIfAvailable`); `OrderItem` snapshots name/price (past invoices never change); `Product.@Version` optimistic lock; unique email index as backstop |
| 3 | `schema-indexes.sql` is the teaching artifact: `EXPLAIN ANALYZE` the category query and watch `Seq Scan` → `Index Scan`. The lesson plan explicitly forbids switching databases before this diagnosis |
| 4 | HikariCP pool settings documented in `application.yml`; `@EntityGraph(attributePaths="items")` kills the N+1 — a scaling win with zero new infrastructure |
| 5 | One Postgres stores everything; tests run *the same code* on H2 in-memory (`application-test.yml`), proving a single persistence contract is enough until a measured signal says otherwise |
| 6 | The H2-vs-Postgres split is the fast-vs-durable teachable moment; `application.yml` explains WAL/`ddl-auto` trade-offs in comments |
| 7 | `docker-compose.yml` has healthcheck + volume; `schema-indexes.sql` is the manual Flyway-style migration; README documents the deploy checklist |

**Domain:** a tiny shop (customers, products, orders + line items) — rich enough
for real transactions and aggregates, small enough to read in one sitting. Seed
data comes from `DataInitializer` (a `CommandLineRunner`), not `data.sql`, to
avoid the Boot `sql.init`/Hibernate schema-ordering pitfall and to run
identically on Postgres and H2.

## 4. Platform Facts Resolved Along the Way

| Question | Finding |
|---|---|
| Current stable Boot? | **4.1.1** (needs Java 17+, tested up to 26) |
| JDK to use here? | **24** — the default shell `java` is 27-ea, unsupported by Boot 4.1.1 |
| MockMvc on Boot 4? | `@AutoConfigureMockMvc` moved to `org.springframework.boot.webmvc.test.autoconfigure`; requires the `spring-boot-starter-webmvc-test` companion starter |
| H2 vs Postgres for tests? | H2 `MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE` in `application-test.yml`, activated via `@ActiveProfiles("test")` — tests pass with **no Docker** |
| Reserved word trap? | JPA entity named `Order` — must map to `@Table(name = "customer_orders")`; bare `order` is a reserved keyword in Postgres |
| HQL boolean comparison | `(p.stockQuantity > 0) = :inStock` is **invalid HQL** → rewrote to `(:inStock = true and p.stockQuantity > 0) or (:inStock = false and p.stockQuantity = 0)` |
| JPQL aggregate portability | `year(o.createdAt)` / `month(o.createdAt)` Hibernate translates to `extract(year/month from …)` on Postgres — works on H2 too (avoids non-portable `to_char`) |

## 5. Verification

**Unit/integration tests — H2 (no Docker):**
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -f seven-database-laws/app/pom.xml test
```
Result: **10 tests, 0 failures**:
- `DbLawsApplicationTests` — context/seed boots on H2.
- `OrderServiceTransactionTest` (the Law 2 proof) — successful order commits and
  stock drops atomically; a failing line rolls back *every* earlier stock
  decrement (order count unchanged); buying the exact last unit succeeds.
- `OrderApiTest` — catalogue filter, order 201 + line totals, decremented stock
  visible on the product, insufficient stock → 409 with nothing decremented,
  orders listed per customer, monthly-sales aggregate grouped by the DB.

**Live smoke test — Postgres via Docker:**
1. Started Docker Desktop (`open -a Docker`), then
   `docker compose up -d --wait` → Postgres 16 healthy on port 5432.
2. Ran the app with the default (Postgres) profile and curl'd:
   - `GET /products?category=grocery` → index-backed, `totalElements` correct,
   - `GET /products?inStock=true` → all rows in stock,
   - `POST /orders` → 201, computed `totalAmount` (`12.50`),
   - `POST /orders` with quantity 999_999 → **409**
     `{"code":"INSUFFICIENT_STOCK", "message":"Only 316 … are in stock, 999999 requested"}`
     — confirming the conditional UPDATE and rollback,
   - `GET /orders/{id}` → order with its `items[]` in one query,
   - `GET /stats/monthly-sales` and `/stats/top-customers` → aggregates.
3. Verified the schema on Postgres: `\di` shows every index from `@Index` +
   `schema-indexes.sql` (all 6 `idx_*`), and `EXPLAIN ANALYZE` on the category
   query reports `Index Scan using idx_products_category`.

One bug was caught by the tests: the first HQL attempt used a boolean
comparison syntax Hibernate rejected at startup ("Bad HQL grammar"), so all 10
context loads failed. The live replay then exposed a second bug: a null
optional filter was bound as `bytea`, and Postgres rejected `lower(bytea)`. The
fix — a `Specification`-built query in `ProductSpecifications` that binds only
non-null filters — is captured above under platform facts.

## 6. Teaching Material

`lesson_plans.md` applies the 9 inputs from `OLD.txt` to each of the 7 laws
plus a culminating integration project. `exercises.md` mixes SQL/diagnosis
tasks (EXPLAIN, pool math, N+1 counting) with code-reading tasks that force
learners into the repo. `solutions.md` answers were verified — the EXPLAIN
outputs quoted in Lesson 3 come from the actual running container.

## 7. Quick Reference — Commands

```bash
# Database (from seven-database-laws/)
docker compose up -d --wait        # Postgres 16, healthy
docker compose down                # keeps data (named volume)
docker compose down -v             # removes data

# Tests — H2, no Docker
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -f app/pom.xml test

# Run the app against Postgres
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -f app/pom.xml spring-boot:run

# Inspect the schema / a plan
docker exec -it dblaws-postgres psql -U dblaws -d dblaws -c '\di'
docker exec -it dblaws-postgres psql -U dblaws -d dblaws \
  -c "EXPLAIN ANALYZE SELECT * FROM products WHERE category = 'grocery';"
```

## 8. Lessons Learned

1. **Schema-first teaching artifacts help.** Indexes declared as `@Index` on
   entities kept the app runnable *and* gave `schema-indexes.sql` exact content
   to teach EXPLAIN against — the two never drifted.
2. **HQL is not SQL.** The boolean-comparison failure showed up as 10 context
   failures with one root cause; the fix lives in one WHERE clause. Test early,
   test one thing first.
3. **H2 "PostgreSQL mode" is good, not perfect.** `MODE=PostgreSQL` + JPQL
   `year()/month()` worked, but native `to_char` would not have — prefer
   portable JPQL for (mostly) dual-dialect code.
4. **Named volumes are the durability lesson in miniature.** Docker container
   deletion ≠ data deletion; that distinction teaches Law 6/7 better than any
   diagram.
5. **Same persistence contract, two dialects** (Postgres vs H2-in-memory) made
   Laws 5 and 6 concrete in one configuration decision.
6. **Reuse beats redo.** `OLD.txt`, the lesson-plan structure, and the Boot 4
   test conventions carried over from the `eighth-api-rules` course and kept
   the material consistent across the series.

## 9. Where to Go Next

- [ ] Add a Flyway/Liquibase migration module and flip `ddl-auto` to `validate`.
- [ ] Add a Redis cache layer lesson (Law 5 staircase step 2) on top of the
      catalogue endpoint.
- [ ] Add a `pg_stat_statements` lab for finding the real hot queries (Law 3).
- [ ] Add a backup/restore drill script using `pg_dump` into the volume.
- [ ] Script the video→course pipeline (transcript → law list → app scaffold),
      now proven twice.