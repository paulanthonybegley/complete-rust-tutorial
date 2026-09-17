"""Tests for the file-wrangling lessons (CSV sniffer, JSONL social graph,
parquet round trips, hive partitions)."""

from duckdb_app import wrangle


def test_csv_sniff_infers_schema_and_count(con, samples):
    payload = wrangle.csv_sniff(con, samples / "fitbit.csv")
    assert payload["row_count"] == 200
    names = {col["column_name"] for col in payload["inferred_schema"]}
    assert {"Date", "Time", "Heart Rate (bpm)", "Steps", "Calories"} <= names
    assert len(payload["head"]) == 3


def test_csv_frozen_freezes_guess(con, samples):
    payload = wrangle.csv_frozen(con, samples / "fitbit.csv")
    assert payload["sql"].startswith("SELECT * FROM read_csv(")
    assert "delim := ','" in payload["sql"]
    assert payload["schema"]


def test_json_social_graph_degrees(con, samples):
    payload = wrangle.json_social_graph(con, samples / "followers.jsonl")
    assert payload["vertices"] == 60
    degrees = payload["degrees"]
    assert degrees[0]["follows_count"] == 5
    assert sum(row["follows_count"] for row in degrees) > 60  # many edges
    assert all(
        degrees[i]["follows_count"] >= degrees[i + 1]["follows_count"]
        for i in range(len(degrees) - 1)
    )


def test_parquet_roundtrip(con, tmp_path):
    payload = wrangle.parquet_roundtrip(con, "sales", tmp_path / "sales.parquet")
    assert payload["rows"] == 6000
    assert len(payload["files"]) >= 1
    assert payload["file_bytes"] > 0


def test_hive_generate_and_query(con, tmp_path):
    gen = wrangle.generate_hive_sales(con, tmp_path / "sales_hive")
    assert {p.replace("category=", "") for p in gen["partitions"]} == {
        "books", "electronics", "grocery", "home",
    }

    payload = wrangle.hive_partition(con, tmp_path / "sales_hive")
    groups = {row["category"]: row["n"] for row in payload["rows_by_partition"]}
    assert sum(groups.values()) == 6000
    cols = {c["column_name"] for c in payload["partition_columns"]}
    assert "category" in cols


def test_hive_partition_pruning(con, tmp_path):
    wrangle.generate_hive_sales(con, tmp_path / "sales_hive")
    payload = wrangle.hive_partition(
        con, tmp_path / "sales_hive", category="books"
    )
    grouped = {row["category"]: row["n"] for row in payload["rows_by_partition"]}
    assert payload["filtered_count"] == grouped["books"]
    assert "PARQUET" in payload["plan"].upper()
    assert "category" in payload["plan"]