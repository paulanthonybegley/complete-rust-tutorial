"""Tests for the extension lessons (spatial is core+opt-in; h3 is a community
extension whose availability depends on platform/version)."""

from duckdb_app import extensions, plans


def test_spatial_demo(con):
    payload = extensions.spatial_demo(con)
    assert payload["extension"] == "spatial"
    distances = payload["points_and_distances_km"]
    assert len(distances) == 5  # one row per place
    hq = next(r for r in distances if r["place"] == "Head Office")
    assert hq["km_to_hq"] == 0.0
    assert all(row["km_to_hq"] >= 0 for row in distances)


def test_h3_demo_is_graceful(con):
    payload = extensions.h3_demo(con)
    assert payload["extension"] == "h3"
    assert isinstance(payload["ok"], bool)
    if payload["ok"]:
        assert len(payload["cells"]) == 5  # platform where h3 exists


def test_explain_plans(con):
    payload = plans.explain_metric(con, "sales-by-category")
    assert "GROUP" in payload["plan"].upper()
    assert "sales" in payload["plan"]


def test_describe_and_summarize(con):
    described = {c["column_name"] for c in plans.describe(con, "employees")}
    assert {"id", "name", "job_title", "department", "salary", "hire_date"} <= described

    summarized = {r["column_name"]: r for r in plans.summarize(con, "sales")}
    assert summarized["quantity"]["min"] == 1
    assert summarized["quantity"]["max"] == 4


def test_database_info(con):
    info = plans.database_info(con)
    assert info["duckdb_version"].startswith("v1.5.5")
    assert "threads" in info["settings"]
    assert {"employees", "products", "sales", "places"} <= set(
        info["tables"]["name"]
    )