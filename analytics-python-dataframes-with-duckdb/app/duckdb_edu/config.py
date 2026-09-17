"""Central configuration for the education app.

The default database path points at the course's single shared
`analytics.duckdb` file. Override with the DUCKDB_PATH environment variable
(e.g. in tests, or to point at the Docker volume data dir).
"""

import os
from pathlib import Path

PACKAGE_DIR = Path(__file__).resolve().parent

DUCKDB_PATH = os.environ.get(
    "DUCKDB_PATH", str(PACKAGE_DIR.parent / "data" / "analytics.duckdb")
)

SEED_SQL = PACKAGE_DIR / "seed" / "seed.sql"