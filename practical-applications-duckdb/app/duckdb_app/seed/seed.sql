-- =====================================================================
-- Practical Applications of DuckDB — seed dataset (idempotent, deterministic)
--
-- Single source of truth for the course database, executed by:
--   * the education app on startup (app/duckdb_app/seed.py)
--   * the Docker "duckdb" CLI container on first start (/init/seed.sql)
--
-- The dataset mirrors the real job stories in the podcast episode
-- "Practical Applications of DuckDB" (Developer Voices, with Simon Aubury
-- and Ned Letcher, authors of "Getting Started with DuckDB"):
--
--   * employees  (80 rows)  -> core SQL / aggregation lessons
--   * products   (8 rows)   -> the parquet & hive-partition demo sales
--   * sales      (6,000)    -> aggregates, COPies to parquet partitions
--   * places     (5 rows)   -> lon/lat points for the spatial extension lesson
--
-- Companion wrangle files (data/files/*.csv|*.jsonl and a hive-partitioned
-- parquet folder) mirror the guests' own stories:
--   * a Fitbit export (messy CSV, auto-detected with the CSV sniffer)
--   * a social-graph export (nested JSONL, wrangled with unnest/group)
--
-- All "random" values are deterministic (a hash of the row index), so every
-- run yields the SAME numbers and exercises have stable expected answers.
-- =====================================================================

CREATE TABLE IF NOT EXISTS employees (
    id          INTEGER PRIMARY KEY,
    name        VARCHAR,
    job_title   VARCHAR,
    department  VARCHAR,
    salary      INTEGER,
    hire_date   DATE
);

INSERT INTO employees (id, name, job_title, department, salary, hire_date)
SELECT
    CAST(i + 1 AS INTEGER)                                  AS id,
    'Employee ' || (i + 1)                                  AS name,
    CASE MOD(abs(HASH(i)), 10)
        WHEN 0 THEN 'Data Scientist'
        WHEN 1 THEN 'Data Engineer'
        WHEN 2 THEN 'Backend Engineer'
        WHEN 3 THEN 'Frontend Engineer'
        WHEN 4 THEN 'DevOps Engineer'
        WHEN 5 THEN 'Manager'
        WHEN 6 THEN 'Product Manager'
        WHEN 7 THEN 'Designer'
        WHEN 8 THEN 'QA Engineer'
        ELSE 'Site Reliability Engineer'
    END                                                     AS job_title,
    CASE MOD(abs(HASH(i * 13)), 4)
        WHEN 0 THEN 'Engineering' WHEN 1 THEN 'Data'
        WHEN 2 THEN 'Product'      ELSE 'Support'
    END                                                     AS department,
    CAST(50000 + MOD(abs(HASH(i * 7)), 95) * 1000 AS INTEGER) AS salary,
    CAST(DATE '2020-01-01' + CAST(i % 2000 AS INTEGER) AS DATE) AS hire_date
FROM range(80) AS t(i)
WHERE (SELECT count(*) FROM employees) = 0;

-- ---------------------------------------------------------------
-- Products (8) + Sales (6,000) — used for aggregates and the parquet /
-- hive-partition lessons. unit_price is a snapshot at sale time.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id         INTEGER PRIMARY KEY,
    name       VARCHAR,
    category   VARCHAR,
    unit_price DECIMAL(10, 2)
);

INSERT INTO products (id, name, category, unit_price)
SELECT id, name, category, unit_price FROM (VALUES
    (1, 'MacBook Pro',    'electronics', 2499.00),
    (2, 'Wireless Mouse', 'electronics', 12.99),
    (3, 'HD Monitor',     'electronics', 199.00),
    (4, 'Kettle',         'home',        34.50),
    (5, 'Desk Lamp',      'home',        41.00),
    (6, 'Clean Code',     'books',       39.99),
    (7, 'Germinal',       'books',       11.20),
    (8, 'Olive Oil',      'grocery',     15.40)
) AS p(id, name, category, unit_price)
WHERE (SELECT count(*) FROM products) = 0;

CREATE TABLE IF NOT EXISTS sales (
    sale_id    INTEGER PRIMARY KEY,
    sold_on    DATE,
    product_id INTEGER REFERENCES products(id),
    quantity   INTEGER,
    unit_price DECIMAL(10, 2)
);

INSERT INTO sales (sale_id, sold_on, product_id, quantity, unit_price)
SELECT
    i + 1                                                          AS sale_id,
    CAST(DATE '2026-01-01' + CAST(i % 90 AS INTEGER) AS DATE)      AS sold_on,
    CAST(1 + MOD(abs(HASH(i)), 8) AS INTEGER)                      AS product_id,
    CAST(1 + MOD(abs(HASH(i * 3)), 4) AS INTEGER)                  AS quantity,
    p.unit_price                                                   AS unit_price
FROM range(6000) AS t(i)
JOIN products p ON p.id = CAST(1 + MOD(abs(HASH(i)), 8) AS INTEGER)
WHERE (SELECT count(*) FROM sales) = 0;

-- ---------------------------------------------------------------
-- Places (5) — lon/lat points for the spatial extension lesson.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS places (
    name     VARCHAR,
    lon      DOUBLE,
    lat      DOUBLE,
    kind     VARCHAR
);

INSERT INTO places (name, lon, lat, kind)
SELECT name, lon, lat, kind FROM (VALUES
    ('Head Office',  153.025, -27.470, 'office'),
    ('Sydney Hub',   151.209, -33.869, 'office'),
    ('Melbourne Hub',144.963, -37.814, 'office'),
    ('Brisbane Depot',153.026, -27.472, 'warehouse'),
    ('Gold Coast Depot',153.400, -28.017, 'warehouse')
) AS p(name, lon, lat, kind)
WHERE (SELECT count(*) FROM places) = 0;