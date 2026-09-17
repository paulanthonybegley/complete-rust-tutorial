# Seven Database Laws

An educational course that turns the YouTube video **"7 Database Laws of Senior
Backend Developer"** into a runnable Spring Boot 4 application backed by
PostgreSQL, plus lesson plans, exercises and verified solutions.

> Tagline from the source framework: **"A lesson plan is a system, not a prompt."**

## The 7 laws covered

| # | Law | Short version |
|---|-----|---------------|
| 1 | Start with the queries | Design the schema from the screens' access patterns, not the entity diagram |
| 2 | Ask what happens if the data is wrong | Transactions, constraints and rollback keep data correct under races |
| 3 | Don't replace the DB before finding the bottleneck | Measure, add the index — only then consider a new store |
| 4 | Understand how it scales | Read-heavy/write-heavy/data-size bound each has its own lever |
| 5 | Know when one database is enough | One Postgres is the right default; earn the second store with a signal |
| 6 | Don't confuse fast with durable | Latency is a feeling; durability (WAL + fsync) is a contract |
| 7 | Think about after deployment | Managed service, backups, versioned migrations, observability |

## Repository layout

```
seven-database-laws/
├── OLD.txt           # OCR text extracted from OLD.jpg (the lesson-planning framework)
├── docker-compose.yml# Postgres 16 + healthcheck + persistent volume
├── .env              # Postgres credentials (match application.yml)
├── lesson_plans.md   # 7 lesson plans + integration project on the OLD framework
├── exercises.md      # per-lesson exercises + integration rubric
├── solutions.md      # sample answers (verified against the running app)
├── education.md      # education log: how this course was built
├── db-laws.http      # VS Code REST Client — every lesson as a runnable request
└── app/              # Spring Boot 4.1.1 e-commerce app (customers/products/orders)
    └── src/main/java/com/example/dblaws/
        ├── config/DataInitializer.java  # idempotent seed (Postgres + H2)
        ├── model/                       # JPA entities + response records + ApiError
        ├── exception/                   # 404/409/422 + GlobalExceptionHandler
        ├── repository/                  # Law 1: access-pattern queries + atomic UPDATE
        ├── service/                     # OrderService (@Transactional), stats, products
        └── web/                         # /products /orders /customers /stats
```

## Run it

Requirements: JDK 17+ (tested on 24), Maven 3.9+, Docker.

```bash
# 1. Database
cd seven-database-laws
docker compose up -d --wait        # Postgres 16 on localhost:5432

# 2. Tests (H2 in-memory — NO Docker needed)
export JAVA_HOME=$(/usr/libexec/java_home -v 24)
mvn -qf app/pom.xml test           # 10 tests: context, transactions, API

# 3. Run the app against real Postgres
mvn -qf app/pom.xml spring-boot:run   # http://localhost:8080
```

Stop the DB with `docker compose down` (data survives via the `db-data` volume).
Remove the data entirely with `docker compose down -v`.

### Try it yourself

```bash
# Law 1 — filter backed by idx_products_category
curl -sg 'http://localhost:8080/products?category=grocery'

# Law 1 — monthly sales aggregate (GROUP BY in the database)
curl -s  http://localhost:8080/stats/monthly-sales

# Law 2 — place an order (201, stock decremented in the same transaction)
curl -s -X POST http://localhost:8080/orders -H 'Content-Type: application/json' \
  -d '{"customerName":"Ava","customerEmail":"ava@example.com","items":[{"productId":1,"quantity":1}]}'

# Law 2 — oversell the last unit -> 409, whole transaction rolled back
curl -s -w '\nHTTP %{http_code}\n' -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerName":"Mal","customerEmail":"mal@example.com","items":[{"productId":1,"quantity":99999}]}'

# Law 1 — orders for a customer (idx_orders_customer_id)
curl -sg 'http://localhost:8080/orders?customerId=1'
```

The same requests, grouped per lesson and with expected responses, are in
[`db-laws.http`](./db-laws.http).

### Law 3 — watch the index switch a Seq Scan to an Index Scan

```bash
docker exec -it dblaws-postgres psql -U dblaws -d dblaws \
  -c "EXPLAIN ANALYZE SELECT * FROM products WHERE category = 'grocery';"
# -> Index Scan using idx_products_category ...
```

`schema-indexes.sql` lists every index by name and the query that needs it —
and is the manual, Flyway-style version of the schema. In production, replace
`ddl-auto: update` with `validate` and apply this file (or a Flyway/Liquibase
migration) in CI before touching tables.

## What's under the hood

- **Spring Boot 4.1.1** with `spring-boot-starter-data-jpa`,
  `spring-boot-starter-web`, `spring-boot-starter-validation`.
- **Two persistence profiles with one codebase**: default = PostgreSQL (runtime,
  `application.yml`), `test` = H2 in memory (`application-test.yml`) — tests
  pass with no Docker. This split is itself Lesson 5 and Lesson 6.
- **Law 2 in code**: `OrderService#placeOrder` is one `@Transactional` method;
  stock is taken with a conditional atomic `UPDATE … WHERE stock >= :qty`
  (`ProductRepository#decrementStockIfAvailable`), so a failing line rolls back
  every earlier decrement — proven by `OrderServiceTransactionTest`.
- **Law 1 in code**: filtered catalogue search, per-customer order history,
  and DB-side `GROUP BY` aggregates; indexes declared on the entities and in
  `schema-indexes.sql` match the queries that need them.
- **Law 7 in code**: `docker-compose.yml` healthcheck + named volume + `.env`-driven
  credentials; idempotent seeding via `DataInitializer`.

## Learning order

1. Read `lesson_plans.md` — the 9-input OLD framework applied per law.
2. Do the matching exercises in `exercises.md` (SQL, code-reading, design).
3. Check your work against `solutions.md` (answers were verified against this app).
4. Finish with the integration project in `exercises.md`.
5. If you prefer hands-on-first, run `db-laws.http` top-to-bottom once, then
   reopen it after each lesson to see the reason behind each request.