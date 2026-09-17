"""The video's concurrency lesson: threads vs processes for database work.

DuckDB queries are I/O bound while the engine reads data — the video's advice
is that *threads* (or async) are the right tool, not processes. Each thread
opens its own connection to the shared .duckdb file and runs the same query;
the SQL engine handles the rest.
"""

import time
from concurrent.futures import ThreadPoolExecutor

import duckdb


def run_threaded_reads(
    db_path: str, sql: str, n_threads: int = 4
) -> dict:
    """Run the same query on `n_threads` threads against one file database.

    Returns per-thread results plus the wall time. Deterministic: every
    thread sees the same (seeded) data, so results are identical.
    """
    t0 = time.perf_counter()

    def one(_n: int):
        con = duckdb.connect(db_path)
        try:
            return con.sql(sql).fetchall()
        finally:
            con.close()

    with ThreadPoolExecutor(max_workers=n_threads) as pool:
        results = list(pool.map(one, range(n_threads)))

    elapsed = round(time.perf_counter() - t0, 4)
    return {
        "threads": n_threads,
        "elapsed_seconds": elapsed,
        "rows_per_thread": len(results[0]) if results else 0,
        "all_threads_agree": all(r == results[0] for r in results),
        "sql": sql,
    }