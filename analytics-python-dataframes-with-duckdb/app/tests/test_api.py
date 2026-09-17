"""API tests: boot the FastAPI app against a fresh file-backed database via
TestClient (lifespan starts the engine and seeds from seed.sql). No Docker.
"""

import pytest
from fastapi.testclient import TestClient

from duckdb_edu.analytics import EXPLAINABLE
from duckdb_edu.main import create_app


@pytest.fixture
def client(tmp_path):
    app = create_app(str(tmp_path / "api.duckdb"))
    with TestClient(app) as c:
        yield c


def test_index(client):
    r = client.get("/")
    assert r.status_code == 200
    assert "DuckDB" in r.text


def test_info(client):
    r = client.get("/info")
    assert r.status_code == 200
    body = r.json()
    assert body["duckdb_version"].startswith("v1.5.5")
    assert set(body["tables"]["name"]) == {
        "employees", "products", "sales", "v_sales_detail"
    }


def test_magic(client):
    body = client.get("/magic").json()
    assert body["matched_rows"] == 3
    assert body["sql"] == body["pandas"]


def test_registered(client):
    rows = client.get("/registered").json()
    assert sum(r["n"] for r in rows) == 200


def test_describe(client):
    body = client.get("/tables/employees/describe").json()
    assert len(body) == 6
    assert any(r["column_name"] == "salary" for r in body)


def test_describe_unknown_table_is_400(client):
    r = client.get("/tables/nope/describe")
    assert r.status_code == 400
    assert "nope" in r.json()["detail"]


def test_summarize(client):
    body = client.get("/tables/sales/summarize").json()
    by = {r["column_name"]: r for r in body}
    assert by["quantity"]["max"] == 4


def test_explain_each_metric(client):
    for metric in EXPLAINABLE:
        body = client.get(f"/explain/{metric}").json()
        assert body["metric"] == metric
        assert "GROUP" in body["plan"].upper()


def test_explain_unknown_metric_is_400(client):
    r = client.get("/explain/bogus")
    assert r.status_code == 400


def test_joins(client):
    body = client.get("/joins").json()
    assert body["matched_rows"] == 1
    assert body["sql"] == body["pandas"]


def test_parquet(client):
    body = client.get("/parquet/products").json()
    assert body["rows"] == 10
    assert body["table"] == "products"


def test_queries_avg_salary(client):
    rows = client.get("/queries/avg-salary-by-job").json()
    assert len(rows) == 10
    assert [r["avg_salary"] for r in rows] == sorted(
        (r["avg_salary"] for r in rows), reverse=True
    )


def test_queries_sales_by_category(client):
    rows = client.get("/queries/sales-by-category").json()
    assert len(rows) == 4


def test_queries_top_products_limits(client):
    body = client.get("/queries/top-products", params={"limit": 3}).json()
    assert len(body) == 3


def test_queries_revenue_trend(client):
    body = client.get("/queries/revenue-trend", params={"bucket": "day"}).json()
    assert len(body) == 180
    buckets = [r["bucket"] for r in body]
    assert buckets == sorted(buckets)


def test_queries_revenue_trend_unknown_bucket_is_400(client):
    r = client.get("/queries/revenue-trend", params={"bucket": "fortnight"})
    assert r.status_code == 400


def test_threads(client):
    body = client.get("/threads", params={"n_threads": 3}).json()
    assert body["rows_per_thread"] == 12000
    assert body["all_threads_agree"] is True
    assert body["threads"] == 3
    assert body["elapsed_seconds"] >= 0