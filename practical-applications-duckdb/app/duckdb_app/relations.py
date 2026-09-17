"""The relational API + dataframe interop lessons.

The podcast's core Python advice (Ned): write queries with the relational
API and you get a *relation* — a lazy description of a query, not data. You
can compose relations, inspect the composite plan, peek, and only materialise
when you ask. Relations also hop between engines for free: .df() -> pandas,
.pl() -> polars, .arrow() -> Apache Arrow, and registered dataframes are
queryable as tables from the other direction.
"""

from __future__ import annotations

import duckdb
import polars as pl

from duckdb_app.wrangle import records


def relational_api(con: duckdb.DuckDBPyConnection) -> dict:
    """Compose a query from relations without materialising.

    Each step below reads as plain Python but the final relation is the whole
    query ("SQL doesn't compose; relations do") and the planner optimises the
    composed graph as one unit.
    """
    source = con.table("sales")                                   # relation over a real table
    joined = con.sql(
        "SELECT s.*, p.category, p.name AS product_name, "
        "       s.quantity * s.unit_price AS line_total "
        "FROM sales s JOIN products p ON p.id = s.product_id"
    )                                                             # relation over SQL
    lite = joined.filter('"category" = \'books\'')                # compose: filter
    grouped = lite.aggregate(
        "product_name, sum(line_total) AS revenue"
    ).order("revenue DESC")                                       # compose: aggregate + order
    peek_head = records(grouped.limit(3).df())                    # peek == materialising

    return {
        "source_is_relation": type(source).__name__ == "DuckDBPyRelation",
        "composite_relations_limit_head": peek_head,
        "composite_plan": grouped.explain(),
        "lazy_rows": [list(r) for r in grouped.fetchmany(2)],
    }


def dataframe_interop(con: duckdb.DuckDBPyConnection) -> dict:
    """One result, three dataframe dialects.

    The same relation materialises to pandas, polars and Arrow; each is
    registered back into the same engine and queried with SQL again.
    """
    rel = con.sql(
        "SELECT department, ROUND(AVG(salary), 2) AS avg_salary "
        "FROM employees GROUP BY department ORDER BY avg_salary DESC"
    )

    pandas_df = rel.df()
    polars_df = rel.pl()
    arrow_table = rel.arrow().read_all()

    con.register("pandas_t", pandas_df)
    con.register("polars_t", polars_df)
    py_tr = con.register("arrow_t", arrow_table) if hasattr(con, "register") else None

    names = [
        con.execute("SELECT department FROM pandas_t LIMIT 2").df(),
        con.execute("SELECT department FROM polars_t LIMIT 2").df(),
        con.execute("SELECT department FROM arrow_t LIMIT 2").df(),
    ]
    return {
        "pandas": {"type": type(pandas_df).__name__, "rows": int(len(pandas_df))},
        "polars": {
            "type": type(polars_df).__name__,
            "rows": int(polars_df.shape[0]),
        },
        "arrow": {"type": type(arrow_table).__name__, "rows": int(arrow_table.num_rows)},
        "sql_on_registered_dataframes": [records(df) for df in names],
        "tip": "Scope magic: a python variable named like a table resolves too.",
    }


def polars_into_sql(con: duckdb.DuckDBPyConnection) -> dict:
    """The reverse direction: a polars frame built imperatively, then queried
    with SQL by registering it in the connection."""
    frame = pl.DataFrame(
        {
            "item": ["mac", "mouse", "kettle"],
            "score": pl.Series("score", [42, 7, 19], dtype=pl.Int64),
        }
    )
    con.register("scorecard", frame)
    sql = "SELECT item, score FROM scorecard WHERE score > 10 ORDER BY score DESC"
    return {
        "polars_frame": records(frame.to_pandas()),
        "sql": sql,
        "result": records(con.sql(sql).df()),
    }