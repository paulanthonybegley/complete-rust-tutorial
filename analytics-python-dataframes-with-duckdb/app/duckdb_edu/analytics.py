"""The analytics operations behind the course's lessons.

Every public function accepts a `duckdb.DuckDBPyConnection` and returns
plain-data frames or JSON-safe lists of dicts, so the same code is called by
the API (app/main.py) and by the pytest suite (tests/) — the tests exercise
the exact code learners will use.

The functions map 1:1 to the video's teaching points:

  * SQL "on a DataFrame"  — run SQL where table names are local pandas/duckdb
    variables (scope magic) and SQL where a DataFrame was registered in a
    connection (manual register()).
  * DESCRIBE / EXPLAIN / SUMMARIZE — the three programmatic introspection
    tools the video recommends over eyeballing data.
  * Result-sets out of DuckDB → back to pandas DataFrames / parquet.
"""

import ast
import datetime
import decimal
import math

import duckdb
import pandas as pd

#: Tables the course exposes (everything else is rejected).
PUBLIC_TABLES = ("employees", "products", "sales", "v_sales_detail")

#: Named explore queries, each run under EXPLAIN to show the plan in the UI.
EXPLAINABLE = {
    "avg-salary-by-job": (
        "SELECT job_title, ROUND(AVG(salary), 2) AS avg_salary "
        "FROM employees GROUP BY job_title ORDER BY avg_salary DESC"
    ),
    "sales-by-category": (
        "SELECT pr.category, ROUND(SUM(s.quantity * s.unit_price), 2) AS revenue "
        "FROM sales s JOIN products pr ON pr.id = s.product_id "
        "GROUP BY pr.category ORDER BY revenue DESC"
    ),
    "top-products": (
        "SELECT p.name, ROUND(SUM(s.quantity * s.unit_price), 2) AS revenue "
        "FROM sales s JOIN products p ON p.id = s.product_id "
        "GROUP BY 1 ORDER BY revenue DESC"
    ),
}

_BUCKETS = {
    "day": "day",
    "week": "week",
    "month": "month",
}


# ---------------------------------------------------------------------------
# JSON-safe serialisation
# ---------------------------------------------------------------------------

def _plain(value):
    """Turn a numpy/pandas/decimal/date scalar into a JSON-safe value."""
    if value is None:
        return None
    if isinstance(value, pd.Timestamp):
        return None if pd.isna(value) else value.isoformat()
    if isinstance(value, (datetime.date, datetime.datetime)):
        return value.isoformat()
    if isinstance(value, decimal.Decimal):
        return float(value)
    if type(value).__module__.startswith("numpy"):
        value = value.item()  # numpy scalar -> python scalar
    if isinstance(value, float) and math.isnan(value):
        return None
    return value


def records(df: pd.DataFrame) -> list[dict]:
    """DataFrame -> list of dicts with JSON-safe values (for FastAPI responses)."""
    return [
        {str(k): _plain(v) for k, v in row.items()}
        for row in df.to_dict(orient="records")
    ]


# ---------------------------------------------------------------------------
# Lesson 1 — SQL on a pandas DataFrame
# ---------------------------------------------------------------------------

def sql_on_dataframe(con: duckdb.DuckDBPyConnection) -> dict:
    """The video's 'SQL on your DataFrame' trick, with route 1 = scope magic.

    A local variable named ``employees`` (a dataframe) is referenced as a
    table by `duckdb.sql(...)` — DuckDB finds it in the caller's scope. The
    pandas equivalent of the same query will return identical rows.
    """
    employees = con.sql("SELECT * FROM employees").df()

    sql_result = duckdb.sql(
        "SELECT name, job_title FROM employees "
        "WHERE salary > 125000 ORDER BY salary DESC, name LIMIT 3"
    ).df()

    pandas_result = (
        employees[employees["salary"] > 125000]
        .sort_values(["salary", "name"], ascending=[False, True])
        .head(3)[["name", "job_title"]]
        .reset_index(drop=True)
    )

    return {
        "matched_rows": int(sql_result.equals(pandas_result)),
        "sql": records(sql_result),
        "pandas": records(pandas_result),
    }


def registered_dataframe(con: duckdb.DuckDBPyConnection) -> dict:
    """Route 2 — explicit `register()` (the video's preferred alternative).

    Scope magic fakes out modern editors (the dataframe variable looks unused
    and renaming it silently breaks the SQL). Registering in the connection
    makes the relationship explicit.
    """
    employees = con.sql("SELECT * FROM employees").df()
    con.register("registered_employees", employees)
    result = con.sql(
        "SELECT job_title, count(*) AS n FROM registered_employees "
        "GROUP BY job_title ORDER BY n DESC LIMIT 3"
    ).df()
    con.unregister("registered_employees")
    return records(result)


# ---------------------------------------------------------------------------
# Lesson 2 — DESCRIBE / SUMMARIZE / EXPLAIN
# ---------------------------------------------------------------------------

def describe_table(con: duckdb.DuckDBPyConnection, table: str) -> list[dict]:
    _check_table(table)
    return records(con.sql(f'DESCRIBE SELECT * FROM "{table}"').df())


def summarize_table(con: duckdb.DuckDBPyConnection, table: str) -> list[dict]:
    _check_table(table)
    return records(con.sql(f'SUMMARIZE SELECT * FROM "{table}"').df())


def explain_metric(con: duckdb.DuckDBPyConnection, metric: str) -> dict:
    """Run EXPLAIN on one of the EXPLAINABLE queries and return the plan text."""
    if metric not in EXPLAINABLE:
        names = ", ".join(sorted(EXPLAINABLE))
        raise ValueError(f"unknown metric '{metric}' (available: {names})")
    sql = EXPLAINABLE[metric]
    frame = con.sql(f"EXPLAIN {sql}").df()
    # EXPLAIN returns a single row whose last column holds the plan text.
    plan = str(frame.iloc[0, -1])
    return {"metric": metric, "sql": sql, "plan": plan}


# ---------------------------------------------------------------------------
# Lesson 3 — linking databases
# ---------------------------------------------------------------------------

def join_dataframes(con: duckdb.DuckDBPyConnection) -> dict:
    """Join two pandas DataFrames with SQL, as in the video.

    Both frames come out of the shared analytics.duckdb file as pandas, are
    registered with register(), and a single SQL statement joins them. The
    pandas .merge() equivalent returns identical rows.
    """
    products = con.sql("SELECT * FROM products").df()
    sales = con.sql("SELECT * FROM sales").df()

    con.register("products_df", products)
    con.register("sales_df", sales)
    sql_result = con.sql(
        "SELECT sales_df.sold_on, products_df.name AS product_name, "
        "       sales_df.quantity, sales_df.unit_price "
        "FROM sales_df JOIN products_df ON products_df.id = sales_df.product_id "
        "WHERE products_df.category = 'books' "
        "ORDER BY sales_df.sold_on, products_df.name"
    ).df()
    con.unregister("products_df")
    con.unregister("sales_df")

    pandas_result = pd.merge(
        sales, products, left_on="product_id", right_on="id", how="inner"
    )
    pandas_result = pandas_result[pandas_result["category"] == "books"]
    pandas_result = pandas_result[["sold_on", "name", "quantity", "unit_price"]]
    pandas_result = pandas_result.rename(columns={"name": "product_name"})
    pandas_result = pandas_result.sort_values(["sold_on", "product_name"]).reset_index(drop=True)

    return {
        "matched_rows": int(sql_result.equals(pandas_result)),
        "sql": records(sql_result),
        "pandas": records(pandas_result),
    }


# ---------------------------------------------------------------------------
# Lesson 4 — moving data out of DuckDB
# ---------------------------------------------------------------------------

def to_parquet(
    con: duckdb.DuckDBPyConnection,
    table: str,
    out_path: str,
) -> dict:
    """`COPY (SELECT * FROM table) TO path (FORMAT PARQUET)` — the video's
    `copy ... to ...` for shipping data to parquet. Returns the row count and
    proves a round trip by reading the file back.
    """
    _check_table(table)
    con.execute(
        f'COPY (SELECT * FROM "{table}") TO ? (FORMAT PARQUET)', [out_path]
    )
    count = con.sql("SELECT count(*) FROM read_parquet(?)", [out_path]).fetchone()[0]
    return {"table": table, "rows": int(count), "parquet_path": out_path}


# ---------------------------------------------------------------------------
# Course queries (mirrored in the exercises — stable because the seed is
# deterministic for the pinned DuckDB version).
# ---------------------------------------------------------------------------

def avg_salary_by_job(con: duckdb.DuckDBPyConnection) -> list[dict]:
    return records(
        con.sql(
            "SELECT job_title, ROUND(AVG(salary), 2) AS avg_salary "
            "FROM employees GROUP BY job_title ORDER BY avg_salary DESC"
        ).df()
    )


def sales_by_category(con: duckdb.DuckDBPyConnection) -> list[dict]:
    return records(
        con.sql(
            "SELECT pr.category, ROUND(SUM(s.quantity * s.unit_price), 2) AS revenue "
            "FROM sales s JOIN products pr ON pr.id = s.product_id "
            "GROUP BY pr.category ORDER BY revenue DESC"
        ).df()
    )


def top_products(con: duckdb.DuckDBPyConnection, limit: int = 5) -> list[dict]:
    return records(
        con.sql(
            "SELECT p.name, p.category, "
            "ROUND(SUM(s.quantity * s.unit_price), 2) AS revenue, "
            "SUM(s.quantity) AS units_sold "
            "FROM sales s JOIN products p ON p.id = s.product_id "
            "GROUP BY 1, 2 ORDER BY revenue DESC LIMIT ?",
            [limit],
        ).df()
    )


def revenue_trend(
    con: duckdb.DuckDBPyConnection, bucket: str = "week"
) -> list[dict]:
    if bucket not in _BUCKETS:
        raise ValueError(f"bucket must be one of: {', '.join(sorted(_BUCKETS))}")
    return records(
        con.sql(
            "SELECT CAST(date_trunc(?, s.sold_on) AS DATE) AS bucket, "
            "ROUND(SUM(s.quantity * s.unit_price), 2) AS revenue "
            "FROM sales s GROUP BY 1 ORDER BY 1",
            [bucket],
        ).df()
    )


def database_info(con: duckdb.DuckDBPyConnection) -> dict:
    info = con.sql(
        "SELECT name, setting_value FROM duckdb_settings() "
        "WHERE name IN ('threads', 'max_memory', 'default_order')"
    ).fetchall()
    tables = con.sql(
        "SELECT table_name, table_type FROM information_schema.tables "
        "WHERE table_schema = 'main' ORDER BY table_name"
    ).fetchnumpy()
    return {
        "duckdb_version": con.sql("SELECT version()").fetchone()[0],
        "settings": {k: v for k, v in info},
        "tables": {
            "name": tables["table_name"].tolist(),
            "type": tables["table_type"].tolist(),
        },
    }


# ---------------------------------------------------------------------------
# helpers
# ---------------------------------------------------------------------------

def _check_table(table: str) -> None:
    if table not in PUBLIC_TABLES:
        raise ValueError(f"unknown table '{table}' (available: {', '.join(PUBLIC_TABLES)})")