# Exercises: The 7 Database Laws of Senior Backend Developer

Work with the app in [`app/`](./app). Answers are in [`solutions.md`](./solutions.md).

**Setup reminders**
- Database: `docker compose up -d --wait` (Postgres 16 on `localhost:5432`).
- Tests (no Docker needed): `export JAVA_HOME=$(/usr/libexec/java_home -v 24)` then
  `mvn -f app/pom.xml test`.
- Run the API: `mvn -f app/pom.xml spring-boot:run`, then use `db-laws.http` or curl.
- SQL against Postgres: `docker exec -it dblaws-postgres psql -U dblaws -d dblaws`.

---

## Lesson 1: Start With the Queries

**Exercise 1.1 — map queries to schema.** List the access patterns of this
catalogue+orders app (screen → query → table/column). Fill the table for at
least 6 rows. Two are started for you:

| Screen / report | Query it runs | Served by |
|-----------------|---------------|-----------|
| Catalogue, filtered by category | `select * from products where category = ?` | `idx_products_category` |
| Customer order history | … | … |

**Exercise 1.2 — spot the access pattern.** Read `repository/OrderRepository.java`.
Which method serves the finance dashboard, and which column(s) must be indexed
for it to stay fast as orders grow? Which method eagerly loads `items` — and
what N+1 problem does that prevent?

**Exercise 1.3 — design from a query.** A new product-owner request: "show me
products whose name contains 'kettle'". Write the access pattern, then state
whether the existing schema serves it and what (if anything) you would add.

## Lesson 2: Ask What Happens If the Data Is Wrong

**Exercise 2.1 — trace the transaction.** Read `service/OrderService.java`.
List, in order, every write that happens inside `placeOrder`. Underline the
line that can fail after earlier writes have already happened.

**Exercise 2.2 — predict rollback state.** Product A has stock 10, product B has
stock 1. A request places an order for `A × 5` and `B × 2`. Write the stock of
A and B and the number of new order rows after the request completes. Then run
`OrderServiceTransactionTest` and confirm.

**Exercise 2.3 — defend the snapshot.** `order_items` stores `product_name` and
`unit_price` even though a `product_id` column exists. The catalogue can rename
or reprice a product. Explain in one sentence why past invoices must not change
when that happens.

## Lesson 3: Don't Replace the Database Before Finding the Bottleneck

**Exercise 3.1 — which index helps?** For each slow query, name the index from
`schema-indexes.sql` (or `@Index` in the entities) that treats it:

1. `select * from customer_orders where customer_id = ?`
2. `select * from customer_orders order by created_at desc`
3. `select * from products where category = ?`
4. `select * from order_items where product_id = ?`

**Exercise 3.2 — read a plan.** Run against Postgres:

```sql
EXPLAIN ANALYZE SELECT * FROM products WHERE category = 'grocery';
```

The app has already created `idx_products_category`. What node appears in the
output (Seq Scan or Index Scan)? Swap the WHERE to
`category = 'does-not-exist'` — does the database still use the index?

**Exercise 3.3 — bottleneck order.** A colleague says "Postgres is too slow,
let's move to a NoSQL document store". List the three steps you insist on
before that conversation is even allowed to continue.

## Lesson 4: Understand How It Scales

**Exercise 4.1 — classify the workload.** Is this e-commerce app read-heavy,
write-heavy, or data-size-bound? Give one query that consumes the majority of
reads and one that is the majority of writes.

**Exercise 4.2 — pool math.** `application.yml` sets
`maximum-pool-size: 10`. If the app runs on 5 instances, what is the maximum
number of database connections the app can hold? What happens to a 6th request
on one instance when all 10 are checked out? What setting in the file bounds
how long it waits?

**Exercise 4.3 — N+1 spotting.** `repository/OrderRepository.java` uses
`@EntityGraph(attributePaths = "items")`. Write the alternative in one line
(loading an order without the graph) and state how many extra queries that
alternative issues for an order with 20 lines.

## Lesson 5: Know When One Database Is Enough

**Exercise 5.1 — defend the single DB.** The demo app runs one Postgres for
everything (catalogue reads, checkout writes, monthly reports). List three
queries in this repo that would have to leave Postgres for a separate store —
then argue why they *still don't* need to today.

**Exercise 5.2 — the evolution staircase.** Put these in the order a senior
developer would add them, explaining the trigger for each step:
(a) read replicas, (b) Redis cache, (c) analytics warehouse, (d) search engine.

**Exercise 5.3 — the H2/Postgres trick.** The app runs H2 in tests and
Postgres in production with the same code (`application-test.yml` vs
`application.yml`). What does this prove about the "one database" decision, and
what is the one thing H2-in-memory does NOT give you (see Lesson 6 if stuck)?

## Lesson 6: Don't Confuse Fast with Durable

**Exercise 6.1 — fast vs durable.** Classify each as *fast*, *durable*,
*both*, or *neither* relative to a PostgreSQL database with default settings:

1. Redis in-memory cache
2. H2 in-memory (tests only)
3. Postgres with `synchronous_commit = off`
4. Postgres with default fsync + WAL
5. A MySQL `memory` table

**Exercise 6.2 — the WAL in one sentence.** Complete: "Postgres first appends
the change to the WAL and fsyncs it; the database can therefore…" and explain
why losing the data *page* on power loss does not lose committed data.

**Exercise 6.3 — where fast is fine.** Give one scenario (from this project)
where accepting non-durable, in-memory storage is the right trade.

## Lesson 7: Think About What Happens After Deployment

**Exercise 7.1 — read the docker config.** From `docker-compose.yml`: what
does the healthcheck check? What survives a `docker compose down` that would
*not* survive deleting the container without the volume? What env settings must
match `application.yml`?

**Exercise 7.2 — the migration gap.** `application.yml` uses
`ddl-auto: update`. Name two production problems that configuration causes,
and state what `schema-indexes.sql` is *for* (hint: it IS the manual,
Flyway-style version).

**Exercise 7.3 — the 10-point deploy checklist.** Write ≥10 actionable
deploy-day items for this app, tagging each one *backup*, *migration*,
*health*, or *observe*. One is started for you:
"Verify `docker compose ps` shows `(healthy)` — *health*".

## Integration: Diagnose & Harden This App

**Exercise I.1 — pick two laws = two finds.** Choose any two of the seven
laws. For each, (a) cite the code in `app/` that already embodies the law
(method/line), and (b) name a real gap — an access pattern with no index
serving it, a read that could N+1, a missing durability check.

**Exercise I.2 — propose + verify.** For each gap, propose a concrete change
and draft one test method (in the style of `OrderServiceTransactionTest`) that
would prove the change works.