"""The shared DuckDB engine.

DuckDB is an *embedded* analytical database: there is no server to talk to.
A "database" is a single file (or an in-memory instance). This class owns the
connection for the whole app and seeds the course dataset from the single
source of truth, seed.sql, on every open (the statements are idempotent).
"""

from pathlib import Path

import duckdb

from duckdb_edu.config import SEED_SQL


class Database:
    def __init__(self, path: str | Path):
        self.path = str(path)
        parent = Path(self.path).parent if self.path != ":memory:" else None
        if parent is not None:
            parent.mkdir(parents=True, exist_ok=True)
        self.connection = duckdb.connect(self.path)
        self.seed()

    def seed(self) -> None:
        """Run the idempotent seed.sql (tables exist → inserts are skipped)."""
        self.connection.execute(SEED_SQL.read_text())

    def close(self) -> None:
        self.connection.close()