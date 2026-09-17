#!/usr/bin/env python3
"""Create (or refresh) the course database: data/analytics.duckdb.

Runs the single source of truth seed/seed.sql with the same DuckDB driver
(v1.5.5) the notebooks use, so local learners get the identical file that
the Docker container seeds on first start.

Usage:
    python scripts/seed_db.py [--path data/analytics.duckdb]
"""

import argparse
import sys
from pathlib import Path

import duckdb

ROOT = Path(__file__).resolve().parents[1]
SEED = ROOT / "seed" / "seed.sql"


def main() -> int:
    parser = argparse.ArgumentParser(description="Create the debt dataset.")
    parser.add_argument(
        "--path",
        default=str(ROOT / "data" / "analytics.duckdb"),
        help="output database file (default: course data/analytics.duckdb)",
    )
    args = parser.parse_args()

    parent = Path(args.path).parent
    parent.mkdir(parents=True, exist_ok=True)

    path = Path(args.path)
    if path.exists() and path.stat().st_size == 0:
        path.unlink()  # DuckDB refuses to open an existing empty file

    con = duckdb.connect(str(path))
    try:
        con.execute(SEED.read_text())
        summary = con.execute(
            "SELECT table_name FROM information_schema.tables "
            "WHERE table_schema = 'main' ORDER BY table_name"
        ).fetchall()
        print(f"[duckdb] seeded: {args.path}")
        print("tables:", ", ".join(name for (name,) in summary))
        print("version:", con.execute("SELECT version()").fetchone()[0])
    finally:
        con.close()
    return 0


if __name__ == "__main__":
    sys.exit(main())