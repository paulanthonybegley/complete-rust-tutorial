# LinkedIn Post: Seven Database Laws

---

**🎓 7 Database Laws Every Senior Backend Developer Lives By**

I just finished teaching a database design course, and these 7 laws are the difference between a schema that works and one that hurts you at 10x.

**1️⃣ Start with the queries, not the entity diagram**

Design the schema from the screens' access patterns. Filter by category → index it. Show per-customer order history → index the customer_id. `EXPLAIN` your access paths first, then create tables.

**2️⃣ Ask: what happens if the data is wrong?**

Wrap every multi-step write in one transaction. Take stock with a conditional atomic UPDATE:
```sql
UPDATE products SET stockQuantity = stockQuantity - 5
WHERE id = 1 AND stockQuantity >= 5;
```
If the next line fails, every earlier decrement rolls back.

**3️⃣ Don't replace the DB before finding the bottleneck**

Measure first. Add the index. Then — and only then — consider a new store. I watched one `EXPLAIN ANALYZE` switch a Seq Scan to an Index Scan:
```
-> Index Scan using idx_products_category
```
The database was never the problem. Nobody measured.

**4️⃣ Understand how it scales**

Read-heavy → read replicas + caching. Write-heavy → reduce contention (atomics, queues, sharding). Data-size-bound → different storage engine. Each constraint has its own lever.

**5️⃣ Know when one database is enough**

One Postgres handles hundreds of rows and hundreds of millions. It's the right default. Make the *second* store earn its place with a measured signal — stale caches and dual-writes are taxes, not features.

**6️⃣ Don't confuse fast with durable**

Latency is a feeling. Durability is a contract — WAL + fsync in the right order. An in-memory cache is fast; it is not where your money lives.

**7️⃣ Think about after deployment**

Managed service. Backups. Versioned migrations (Flyway/Liquibase). `ddl-auto: update` is fine for a tutorial, `validate` is what ships. Slow-query logs and latency alerts — *observe before you scale.*

---

**Pro Tip:** The law order is intentional — access patterns first (1), correctness before speed (2 → 3), and "one database is enough" (5) before you ever reach for the second store.

The whole course ships as a runnable Spring Boot 4 + PostgreSQL app — every law demonstrated in working code, atomic transactions, index-backed queries, and 11 passing tests.

What's the worst database decision you've inherited? Drop it in the comments 👇

---

*#Database #PostgreSQL #Backend #SoftwareEngineering #SQL #SpringBoot #DataEngineering #TechLearning*