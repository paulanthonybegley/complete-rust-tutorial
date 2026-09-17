"""Central configuration for the practical-applications-duckdb course app.

DUCKDB_PATH — the single shared analytics.duckdb file (override in tests).
DATA_DIR    — where generated wrangle files (csv/jsonl/parquet) live.
"""

import os
from pathlib import Path

PACKAGE_DIR = Path(__file__).resolve().parent
APP_DIR = PACKAGE_DIR.parent

DUCKDB_PATH = os.environ.get(
    "DUCKDB_PATH", str(APP_DIR.parent / "data" / "analytics.duckdb")
)

DATA_DIR = Path(os.environ.get("DUCKDB_DATA_DIR", str(APP_DIR.parent / "data" / "files")))

SEED_SQL = PACKAGE_DIR / "seed" / "seed.sql"
STATIC_DIR = APP_DIR / "static"


def resolve_data_file(name: str) -> Path:
    """Resolve a user-supplied file/relative path safely inside DATA_DIR."""
    candidate = (DATA_DIR / name).resolve()
    if not str(candidate).startswith(str(DATA_DIR.resolve()) + "/"):
        raise ValueError(f"path escapes the data directory: {name}")
    return candidate