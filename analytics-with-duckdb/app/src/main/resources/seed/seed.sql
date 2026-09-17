-- =====================================================================
-- Analytics with DuckDB — seed dataset (idempotent, deterministic)
--
-- This file is the single source of truth for the course database. It is
-- executed:
--   * by the Spring app's DataInitializer (every start, app/src/main/resources)
--   * by the Docker "duckdb" CLI container on first start (/init/seed.sql)
--
-- DuckDB stores seed data in one file (analytics.duckdb). All statements are
-- idempotent: tables use IF NOT EXISTS and inserts are guarded by a row-count
-- check, so re-running is safe.
--
-- The data is "medium data" by the talk's definition: ~50k orders, ~250k
-- order lines, ~300k logs — large enough that aggregation and EXPLAIN show
-- columnar behaviour, small enough to regenerate in seconds.
--
-- "Randomness" is deterministic (hash of row index), so every run of the
-- course produces the SAME numbers — exercises have stable expected answers.
-- =====================================================================

-- ---------------------------------------------------------------
-- Reference: countries
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS country (
    id   INTEGER PRIMARY KEY,
    name VARCHAR
);

INSERT INTO country (id, name)
SELECT id, name FROM (VALUES
    (1,  'USA'),       (2,  'Canada'),     (3,  'UK'),
    (4,  'Germany'),   (5,  'France'),     (6,  'Spain'),
    (7,  'Italy'),     (8,  'Netherlands'),(9,  'Sweden'),
    (10, 'Norway'),    (11, 'Australia'),  (12, 'Japan'),
    (13, 'South Korea'),(14, 'Singapore'), (15, 'India'),
    (16, 'Brazil'),    (17, 'Mexico'),     (18, 'Argentina'),
    (19, 'South Africa'),(20, 'Egypt'),    (21, 'UAE'),
    (22, 'Israel'),    (23, 'Poland'),     (24, 'Czechia'),
    (25, 'Portugal'),  (26, 'Denmark'),    (27, 'Finland'),
    (28, 'Austria'),   (29, 'Switzerland'),(30, 'Belgium')
) AS c(id, name)
WHERE (SELECT count(*) FROM country) = 0;

-- ---------------------------------------------------------------
-- Customers (500), each hash-mapped to one of 30 countries
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer (
    id         INTEGER PRIMARY KEY,
    name       VARCHAR,
    email      VARCHAR,
    country_id INTEGER REFERENCES country(id),
    segment    VARCHAR            -- retail | wholesale | partner
);

INSERT INTO customer (id, name, email, country_id, segment)
SELECT
    CAST(i + 1 AS INTEGER)                                             AS id,
    'Customer ' || (i + 1)                                        AS name,
    'customer' || (i + 1) || '@example.com'                       AS email,
    CAST(1 + MOD(abs(HASH(i)), 30) AS INTEGER)                    AS country_id,
    CASE MOD(i, 3) WHEN 0 THEN 'retail' WHEN 1 THEN 'wholesale'
                   ELSE 'partner' END                             AS segment
FROM range(500) AS t(i)
WHERE (SELECT count(*) FROM customer) = 0;

-- ---------------------------------------------------------------
-- Products (10) across the four categories used by the exercises
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product (
    id         INTEGER PRIMARY KEY,
    name       VARCHAR,
    category   VARCHAR,
    unit_price DECIMAL(10, 2)
);

INSERT INTO product (id, name, category, unit_price)
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
WHERE (SELECT count(*) FROM product) = 0;

-- ---------------------------------------------------------------
-- Orders (50,000) across the 365 days of 2025
--   10% are cancelled — the rest completed
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id          BIGINT PRIMARY KEY,
    customer_id INTEGER REFERENCES customer(id),
    order_date  DATE,
    status      VARCHAR            -- completed | cancelled
);

INSERT INTO orders (id, customer_id, order_date, status)
SELECT
    i + 1                                             AS id,
    CAST(1 + MOD(abs(HASH(i)), 500) AS INTEGER)       AS customer_id,
    CAST(DATE '2025-01-01' + CAST(i % 365 AS INTEGER) AS DATE) AS order_date,
    CASE WHEN MOD(abs(HASH(i * 1009)), 10) = 0
         THEN 'cancelled' ELSE 'completed' END        AS status
FROM range(50000) AS t(i)
WHERE (SELECT count(*) FROM orders) = 0;

-- ---------------------------------------------------------------
-- Order lines (~250k): 1..9 lines per order, quantity 1..4.
-- unit_price is a SNAPSHOT (the price at order time), which is why it is
-- copied into this table instead of being looked up from product live.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_lines (
    order_id   BIGINT REFERENCES orders(id),
    product_id INTEGER REFERENCES product(id),
    quantity   INTEGER,
    unit_price DECIMAL(10, 2)
);

INSERT INTO order_lines (order_id, product_id, quantity, unit_price)
SELECT
    o.id                                                    AS order_id,
    CAST(1 + MOD(abs(HASH(o.id + l.n * 13)), 10) AS INTEGER) AS product_id,
    CAST(1 + MOD(abs(HASH(o.id * 7 + l.n * 29)), 4) AS INTEGER) AS quantity,
    p.unit_price                                            AS unit_price
FROM orders o
CROSS JOIN range(CAST(1 + MOD(abs(HASH(o.id)), 9) AS INTEGER)) AS l(n)
JOIN product p ON p.id = 1 + MOD(abs(HASH(o.id + l.n * 13)), 10)
WHERE (SELECT count(*) FROM order_lines) = 0;

-- ---------------------------------------------------------------
-- Events (300,000) — application logs, the course's "dark data".
-- ~1% ERROR, ~1% WARN, ~0.1% FATAL, the rest INFO (lesson 5).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS events (
    id      BIGINT PRIMARY KEY,
    ts      TIMESTAMP,
    level   VARCHAR,             -- INFO | WARN | ERROR | FATAL
    service VARCHAR,             -- cart | checkout | gateway
    message VARCHAR
);

INSERT INTO events (id, ts, level, service, message)
SELECT
    i                                                            AS id,
    TIMESTAMP '2026-01-01 00:00:00'
        + (CAST(i AS INTEGER) % 43200) * INTERVAL '1 minute'     AS ts,
    CASE
        WHEN MOD(abs(HASH(i)), 1000) = 0 THEN 'FATAL'
        WHEN MOD(abs(HASH(i)), 100) = 0  THEN 'ERROR'
        WHEN MOD(abs(HASH(i)), 50) = 0   THEN 'WARN'
        ELSE 'INFO'
    END                                                          AS level,
    CASE MOD(i, 3) WHEN 0 THEN 'cart'
                   WHEN 1 THEN 'checkout' ELSE 'gateway' END     AS service,
    'operation #' || i                                           AS message
FROM range(300000) AS t(i)
WHERE (SELECT count(*) FROM events) = 0;

-- ---------------------------------------------------------------
-- Convenience view (lesson 6: views vs tables) — completed orders
-- with the product names/categories denormalised, then read each
-- time it is queried (a view stores the query, not the data).
-- ---------------------------------------------------------------
CREATE OR REPLACE VIEW v_completed_orders AS
SELECT
    o.id         AS order_id,
    o.order_date,
    o.customer_id,
    pr.name      AS product_name,
    pr.category,
    ol.quantity,
    ol.unit_price,
    ol.quantity * ol.unit_price AS line_total
FROM orders o
JOIN order_lines ol ON ol.order_id = o.id
JOIN product pr     ON pr.id      = ol.product_id
WHERE o.status = 'completed';