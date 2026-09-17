-- One database is enough (Law 5) while this schema stays small. Below are the
-- indexes exactly as the access patterns (Law 1) require. On Postgres you can
-- hand-apply them to replace ddl-auto:update:
--
--   su - postgres -c 'psql -d dblaws -f schema-indexes.sql'
--
-- Law 3 exercise: run EXPLAIN ANALYZE before and after each index to see the
-- plan change from Seq Scan to Index Scan.

-- Hot read: "orders page for a customer" (OrderRepository#findWithItemsByCustomer_Id)
CREATE INDEX IF NOT EXISTS idx_orders_customer_id
  ON customer_orders (customer_id);

-- Hot aggregate: "sales per month" groups on created_at (OrderRepository#monthlySales)
CREATE INDEX IF NOT EXISTS idx_orders_created_at
  ON customer_orders (created_at);

-- Catalogue filters by category and sorting by name (ProductSpecifications)
CREATE INDEX IF NOT EXISTS idx_products_category
  ON products (category);

CREATE INDEX IF NOT EXISTS idx_products_name
  ON products (name);

-- Lookup of the "orders for this product" back-reference
CREATE INDEX IF NOT EXISTS idx_order_items_product_id
  ON order_items (product_id);

-- Law 2 backstop: a second account with the same address is impossible.
CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_email
  ON customers (email);