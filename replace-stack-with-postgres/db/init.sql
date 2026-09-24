CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS hstore;

CREATE ROLE demo_app LOGIN PASSWORD 'demo_app';
GRANT usage ON SCHEMA public TO demo_app;

CREATE OR REPLACE FUNCTION text_embedding(p_input text, dims int DEFAULT 32)
RETURNS vector
LANGUAGE plpgsql
AS $$
DECLARE
  t text := lower(coalesce(p_input, ''));
  buf text := ' ' || t || ' ';
  vec float8[] := array_fill(0.0, ARRAY[dims]);
  norm float8 := 0;
  h int := 0;
  i int;
  j int;
  trigram text;
BEGIN
  FOR i IN 1 .. length(buf) - 2 LOOP
    trigram := substr(buf, i, 3);
    h := mod(hashtext(trigram), dims);
    IF h < 0 THEN h := h + dims; END IF;
    vec[h + 1] := vec[h + 1] + 1;
  END LOOP;
  FOR j IN 1 .. dims LOOP
    norm := norm + vec[j] * vec[j];
  END LOOP;
  IF norm > 0 THEN
    FOR j IN 1 .. dims LOOP
      vec[j] := vec[j] / sqrt(norm);
    END LOOP;
  END IF;
  RETURN vec::vector;
END $$;

CREATE TABLE products (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  price NUMERIC(10, 2) NOT NULL,
  attributes JSONB NOT NULL DEFAULT '{}'
);

INSERT INTO products (name, category, price, attributes) VALUES
  ('iPhone 15', 'phone', 899.00, '{"brand":"Apple","os":"iOS","storage_gb":256,"color":"black","specs":{"cpu":"A16 Bionic","ram_gb":6}}'),
  ('Pixel 8', 'phone', 649.00, '{"brand":"Google","os":"Android","storage_gb":128,"color":"sage","specs":{"cpu":"Tensor G3","ram_gb":8}}'),
  ('Galaxy S24', 'phone', 799.00, '{"brand":"Samsung","os":"Android","storage_gb":256,"color":"violet","specs":{"cpu":"Snapdragon 8 Gen 3","ram_gb":12}}'),
  ('MacBook Air M3', 'laptop', 1099.00, '{"brand":"Apple","os":"macOS","storage_gb":512,"color":"midnight","specs":{"cpu":"Apple M3","ram_gb":16}}'),
  ('ThinkPad X1', 'laptop', 1399.00, '{"brand":"Lenovo","os":"Windows","storage_gb":1024,"color":"black","specs":{"cpu":"Intel Core i7","ram_gb":32}}'),
  ('Dell XPS 13', 'laptop', 1199.00, '{"brand":"Dell","os":"Windows","storage_gb":512,"color":"platinum","specs":{"cpu":"Intel Core Ultra 7","ram_gb":16}}'),
  ('Nikon Z8', 'camera', 3799.00, '{"brand":"Nikon","type":"mirrorless","sensor_mp":45.7,"specs":{"cpu":"EXPEED 7"}}'),
  ('Sony A7 IV', 'camera', 2499.00, '{"brand":"Sony","type":"mirrorless","sensor_mp":33.0,"specs":{"cpu":"BIONZ XR"}}'),
  ('GoPro Hero 12', 'camera', 399.00, '{"brand":"GoPro","waterproof":true,"specs":{"sensor":"1/1.9 inch","cpu":"GP2"}}'),
  ('Fender Stratocaster', 'guitar', 1199.00, '{"brand":"Fender","type":"electric","strings":6,"specs":{"pickups":"3x single coil"}}'),
  ('Martin D-28', 'guitar', 3299.00, '{"brand":"Martin","type":"acoustic","strings":6,"specs":{"body":"dreadnought"}}'),
  ('Kindle Paperwhite', 'ereader', 149.00, '{"brand":"Amazon","e_ink":true,"specs":{"ram_gb":1}}'),
  ('LG UltraFine 27', 'monitor', 699.00, '{"brand":"LG","type":"4K","specs":{"panel":"IPS","resolution":"4K","ports":["HDMI","DP","USB-C"]}}'),
  ('AirPods Pro 2', 'audio', 249.00, '{"brand":"Apple","type":"earbuds","specs":{"chip":"H2"}}'),
  ('Bose QC Ultra', 'audio', 429.00, '{"brand":"Bose","type":"headphones","noise_cancelling":true,"specs":{"driver":"28mm"}}');

CREATE INDEX idx_products_attributes ON products USING GIN (attributes);

CREATE TABLE jobs (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  kind TEXT NOT NULL,
  payload JSONB NOT NULL,
  status TEXT NOT NULL DEFAULT 'pending',
  attempts INT NOT NULL DEFAULT 0,
  max_attempts INT NOT NULL DEFAULT 3,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  started_at TIMESTAMPTZ,
  finished_at TIMESTAMPTZ,
  last_error TEXT,
  result TEXT
);

CREATE INDEX idx_jobs_claim ON jobs (kind, created_at) WHERE status = 'pending';
CREATE INDEX idx_jobs_status ON jobs (status);

INSERT INTO jobs (kind, payload, status, attempts, created_at, started_at, finished_at, result) VALUES
  ('email', '{"to":"ada@example.com","subject":"Weekly digest"}', 'done', 1, now() - interval '2 hours', now() - interval '2 hours' + interval '1 sec', now() - interval '2 hours' + interval '9 sec', 'sent'),
  ('email', '{"to":"bob@example.com","subject":"Welcome"}', 'done', 1, now() - interval '100 minutes', now() - interval '100 minutes' + interval '3 sec', now() - interval '100 minutes' + interval '13 sec', 'sent'),
  ('report', '{"rows":1200,"format":"csv"}', 'done', 1, now() - interval '80 minutes', now() - interval '80 minutes' + interval '1 sec', now() - interval '80 minutes' + interval '21 sec', 'generated 1200 rows'),
  ('thumbnail', '{"image_id":77}', 'done', 1, now() - interval '50 minutes', now() - interval '50 minutes' + interval '2 sec', now() - interval '50 minutes' + interval '5 sec', '640x360'),
  ('email', '{"to":"carol@example.com","subject":"Failed payment"}', 'failed', 3, now() - interval '45 minutes', now() - interval '45 minutes' + interval '2 sec', now() - interval '45 minutes' + interval '30 sec', NULL),
  ('report', '{"rows":400,"format":"pdf"}', 'done', 2, now() - interval '30 minutes', now() - interval '30 minutes' + interval '1 sec', now() - interval '30 minutes' + interval '19 sec', 'generated 400 rows'),
  ('thumbnail', '{"image_id":78}', 'done', 1, now() - interval '15 minutes', now() - interval '15 minutes' + interval '2 sec', now() - interval '15 minutes' + interval '6 sec', '640x360'),
  ('email', '{"to":"dave@example.com","subject":"Invoice"}', 'pending', 0, now() - interval '2 minutes', NULL, NULL, NULL),
  ('report', '{"rows":2500,"format":"xlsx"}', 'pending', 0, now() - interval '1 minute', NULL, NULL, NULL),
  ('email', '{"to":"erin@example.com","subject":"Newsletter"}', 'pending', 0, now(), NULL, NULL, NULL);

CREATE TABLE articles (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title TEXT NOT NULL,
  body TEXT NOT NULL,
  author TEXT NOT NULL,
  tag TEXT NOT NULL,
  published_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  search_vector tsvector GENERATED ALWAYS AS (
    setweight(to_tsvector('english', title), 'A') ||
    setweight(to_tsvector('english', body), 'B')
  ) STORED
);

INSERT INTO articles (title, body, author, tag, published_at) VALUES
  ('Postgres JSONB with GIN indexes', 'JSONB stores arbitrary documents as binary JSON. A generalized inverted index maps every key and value directly to row ids, so querying deeply nested properties is fast instead of scanning the whole table.', 'alice', 'database', now() - interval '40 days'),
  ('Using JSONB as a document store', 'You can treat a Postgres table like MongoDB. Store flexible documents in a JSONB column, add a GIN index, then still join that data with fully relational tables in one ACID transaction.', 'bob', 'database', now() - interval '38 days'),
  ('Postgres full text search guide', 'Postgres ships language aware search through tsvector and tsquery. Text is parsed, stop words are removed and words are stemmed to their roots, so running and run match the same query.', 'carol', 'search', now() - interval '35 days'),
  ('How trigrams make search typo tolerant', 'The pg_trgm extension breaks text into three-letter chunks and compares the overlap. A misspelled word still shares enough trigrams with the correct one that the right result is returned anyway.', 'dave', 'search', now() - interval '33 days'),
  ('Running Postgres in Docker', 'The official postgres image makes it trivial to spin up an isolated database with docker compose. Mount an init script into docker-entrypoint-initdb.d and your schema is created on first boot.', 'alice', 'devops', now() - interval '30 days'),
  ('Postgres on Docker: a beginner guide', 'A complete walkthrough of running Postgresql in containers, keeping data in a named volume, and connecting from local tools and applications.', 'bob', 'devops', now() - interval '29 days'),
  ('Redis cache eviction patterns', 'Caches evict stale entries and expire with TTLs. The same behaviours are easy to model in Postgres with an expires_at column, an unlogged table for speed, and a background sweep for eviction.', 'carol', 'backends', now() - interval '26 days'),
  ('SKIP LOCKED for safe job queues', 'FOR UPDATE SKIP LOCKED lets many workers claim rows concurrently. Each worker locks the row it grabs and skips rows already locked by others, turning an ordinary table into a wait free queue.', 'dave', 'backends', now() - interval '24 days'),
  ('Postgres as a message queue', 'No RabbitMQ required. Insert a job row, let workers poll with FOR UPDATE SKIP LOCKED, retry failures with backoff, and get ACID guarantees that most brokers cannot offer you.', 'alice', 'backends', now() - interval '22 days'),
  ('pgvector and AI search', 'The pgvector extension stores embeddings as a native column type right next to your relational data. That kills the hybrid search problem, keeping vectors and records in one place.', 'bob', 'ai', now() - interval '20 days'),
  ('Vector similarity with HNSW indexes', 'Hierarchical navigable small world indexes build a multi-layer graph over your vectors. Searches start with long-range hops at the top and refine down to exact neighbors, so top-k similarity is milliseconds.', 'dave', 'ai', now() - interval '18 days'),
  ('Relational filters on vector search', 'The killer feature of pgvector is filtering. Restrict by author and date in the same SQL statement as the similarity search, instead of cross-referencing two databases over the network.', 'alice', 'ai', now() - interval '16 days'),
  ('PostGIS spatial queries tutorial', 'PostGIS turns Postgres into a spatial powerhouse. A GiST index draws bounding boxes around geometries so the planner can discard millions of points before running precise math on the few that remain.', 'carol', 'database', now() - interval '14 days'),
  ('Time series with partitioning and BRIN', 'Declarative partitioning hides thousands of rows behind physical per month tables. A BRIN index then stores only min and max timestamps per disk block, skipping entire blocks during range scans.', 'dave', 'database', now() - interval '12 days'),
  ('Materialized views for dashboards', 'Heavy dashboard aggregations run once and the result is stored on disk. REFRESH MATERIALIZED VIEW CONCURRENTLY recomputes in the background and hot swaps rows without ever locking the dashboard.', 'alice', 'database', now() - interval '10 days'),
  ('Row level security in Postgres', 'Policies attached to a table decide which rows each authenticated user can see. The database itself enforces tenancy, so a badly written query can never leak another user''s rows.', 'bob', 'database', now() - interval '8 days'),
  ('PostgREST: instant REST API', 'Point PostgREST at any schema and it generates a complete REST API from your tables and views. Add a table, get an endpoint. No controller code and no middleware to maintain.', 'carol', 'backends', now() - interval '6 days'),
  ('HTMX and server side rendering', 'Combine HTMX attributes with templated server pages for a very lean frontend architecture. The server stays the single source of truth and the browser just swaps small HTML fragments.', 'alice', 'web', now() - interval '4 days');

CREATE INDEX idx_articles_fts ON articles USING GIN (search_vector);
CREATE INDEX idx_articles_title_trgm ON articles USING GIN (title gin_trgm_ops);

CREATE TABLE documents (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title TEXT NOT NULL,
  body TEXT NOT NULL,
  author TEXT NOT NULL,
  tag TEXT NOT NULL,
  created_at DATE NOT NULL DEFAULT CURRENT_DATE,
  embedding vector(32)
);

INSERT INTO documents (title, body, author, tag, created_at, embedding) VALUES
  ('Postgres JSONB with GIN indexes', 'How jsonb documents are indexed with an inverted index for fast nested queries.', 'alice', 'database', CURRENT_DATE - 40, text_embedding('Postgres JSONB with GIN indexes How jsonb documents are indexed with an inverted index for fast nested queries')),
  ('Using JSONB as a document store', 'Treat a table like a document database and still keep relational integrity.', 'bob', 'database', CURRENT_DATE - 38, text_embedding('Using JSONB as a document store Treat a table like a document database and still keep relational integrity')),
  ('Postgres full text search guide', 'Language aware search with tsvector and stemming for natural language queries.', 'carol', 'search', CURRENT_DATE - 35, text_embedding('Postgres full text search guide Language aware search with tsvector and stemming for natural language queries')),
  ('Trigram fuzzy matching', 'The pg_trigram extension finds results even when the user makes a typo.', 'dave', 'search', CURRENT_DATE - 33, text_embedding('Trigram fuzzy matching The pg_trigram extension finds results even when the user makes a typo')),
  ('Running Postgres in Docker', 'Spin up postgresql with docker compose and initialize schema with an init script.', 'alice', 'devops', CURRENT_DATE - 30, text_embedding('Running Postgres in Docker Spin up postgresql with docker compose and initialize schema with an init script')),
  ('Docker Compose for databases', 'Declarative multi container setup with volumes and health checks.', 'bob', 'devops', CURRENT_DATE - 29, text_embedding('Docker Compose for databases Declarative multi container setup with volumes and health checks')),
  ('Cache eviction patterns', 'Model TTL expiry in a regular table with an expires_at column and a sweep job.', 'carol', 'backends', CURRENT_DATE - 26, text_embedding('Cache eviction patterns Model TTL expiry in a regular table with an expires_at column and a sweep job')),
  ('FOR UPDATE SKIP LOCKED queues', 'Safe concurrent job claiming without a dedicated message broker.', 'dave', 'backends', CURRENT_DATE - 24, text_embedding('FOR UPDATE SKIP LOCKED queues Safe concurrent job claiming without a dedicated message broker')),
  ('Postgres as a message queue', 'Job tables with retries and dead letters replace RabbitMQ and Redis for most apps.', 'alice', 'backends', CURRENT_DATE - 22, text_embedding('Postgres as a message queue Job tables with retries and dead letters replace RabbitMQ and Redis for most apps')),
  ('pgvector embeddings', 'Store high dimensional vectors next to application records as a native column type.', 'bob', 'ai', CURRENT_DATE - 20, text_embedding('pgvector embeddings Store high dimensional vectors next to application records as a native column type')),
  ('HNSW approximate nearest neighbor', 'Graph based index for millisecond vector similarity searches.', 'dave', 'ai', CURRENT_DATE - 18, text_embedding('HNSW approximate nearest neighbor Graph based index for millisecond vector similarity searches')),
  ('Hybrid vector and relational filter', 'Filter vector results by author and date in the very same SQL statement.', 'alice', 'ai', CURRENT_DATE - 16, text_embedding('Hybrid vector and relational filter Filter vector results by author and date in the very same SQL statement')),
  ('PostGIS spatial queries', 'GiST indexes and geography types for radius and polygon queries at scale.', 'carol', 'database', CURRENT_DATE - 14, text_embedding('PostGIS spatial queries GiST indexes and geography types for radius and polygon queries at scale')),
  ('Partitioning and BRIN indexes', 'Range partitions and block range indexes for fast time series scans.', 'dave', 'database', CURRENT_DATE - 12, text_embedding('Partitioning and BRIN indexes Range partitions and block range indexes for fast time series scans')),
  ('Materialized views for analytics', 'Stored aggregations refreshed concurrently for lock free dashboards.', 'alice', 'database', CURRENT_DATE - 10, text_embedding('Materialized views for analytics Stored aggregations refreshed concurrently for lock free dashboards')),
  ('Row level security', 'Policies that make the database itself enforce who can see each row.', 'bob', 'database', CURRENT_DATE - 8, text_embedding('Row level security Policies that make the database itself enforce who can see each row')),
  ('PostgREST instant API', 'Generate a REST endpoint from every table and view without middleware.', 'carol', 'backends', CURRENT_DATE - 6, text_embedding('PostgREST instant API Generate a REST endpoint from every table and view without middleware')),
  ('HTMX server side rendering', 'Lean frontends that swap HTML fragments and keep all logic on the server.', 'alice', 'web', CURRENT_DATE - 4, text_embedding('HTMX server side rendering Lean frontends that swap HTML fragments and keep all logic on the server')),
  ('Server rendered app patterns', 'Templates rendered entirely on the server with small script boosters.', 'dave', 'web', CURRENT_DATE - 2, text_embedding('Server rendered app patterns Templates rendered entirely on the server with small script boosters'));

CREATE INDEX idx_documents_embedding ON documents USING hnsw (embedding vector_cosine_ops);

CREATE TABLE coffee_shops (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name TEXT NOT NULL,
  chain TEXT,
  rating NUMERIC(2, 1) NOT NULL,
  location geography(Point, 4326) NOT NULL
);

INSERT INTO coffee_shops (name, chain, rating, location) VALUES
  ('Monmouth Coffee', 'Monmouth', 4.8, ST_GeogFromText('POINT(-0.1273 51.5145)')),
  ('Ozone Coffee Roasters', 'Ozone', 4.6, ST_GeogFromText('POINT(-0.0856 51.5272)')),
  ('Kaffeine', 'Kaffeine', 4.7, ST_GeogFromText('POINT(-0.1358 51.5168)')),
  ('Attendant', NULL, 4.6, ST_GeogFromText('POINT(-0.1390 51.5172)')),
  ('Notes Coffee', 'Notes', 4.2, ST_GeogFromText('POINT(-0.1258 51.5078)')),
  ('Department of Coffee', NULL, 4.5, ST_GeogFromText('POINT(-0.1197 51.5112)')),
  ('The Gentlemen Baristas', NULL, 4.7, ST_GeogFromText('POINT(-0.0820 51.5074)')),
  ('Grind', 'Grind', 4.3, ST_GeogFromText('POINT(-0.1057 51.5333)')),
  ('Prufrock Coffee', NULL, 4.6, ST_GeogFromText('POINT(-0.1136 51.5231)')),
  ('Origin Coffee', 'Origin', 4.5, ST_GeogFromText('POINT(-0.0831 51.5134)')),
  ('Rosslyn Coffee', NULL, 4.7, ST_GeogFromText('POINT(-0.1159 51.5122)')),
  ('Redemption Roasters', 'Redemption', 4.4, ST_GeogFromText('POINT(-0.1308 51.5173)')),
  ('Flat White', NULL, 4.5, ST_GeogFromText('POINT(-0.1317 51.5153)')),
  ('Timberyard', NULL, 4.6, ST_GeogFromText('POINT(-0.1205 51.5132)')),
  ('Store Street Espresso', NULL, 4.5, ST_GeogFromText('POINT(-0.1312 51.5189)')),
  ('Lantana', NULL, 4.4, ST_GeogFromText('POINT(-0.1322 51.5228)')),
  ('Caravan Bankside', 'Caravan', 4.4, ST_GeogFromText('POINT(-0.0990 51.5073)')),
  ('Black Sheep Coffee', 'Black Sheep', 4.2, ST_GeogFromText('POINT(-0.0887 51.5112)')),
  ('Blank Street Coffee', 'Blank Street', 4.1, ST_GeogFromText('POINT(-0.0870 51.5117)')),
  ('Refuge Coffee Brixton', NULL, 4.3, ST_GeogFromText('POINT(-0.1140 51.4613)')),
  ('Peckham Rye Espresso', NULL, 4.2, ST_GeogFromText('POINT(-0.0800 51.4658)')),
  ('Stratford Grind', 'Grind', 4.0, ST_GeogFromText('POINT(-0.0012 51.5431)'));

CREATE INDEX idx_coffee_shops_location ON coffee_shops USING GIST (location);

CREATE TABLE events (
  id BIGINT NOT NULL,
  device TEXT NOT NULL,
  metric TEXT NOT NULL,
  value DOUBLE PRECISION NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL
) PARTITION BY RANGE (occurred_at);

CREATE TABLE events_2025_01 PARTITION OF events FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE events_2025_02 PARTITION OF events FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
CREATE TABLE events_2025_03 PARTITION OF events FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');
CREATE TABLE events_2025_04 PARTITION OF events FOR VALUES FROM ('2025-04-01') TO ('2025-05-01');
CREATE TABLE events_2025_05 PARTITION OF events FOR VALUES FROM ('2025-05-01') TO ('2025-06-01');
CREATE TABLE events_2025_06 PARTITION OF events FOR VALUES FROM ('2025-06-01') TO ('2025-07-01');
CREATE TABLE events_default PARTITION OF events DEFAULT;

CREATE INDEX idx_events_brin ON events USING brin (occurred_at) WITH (pages_per_range = 32);

INSERT INTO events (id, device, metric, value, occurred_at)
SELECT
  s.i,
  'device-' || (1 + floor(random() * 200))::int,
  (ARRAY['cpu_usage', 'memory_usage', 'network_rx', 'disk_io'])[1 + floor(random() * 4)::int],
  round((random() * 100)::numeric, 2)::double precision,
  timestamp '2025-01-01 00:00:00' + ((s.i - 1) / 100) * interval '1 hour' + random() * interval '55 minutes'
FROM generate_series(1, 360000) AS s(i);

INSERT INTO events (id, device, metric, value, occurred_at)
SELECT
  s.i,
  'device-' || (1 + floor(random() * 50))::int,
  (ARRAY['cpu_usage', 'memory_usage'])[1 + floor(random() * 2)::int],
  round((random() * 100)::numeric, 2)::double precision,
  now() - random() * interval '1 day'
FROM generate_series(1, 2000) AS s(i);

ANALYZE events;

CREATE TABLE orders (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product TEXT NOT NULL,
  customer TEXT NOT NULL,
  units INT NOT NULL,
  unit_price NUMERIC(10, 2) NOT NULL,
  ordered_on DATE NOT NULL
);

INSERT INTO orders (product, customer, units, unit_price, ordered_on)
SELECT
  (ARRAY['Espresso Machine', 'Coffee Beans 1kg', 'Pour-Over Kettle', 'Burr Grinder', 'Mug Set', 'Filter Papers', 'Tamper', 'Coffee Scale'])[1 + floor(random() * 8)::int],
  (ARRAY['alice', 'bob', 'carol', 'dave', 'erin', 'frank', 'grace', 'henry', 'ivy', 'jack', 'kate', 'leo'])[1 + floor(random() * 12)::int],
  1 + floor(random() * 4)::int,
  round((random() * 120 + 10)::numeric, 2),
  CURRENT_DATE - (floor(random() * 90)::int)
FROM generate_series(1, 1100);

ANALYZE orders;

CREATE MATERIALIZED VIEW mv_daily_sales AS
SELECT ordered_on, product, SUM(units) AS units, SUM(units * unit_price) AS revenue, COUNT(*) AS orders
FROM orders
GROUP BY ordered_on, product;

CREATE UNIQUE INDEX idx_mv_daily_sales ON mv_daily_sales (ordered_on, product);

CREATE TABLE app_meta (
  key TEXT PRIMARY KEY,
  value TEXT NOT NULL
);

INSERT INTO app_meta (key, value) VALUES ('mv_refreshed_at', now()::text);

CREATE TABLE app_users (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name TEXT UNIQUE NOT NULL
);

INSERT INTO app_users (name) VALUES ('alice'), ('bob'), ('carol');

CREATE TABLE private_notes (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  owner_id INT NOT NULL REFERENCES app_users (id),
  content TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO private_notes (owner_id, content) VALUES
  (1, 'Q3 roadmap draft for the payments team is in the shared drive.'),
  (1, 'Ask ops about the connection pool limits before the launch.'),
  (1, 'Remember to review the migration pull request today.'),
  (2, 'Fix the flaky integration test in the checkout module.'),
  (2, 'Customer 481 reported a duplicate invoice, escalate to billing.'),
  (2, 'Move the analytics dashboard to the materialized view this week.'),
  (3, 'Schema change for the events table needs a maintenance window.'),
  (3, 'Prepare the demo of row level security for the security review.'),
  (3, 'Update the runbook entry for the job queue worker.'),
  (1, 'Book the retro room for friday morning.'),
  (1, 'The brave new jsonb catalog search is so fast with GIN.'),
  (2, 'Remember to rotate the demo_app password after the demo.');

ALTER TABLE private_notes ENABLE ROW LEVEL SECURITY;
ALTER TABLE private_notes FORCE ROW LEVEL SECURITY;

CREATE POLICY notes_select ON private_notes FOR SELECT
  USING (owner_id = current_setting('app.current_user', true)::int);

CREATE POLICY notes_insert ON private_notes FOR INSERT
  WITH CHECK (owner_id = current_setting('app.current_user', true)::int);

CREATE POLICY notes_update ON private_notes FOR UPDATE
  USING (owner_id = current_setting('app.current_user', true)::int);

CREATE POLICY notes_delete ON private_notes FOR DELETE
  USING (owner_id = current_setting('app.current_user', true)::int);

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO demo_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO demo_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO demo_app;
GRANT EXECUTE ON ALL PROCEDURES IN SCHEMA public TO demo_app;
GRANT EXECUTE ON FUNCTION public.text_embedding(text) TO demo_app;

ANALYZE products;
ANALYZE jobs;
ANALYZE articles;
ANALYZE documents;
ANALYZE coffee_shops;
ANALYZE private_notes;