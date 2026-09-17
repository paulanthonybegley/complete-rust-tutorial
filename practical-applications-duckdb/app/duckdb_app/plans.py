"""Inspection tools — DESCRIBE / SUMMARIZE / EXPLAIN and engine facts.

The book's data-engineering chapters (Simon's side) focus on being able to
*see* what the engine will do before you run anything: schema inspection,
column statistics, and query plans. These endpoints expose all three.
"""

import duckdb

from duckdb_app.wrangle import records

PUBLIC_TABLES = ("employees", "products", "sales", "places")

EXPLAINABLE = {
    "sales-by-category": (
        "SELECT p.category, ROUND(SUM(s.quantity * s.unit_price), 2) AS revenue "
        "FROM sales s JOIN products p ON p.id = s.product_id "
        "GROUP BY p.category ORDER BY revenue DESC"
    ),
    "employees-variance": (
        "SELECT department, round(stddev(salary), 2) AS spread "
        "FROM employees GROUP BY department ORDER BY spread DESC"
    ),
    "sales-by-week": (
        "SELECT CAST(date_trunc('week', sold_on) AS DATE) AS week, "
        "       ROUND(SUM(quantity * unit_price), 2) AS revenue "
        "FROM sales GROUP BY 1 ORDER BY 1"
    ),
}


def describe(con: duckdb.DuckDBPyConnection, table: str) -> list[dict]:
    _check(table)
    return records(con.sql(f'DESCRIBE SELECT * FROM "{table}"').df())


def summarize(con: duckdb.DuckDBPyConnection, table: str) -> list[dict]:
    _check(table)
    return records(con.sql(f'SUMMARIZE SELECT * FROM "{table}"').df())


def explain_metric(con: duckdb.DuckDBPyConnection, metric: str) -> dict:
    if metric not in EXPLAINABLE:
        names = ", ".join(sorted(EXPLAINABLE))
        raise ValueError(f"unknown metric '{metric}' (available: {names})")
    sql = EXPLAINABLE[metric]
    plan = str(con.sql(f"EXPLAIN {sql}").df().iloc[0, -1])
    return {"metric": metric, "sql": sql, "plan": plan}


def database_info(con: duckdb.DuckDBPyConnection) -> dict:
    settings = con.execute(
        "SELECT name, setting_value FROM duckdb_settings() "
        "WHERE name IN ('threads', 'max_memory', 'default_order')"
    ).fetchall()
    tables = con.execute(
        "SELECT table_name, table_type FROM information_schema.tables "
        "WHERE table_schema = 'main' ORDER BY table_name"
    ).fetchnumpy()
    return {
        "duckdb_version": con.execute("SELECT version()").fetchone()[0],
        "settings": {k: v for k, v in settings},
        "tables": {
            "name": tables["table_name"].tolist(),
            "type": tables["table_type"].tolist(),
        },
    }


def _check(table: str) -> None:
    if table not in PUBLIC_TABLES:
        raise ValueError(f"unknown table '{table}' (available: {', '.join(PUBLIC_TABLES)})")