"""File wrangling lessons — the "get data into a table, fast" stories.

Mirrors the practical applications the guests describe:

  * CSV sniffing: `read_csv` auto-detects delimiter, header, types, dates.
    This is how Simon ate his 70,000-file Fitbit export.
  * JSON wrangling: `read_json_auto` handles nested records; `unnest` turns
    a "social graph" export into rows you can aggregate. This is how Ned
    turned 70M JSONL lines into a queryable social graph.
  * Parquet round trips: `COPY ... TO (FORMAT PARQUET)` and `read_parquet`.
  * Hive-partitioned data: query a `category=.../part-*.parquet` folder as
    one table; partitions become columns and filters prune the scan.
"""

import datetime
import decimal
import math
from pathlib import Path

import duckdb
import pandas as pd


# ---------------------------------------------------------------------------
# JSON-safe serialisation (shared by every module)
# ---------------------------------------------------------------------------

def _plain(value):
    if value is None:
        return None
    if isinstance(value, pd.Timestamp):
        return None if pd.isna(value) else value.isoformat()
    if isinstance(value, (datetime.date, datetime.datetime)):
        return value.isoformat()
    if isinstance(value, decimal.Decimal):
        return float(value)
    if type(value).__module__.startswith("numpy"):
        value = value.item()
    if isinstance(value, float) and math.isnan(value):
        return None
    if isinstance(value, (bytes, bytearray)):
        return value.hex()
    return value


def records(df: pd.DataFrame) -> list[dict]:
    return [
        {str(k): _plain(v) for k, v in row.items()}
        for row in df.to_dict(orient="records")
    ]


# ---------------------------------------------------------------------------
# CSV — the sniffer and freezing the guess
# ---------------------------------------------------------------------------

def csv_sniff(con: duckdb.DuckDBPyConnection, csv_path: str | Path) -> dict:
    """Read a messy CSV with the sniffer; report what it inferred."""
    path = str(csv_path)
    st = con.execute(
        "DESCRIBE SELECT * FROM read_csv(?, auto_detect := true)", [path]
    ).df()
    head = records(con.execute(
        "SELECT * FROM read_csv(?, auto_detect := true) LIMIT 3", [path]
    ).df())
    return {
        "file": str(csv_path),
        "inferred_schema": records(st),
        "row_count": int(con.execute(
            "SELECT count(*) FROM read_csv(?, auto_detect := true)", [path]
        ).fetchone()[0]),
        "head": head,
    }


def csv_frozen(con: duckdb.DuckDBPyConnection, csv_path: str | Path, **overrides) -> dict:
    """Freeze the guess: read the same file with explicit options so the
    pipeline stays robust when the raw file drifts (the podcast's advice:
    "take the guess and freeze it")."""
    defaults = {"delim": ",", "header": True, "auto_detect": True}
    defaults.update(overrides)
    args = []
    for key, value in defaults.items():
        if isinstance(value, str):
            args.append(f"{key} := '{value.replace(chr(39), chr(39) + chr(39))}'")
        else:
            args.append(f"{key} := {value}")
    sql = f"SELECT * FROM read_csv('{str(csv_path)}', {', '.join(args)})"
    return {"sql": sql, "schema": records(con.execute("DESCRIBE " + sql).df())}


# ---------------------------------------------------------------------------
# JSON — nested records and the social-graph wrangle
# ---------------------------------------------------------------------------

def json_social_graph(con: duckdb.DuckDBPyConnection, jsonl_path: str | Path) -> dict:
    """Read a JSONL social-graph export and compute per-user degree counts.

    The file has nested `follows` arrays per user; `unnest` turns those into
    rows, then a plain GROUP BY computes "how many people each user follows".
    Exactly Ned's story: a Twitter API feed turned into graph-shaped rows.
    """
    path = str(jsonl_path)
    degrees = records(con.execute(
        """
        SELECT u.user_id, count(*) AS follows_count
        FROM read_json_auto(?) AS data
        UNNEST(data.follows) AS u(user_id)
        GROUP BY 1
        ORDER BY follows_count DESC, user_id
        """,
        [path],
    ).df())
    return {
        "file": str(jsonl_path),
        "vertices": int(con.execute(
            "SELECT count(*) FROM (SELECT DISTINCT user_id FROM read_json_auto(?) UNNEST(follows) AS u(user_id))",
            [path],
        ).fetchone()[0]),
        "degrees": degrees,
    }


# ---------------------------------------------------------------------------
# Parquet — round trip and hive partitions
# ---------------------------------------------------------------------------

def parquet_roundtrip(
    con: duckdb.DuckDBPyConnection, table: str, parquet_path: str | Path
) -> dict:
    con.execute(
        f'COPY (SELECT * FROM "{table}") TO ? (FORMAT PARQUET)', [str(parquet_path)]
    )
    return {
        "table": table,
        "files": _parquet_summary(str(parquet_path)),
        "rows": int(con.execute(
            "SELECT count(*) FROM read_parquet(?)", [str(parquet_path)]
        ).fetchone()[0]),
        "file_bytes": Path(parquet_path).stat().st_size,
    }


def hive_partition(
    con: duckdb.DuckDBPyConnection, parquet_dir: str | Path, category: str | None = None
) -> dict:
    """Query a hive-partitioned folder (category=.../part-*.parquet).

    Shows the partition columns DuckDB discovers and — when a filter is given
    — the EXPLAIN plan proving the scan prunes partitions instead of reading
    everything.
    """
    root = str(parquet_dir)
    hive = f"read_parquet('{root}/*/*.parquet', hive_partitioning := 1)"

    summary = records(con.execute(f"DESCRIBE SELECT * FROM {hive}").df())
    grouped = records(con.execute(
        f"SELECT category, count(*) AS n FROM {hive} GROUP BY 1 ORDER BY 1"
    ).df())

    if category is None:
        return {
            "root": root,
            "partition_columns": summary,
            "rows_by_partition": grouped,
            "filtered_count": None,
            "plan": None,
        }

    plan = str(
        con.execute(
            f"EXPLAIN SELECT * FROM {hive} WHERE category = ?", [category]
        ).df().iloc[0, -1]
    )
    filtered_count = int(con.execute(
        f"SELECT count(*) FROM {hive} WHERE category = ?", [category]
    ).fetchone()[0])

    return {
        "root": root,
        "partition_columns": summary,
        "rows_by_partition": grouped,
        "filtered_count": filtered_count,
        "plan": plan,
    }


def generate_hive_sales(
    con: duckdb.DuckDBPyConnection, out_dir: str | Path
) -> dict:
    """COPY the seeded sales (joined with products) into a hive-partitioned
    parquet folder keyed by category. Demonstrates both the write and that the
    resulting layout is queryable as one table."""
    dest = str(out_dir)
    con.execute(
        """
        COPY (
            SELECT s.sale_id, s.sold_on, p.category, p.name AS product_name,
                   s.quantity, s.unit_price
            FROM sales s JOIN products p ON p.id = s.product_id
        ) TO ? (FORMAT PARQUET, PARTITION_BY category)
        """,
        [dest],
    )
    parts = sorted(str(p).replace(dest, "").lstrip("/") for p in Path(dest).glob("*/"))
    return {"out_dir": dest, "partitions": parts}


# ---------------------------------------------------------------------------
# helpers
# ---------------------------------------------------------------------------

def _parquet_summary(path: str) -> list[dict]:
    import duckdb as _d
    con = _d.connect()
    try:
        return records(con.execute("DESCRIBE SELECT * FROM read_parquet(?)", [path]).df())
    finally:
        con.close()