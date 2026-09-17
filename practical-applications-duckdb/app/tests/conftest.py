"""Shared fixtures — every test gets a fresh, seeded DuckDB engine and its own
sample files. No Docker, no network, no shared state."""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))  # app/ on path

import pytest

from duckdb_app.database import Database
from scripts.generate_samples import fitbit_csv, followers_jsonl


@pytest.fixture
def con():
    db = Database(":memory:")
    yield db.connection
    db.close()


@pytest.fixture
def samples(tmp_path):
    """Sample wrangle files (fitbit.csv, followers.jsonl) in a tmp dir."""
    out = tmp_path / "files"
    out.mkdir()
    fitbit_csv(out / "fitbit.csv")
    followers_jsonl(out / "followers.jsonl")
    return out


@pytest.fixture
def client(tmp_path, samples):
    from fastapi.testclient import TestClient

    from duckdb_app.main import create_app

    app = create_app(str(tmp_path / "api.duckdb"), data_dir=samples)
    with TestClient(app) as c:
        yield c