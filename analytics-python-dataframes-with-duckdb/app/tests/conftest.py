"""Shared fixtures. Ever pytest run builds a fresh, seeded DuckDB engine —
in-memory for analytics tests, backed by a tmp file for the threading test.
No Docker, no network, no shared state between tests.
"""

import pytest

from duckdb_edu.database import Database


@pytest.fixture
def con():
    db = Database(":memory:")
    yield db.connection
    db.close()


@pytest.fixture
def seeded_file(tmp_path):
    """File-backed database (the threading lesson needs a real file)."""
    path = tmp_path / "analytics.duckdb"
    db = Database(path)
    db.close()
    return str(path)