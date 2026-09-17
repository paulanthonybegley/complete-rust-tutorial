# LinkedIn Post: Analytics for Not-So-Big Data with DuckDB

---

**📊 6 Lessons from Teaching an "Analytics for Not-So-Big Data" Course**

I just built a complete course around the NDC Oslo 2025 talk *"Analytics for not-so-big data with DuckDB"* — and it changed how I think about analytics for the 99% of us who aren't running Snowflake at petabyte scale.

**1️⃣ Medium data is your data**

Gartner calls ~1–100 GB "medium data." That's your app logs. Your invoices. Your events. We built a real dataset: 50,000 orders, ~250,000 order lines, 300,000 log events — and it all lives in **one portable ~few-MB file** that the whole team can copy to their laptop.

**2️⃣ DuckDB is embedded — there is no server**

No daemon. No port. No connection pool. "The database" is a file you open. In-memory mode? Every *new* JDBC connection is a brand-new empty database:
```
Catalog Error: Table with name orders does not exist!
```
So the app shares one connection to share one engine. Embedding changes architecture in ways a diagram never teaches you.

**3️⃣ Analytical SQL runs at 5 milliseconds**

224,508 completed order lines, three tables, one `GROUP BY`:
```
Total Time: 0.0053s
```
Reading the `EXPLAIN ANALYZE` — `TABLE_SCAN` → `HASH_JOIN` → `HASH_GROUP_BY` — is how learners actually *believe* columnar + vectorized execution. The talk's 6M-row lineitem at ~32 ms is the same lesson, one scale up.

**4️⃣ One query, every bucket**

Date is a dimension, not a string:
```sql
SELECT date_trunc('day', o.order_date) AS bucket, ...
GROUP BY 1 ORDER BY 1
```
Flip the bucket from `day` to `month` and the same query returns 365 rows instead of 12. Want `TOP_N` in the plan instead of sorting everyone? Ask for it.

**5️⃣ "Dark data" — the needle was there the whole time**

300,000 event rows the app was writing for months, never read:
```
INFO  293,835   (~97.9%)
WARN    3,090   (~1%)
ERROR   2,751   (~0.9%)
FATAL     324   (~0.1%)
```
One `GROUP BY` finds the dangerous 1%. The analytics engine didn't need a new pipeline — it needed the table the app already owned.

**6️⃣ One file, whole toolchain**

The database is a file — copy it, open the copy, same answer. Query Parquet directly with `read_parquet`, COPY results out, scan Postgres or SQLite through extensions. Just remember the honest trade-offs: it's embedded OLAP, not a write-heavy OLTP store and not a multi-node warehouse — it fills the not-so-big-data slot in between.

---

**Pro Tip:** Pin your DuckDB version. CLI and JDBC must match — `.duckdb` files are forward-, *not* backward- compatible. We pinned both to v1.3.1 and it removed a whole class of subtle bugs.

Everything ships as a runnable Spring Boot 4 + DuckDB app: seeded "medium data," `EXPLAIN` endpoints, 7 passing tests, and the CLI container sharing the same file — plus full lesson plans, exercises and verified solutions.

Could you move your oldest query onto an embedded engine this week? 👇

---

*#DuckDB #Analytics #DataEngineering #SQL #SpringBoot #Databases #TechLearning #DataAnalysis*