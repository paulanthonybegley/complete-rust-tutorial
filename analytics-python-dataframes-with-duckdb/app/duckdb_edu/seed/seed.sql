-- =====================================================================
-- Analytics from Python DataFrames with DuckDB — seed dataset
-- (idempotent, deterministic)
--
-- This file is the single source of truth for the course database. It is
-- executed:
--   * by the Python app on startup (app/duckdb_edu/seed.py)
--   * by the Docker "duckdb" CLI container on first start (/init/seed.sql)
--
-- DuckDB stores seed data in one file (analytics.duckdb). All statements are
-- idempotent: tables use IF NOT EXISTS and inserts are guarded by a row-count
-- check, so re-running is safe.
--
-- The dataset mirrors the video's example (employees.csv: names, job titles,
-- salaries) and adds a sales/product dimension so both the "SQL on a single
-- dataframe" lesson and the join-aggregate lessons have material to work on:
--   * employees  (200 rows) — the video's table, plus department + hire_date
--   * products   ( 10 rows) — four categories
--   * sales      (12,000 rows) — 180 days of purchases, quantity + snapshot price
--
-- "Randomness" is deterministic (hash of row index), so every run of the
-- course produces the SAME numbers — exercises have stable expected answers.
-- =====================================================================

-- ---------------------------------------------------------------
-- Employees (200): name, job title, department, salary, hire date
-- The video's data (name/job_title/salary) plus a bit of dimension.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employees (
    id          INTEGER PRIMARY KEY,
    name        VARCHAR,
    job_title   VARCHAR,
    department  VARCHAR,   -- Engineering | Data | Product | Support
    salary      INTEGER,   -- 50,000 .. 144,000 (the video's range)
    hire_date   DATE
);

INSERT INTO employees (id, name, job_title, department, salary, hire_date)
SELECT
    CAST(i + 1 AS INTEGER)                                            AS id,
    'Employee ' || (i + 1)                                      AS name,
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
    END                                                              AS job_title,
    CASE MOD(abs(HASH(i * 13)), 4)
        WHEN 0 THEN 'Engineering' WHEN 1 THEN 'Data'
        WHEN 2 THEN 'Product'      ELSE 'Support'
    END                                                              AS department,
    CAST(50000 + MOD(abs(HASH(i * 7)), 95) * 1000 AS INTEGER)        AS salary,
    CAST(DATE '2020-01-01' + CAST(i % 2000 AS INTEGER) AS DATE)      AS hire_date
FROM range(200) AS t(i)
WHERE (SELECT count(*) FROM employees) = 0;

-- ---------------------------------------------------------------
-- Products (10) across the four categories used by the exercises
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id           INTEGER PRIMARY KEY,
    name         VARCHAR,
    category     VARCHAR,
    unit_price   DECIMAL(10, 2)
);

INSERT INTO products (id, name, category, unit_price)
SELECT id, name, category, unit_price FROM (VALUES
    (1,  'MacBook Pro',     'electronics', 2499.00),
    (2,  'Wireless Mouse',  'electronics', 12.99),
    (3,  'HD Monitor',      'electronics', 199.00),
    (4,  'Kettle',          'home',        34.50),
    (5,  'Desk Lamp',       'home',        41.00),
    (6,  'Chess Set',       'home',        22.75),
    (7,  'Clean Code',      'books',       39.99),
    (8,  'Germinal',        'books',       11.20),
    (9,  'Organic Coffee',  'grocery',      8.90),
    (10, 'Olive Oil',       'grocery',     15.40)
) AS p(id, name, category, unit_price)
WHERE (SELECT count(*) FROM products) = 0;

-- ---------------------------------------------------------------
-- Sales (12,000) across 180 days from 2026-01-01.
-- unit_price is a SNAPSHOT (price at sale time), copied from product —
-- past invoices never change when the catalogue is repriced.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales (
    sale_id     INTEGER PRIMARY KEY,
    sold_on     DATE,
    product_id  INTEGER REFERENCES products(id),
    quantity    INTEGER,          -- 1..4
    unit_price  DECIMAL(10, 2)
);

INSERT INTO sales (sale_id, sold_on, product_id, quantity, unit_price)
SELECT
    i + 1                                                            AS sale_id,
    CAST(DATE '2026-01-01' + CAST(i % 180 AS INTEGER) AS DATE)       AS sold_on,
    CAST(1 + MOD(abs(HASH(i)), 10) AS INTEGER)                       AS product_id,
    CAST(1 + MOD(abs(HASH(i * 3)), 4) AS INTEGER)                    AS quantity,
    p.unit_price                                                     AS unit_price
FROM range(12000) AS t(i)
JOIN products p ON p.id = CAST(1 + MOD(abs(HASH(i)), 10) AS INTEGER)
WHERE (SELECT count(*) FROM sales) = 0;

-- ---------------------------------------------------------------
-- Denormalised convenience view: every sale with product names/categories
-- (a view stores the query, not the data).
-- ---------------------------------------------------------------
CREATE OR REPLACE VIEW v_sales_detail AS
SELECT
    s.sale_id,
    s.sold_on,
    p.name      AS product_name,
    p.category,
    s.quantity,
    s.unit_price,
    s.quantity * s.unit_price AS line_total
FROM sales s
JOIN products p ON p.id = s.product_id;