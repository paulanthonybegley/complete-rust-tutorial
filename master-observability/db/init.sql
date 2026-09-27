-- master-observability: the "shop" that misbehaves on purpose.
-- Every lab reads/writes this schema, and the observability stack (Loki, Prometheus,
-- Jaeger, Grafana) is pointed at the app so learners can correlate what they see here
-- with what they see in the dashboards.

CREATE ROLE observability_app LOGIN PASSWORD 'observability_app';

CREATE TABLE customers (
    id          integer PRIMARY KEY,
    email       text NOT NULL,
    name        text NOT NULL,
    tier        text NOT NULL DEFAULT 'standard',   -- standard | vip
    created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE products (
    id       integer PRIMARY KEY,
    sku      text NOT NULL,
    name     text NOT NULL,
    price    numeric(8,2) NOT NULL,
    stock    integer NOT NULL
);

CREATE TABLE orders (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id integer NOT NULL REFERENCES customers(id),
    product_id  integer NOT NULL REFERENCES products(id),
    qty         integer NOT NULL,
    total       numeric(8,2) NOT NULL,
    status      text NOT NULL,              -- completed | failed
    error_code  text,                       -- card_declined | insufficient_funds | timeout | fraud_check
    trace_id    text,                       -- the request that produced this order
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- the sampling lab records what the app decided to keep/drop, and why.
CREATE TABLE trace_decisions (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trace_id    text NOT NULL,
    endpoint    text NOT NULL,
    kept        boolean NOT NULL,
    reason      text NOT NULL,
    duration_ms integer NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- Alertmanager posts fired alerts here (via a webhook receiver) so the /alerts lab
-- can show "what would have paged someone" and teach alert fatigue.
CREATE TABLE alert_events (
    id           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fingerprint  text NOT NULL,
    alert_name   text NOT NULL,
    status       text NOT NULL,
    starts_at    timestamptz,
    labels       jsonb NOT NULL DEFAULT '{}'::jsonb,
    annotations  jsonb NOT NULL DEFAULT '{}'::jsonb,
    received_at  timestamptz NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Seed data (deterministic: same hashes => same data on every fresh build)
-- ---------------------------------------------------------------------------

INSERT INTO customers (id, email, name, tier, created_at) VALUES
    (1, 'alice@example.com',    'Alice Adeyemi',    'vip',      now() - interval '400 days'),
    (2, 'bob@example.com',      'Bob Bellamy',      'standard', now() - interval '380 days'),
    (3, 'carol@example.com',    'Carol Chen',       'vip',      now() - interval '350 days'),
    (4, 'dave@example.com',     'Dave Dlamini',     'standard', now() - interval '310 days'),
    (5, 'erin@example.com',     'Erin El-Amin',     'standard', now() - interval '290 days'),
    (6, 'frank@example.com',    'Frank Ferrara',    'standard', now() - interval '250 days'),
    (7, 'grace@example.com',    'Grace Gulyas',     'vip',      now() - interval '210 days'),
    (8, 'henry@example.com',    'Henry Haddad',     'standard', now() - interval '160 days'),
    (9, 'ines@example.com',     'Ines Ito',         'standard', now() - interval '120 days'),
    (10,'jack@example.com',     'Jack Jensen',      'standard', now() - interval '90 days'),
    (11,'kofi@example.com',     'Kofi Kwarteng',    'vip',      now() - interval '60 days'),
    (12,'lucy@example.com',     'Lucy Lindqvist',   'standard', now() - interval '30 days');

INSERT INTO products (id, sku, name, price, stock) VALUES
    (1,  'BEAN-EO-001', 'Ethiopian Yirgacheffe 250g',      14.50, 120),
    (2,  'BEAN-EO-002', 'Kenyan AA 250g',                  15.00,  80),
    (3,  'BEAN-EO-003', 'Brazilian Cerrado 1kg',           32.00,  45),
    (4,  'BEAN-EO-004', 'Colombian Supremo 1kg',           31.50,  95),
    (5,  'BEAN-EO-005', 'Sumatra Mandheling 250g',         13.80,  60),
    (6,  'GRIND-001',   'Burr Grinder Home',              129.00,  12),
    (7,  'GRIND-002',   'Burr Grinder Pro',               249.00,   6),
    (8,  'MACH-001',    'Filter Machine Home',            189.00,  18),
    (9,  'MACH-002',    'Espresso Machine Barista',       649.00,   3),
    (10, 'MUG-001',     'Ceramic Mug 350ml',               18.00, 200),
    (11, 'MUG-002',     'Travel Mug 470ml',                24.00, 150),
    (12, 'KETTLE-001',  'Gooseneck Kettle',                79.00,  25);

-- ~6000 orders spread over 60 days. Roughly 8% fail, with a weighted error_code mix.
-- Deterministic via md5() so every rebuild is identical and every exercise answerable.
WITH codes AS (
    SELECT unnest(ARRAY['card_declined','insufficient_funds','timeout','fraud_check']) AS error_code
), series AS (
    SELECT gs                                                         AS i,
           (abs(hashtext('o' || gs::text)))::int                     AS h1,
           (abs(hashtext('p' || gs::text)))::int                     AS h2
    FROM generate_series(1, 6000) AS gs
)
INSERT INTO orders (customer_id, product_id, qty, total, status, error_code, trace_id, created_at)
SELECT
    1 + (h2 % 12)                       AS customer_id,
    1 + (h1 % 12)                       AS product_id,
    1 + (h1 % 4)                        AS qty,
    (SELECT price FROM products WHERE id = 1 + (h1 % 12)) * (1 + (h1 % 4)) AS total,
    CASE WHEN h2 % 100 < 8 THEN 'failed' ELSE 'completed' END AS status,
    CASE WHEN h2 % 100 < 8 THEN
        (ARRAY['card_declined','card_declined','insufficient_funds','timeout','fraud_check'])[1 + (h2 % 5)]
    END                                 AS error_code,
    CASE WHEN h2 % 100 < 8 THEN
        'legacy-' || substr(md5('trace' || i), 1, 16)
    END                                 AS trace_id,
    now() - ((i % 60) || ' days')::interval - ((i % 2880) || ' minutes')::interval
FROM series;

-- role the app uses
GRANT CONNECT ON DATABASE postgres TO observability_app;
GRANT USAGE ON SCHEMA public TO observability_app;
GRANT SELECT, INSERT ON orders TO observability_app;
GRANT SELECT, UPDATE ON products TO observability_app;
GRANT SELECT ON customers TO observability_app;
GRANT SELECT, INSERT ON trace_decisions TO observability_app;
GRANT SELECT, INSERT ON alert_events TO observability_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO observability_app;

ALTER TABLE orders OWNER TO postgres;
ALTER TABLE products OWNER TO postgres;