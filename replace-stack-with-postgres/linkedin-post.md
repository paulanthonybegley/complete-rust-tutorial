# LinkedIn post — "I replaced 8 services with one Postgres"

> Suggested hook + body for the creator's timeline feed. Drop the blockquotes
> (they are staging notes, not post text) and paste the plain paragraphs.

---

**"I replaced MongoDB, Redis, Elasticsearch, a vector DB, a GIS service, a
time-series DB, a warehouse and my authz layer with one Postgres — and
recorded the whole honest mess."**

That's not a pitch. It's the demo I built: `replace-stack-with-postgres`, a
Spring Boot 4 + Thymeleaf + HTMX app where every "replacement" is a live page
backed by real Postgres 16 (with PostGIS, pgvector and pg_trgm):

- **MongoDB** → a `JSONB` column + GIN index (`@>` containment on nested keys)
- **Redis/RabbitMQ** → `FOR UPDATE SKIP LOCKED`; three workers, retries with `max_attempts`
- **Elasticsearch** → `tsvector`/`tsquery` ranking+highlighting, falling back to `word_similarity` trigram typos
- **Pinecone / vector DBs** → `pgvector` + HNSW, filtered by author/tag *in the same SQL*
- **GIS / geocoding** → `PostGIS` + GiST: radius, k-NN, point-in-polygon
- **InfluxDB / time-series** → range partitioning + BRIN; `EXPLAIN` shows it skip blocks
- **Snowflake / warehouses** → materialized views + `REFRESH ... CONCURRENTLY`
- **authz middleware** → Row Level Security, set per session, spoof-free

One `docker-compose.yml`. One seeded schema (387k events across monthly
partitions, articles, vectors, geography, orders). ~10 pages, each one a
working SQL demo — plus an honest `/caveats` page about when Postgres is the
*wrong* tool. I only wrote it because The Coding Gopher's
[#](https://www.youtube.com/watch?v=TdondBmyNXc) video made the same argument
so well.

**The part worth reading — the failures.** Almost everything that looked
simple broke, and each break taught the concept:

- `SET LOCAL app.current_user`? `current_user` is a **reserved keyword** — the GUC became `app.uid`.
- `REFRESH MATERIALIZED VIEW CONCURRENTLY` needs *ownership*, not a `SELECT` grant.
- `attributes @? '$.specs...'` — that `?` collided with the JDBC placeholder parser. Use `jsonb_path_exists()`.
- `word_similarity(a, b)` is **not symmetric**. Arg order is the whole bug.
- An RLS-violating INSERT doesn't silently drop the row — in PG 16 it **aborts the whole transaction**.
- BRIN needs `USING brin` with `pages_per_range`, or it's a worse full scan.
- HTMX `targetError`: `th:replace` discards the target element's `id`. Every page's search target vanished.

Every one of those is a 10-minute hiring-question-shaped lesson.

And because "a lesson plan is a system, not a prompt", the repo ships a full
unit built on that 9-input framework (`lessons/`): learning goal, sequence,
assessment evidence, learner profile, prior knowledge, activities, output
requirements, accessibility, teacher decisions — with a worksheet and an
answer key where every SQL solution was **executed against the seeded DB**
before release. The capstone? "Delete a service" — prove a replacement, show
the plan, name the honest limit.

**Try it (it's all local, Docker only):**
`docker compose up -d --build && mvn spring-boot:run` → http://localhost:8080

#postgres #springboot #htmx #databases #devops #teaching