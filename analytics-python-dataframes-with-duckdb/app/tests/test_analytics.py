"""Unit tests for duckdb_edu.analytics — the exact code the API calls.

Seed data is deterministic for the pinned DuckDB version, so numeric
assertions below are stable. If a DuckDB upgrade changes the hashing
algorithm, only the *numbers* change — structure assertions stay valid.
"""

import duckdb
import pytest

from duckdb_edu import analytics


# ---------------------------------------------------------------------------
# Lesson 1 — SQL on a pandas DataFrame
# ---------------------------------------------------------------------------

def test_scope_magic_matches_pandas(con):
    payload = analytics.sql_on_dataframe(con)
    assert payload["matched_rows"] == 3
    assert payload["sql"] == payload["pandas"]


def test_scope_magic_top_three_orders(con):
    payload = analytics.sql_on_dataframe(con)
    sql = payload["sql"]
    assert sql[0]["job_title"]  # every nudged row has a job title
    assert len(sql) == 3


def test_registered_dataframe(con):
    rows = analytics.registered_dataframe(con)
    top = rows[0]
    assert top["n"] == max(row["n"] for row in rows)
    assert sum(row["n"] for row in rows) == 200  # whole table grouped


# ---------------------------------------------------------------------------
# Lesson 2 — DESCRIBE / SUMMARIZE / EXPLAIN
# ---------------------------------------------------------------------------

def test_describe_table(con):
    rows = analytics.describe_table(con, "employees")
    columns = {r["column_name"] for r in rows}
    assert columns == {"id", "name", "job_title", "department", "salary", "hire_date"}
    salary = next(r for r in rows if r["column_name"] == "salary")
    assert salary["column_type"] == "INTEGER"


def test_describe_unknown_table(con):
    with pytest.raises(ValueError):
        analytics.describe_table(con, "nope")


def test_summarize_table(con):
    rows = analytics.summarize_table(con, "sales")
    by_column = {r["column_name"]: r for r in rows}

    quantity = by_column["quantity"]
    assert quantity["min"] == 1
    assert quantity["max"] == 4

    revenue_share = by_column["unit_price"]
    assert revenue_share["null_percentage"] == 0.0
    assert revenue_share["count"] == 12000


def test_summarize_distribution_shape(con):
    rows = analytics.summarize_table(con, "employees")
    assert len(rows) == 6  # one row per column
    salary = next(r for r in rows if r["column_name"] == "salary")
    for key in ("min", "max", "avg", "std", "q25", "q50", "q75"):
        assert key in salary


def test_explain_plan(con):
    payload = analytics.explain_metric(con, "avg-salary-by-job")
    assert payload["metric"] == "avg-salary-by-job"
    assert "GROUP" in payload["plan"].upper()
    assert "employees" in payload["plan"]
    assert payload["plan"]  # non-empty plan text


def test_explain_unknown_metric(con):
    with pytest.raises(ValueError):
        analytics.explain_metric(con, "bogus")


# ---------------------------------------------------------------------------
# Lesson 3 — linking databases (joins)
# ---------------------------------------------------------------------------

def test_join_dataframes(con):
    payload = analytics.join_dataframes(con)
    assert payload["matched_rows"] == 1
    sql, pandas_ = payload["sql"], payload["pandas"]
    assert sql == pandas_
    assert all(row["product_name"] in ("Clean Code", "Germinal") for row in sql)


# ---------------------------------------------------------------------------
# Lesson 4 — getting data out
# ---------------------------------------------------------------------------

def test_to_parquet_roundtrip(con, tmp_path):
    out = tmp_path / "products.parquet"
    payload = analytics.to_parquet(con, "products", str(out))
    assert payload["rows"] == 10
    assert out.exists()


# ---------------------------------------------------------------------------
# Course queries (stable for the pinned DuckDB version)
# ---------------------------------------------------------------------------

def test_avg_salary_by_job(con):
    rows = analytics.avg_salary_by_job(con)
    assert len(rows) == 10  # one row per job title
    values = [r["avg_salary"] for r in rows]
    assert values == sorted(values, reverse=True)


def test_sales_by_category_four_categories_ordered(con):
    rows = analytics.sales_by_category(con)
    assert len(rows) == 4
    revenue = [r["revenue"] for r in rows]
    assert revenue == sorted(revenue, reverse=True)


def test_sales_by_category_categories(con):
    rows = analytics.sales_by_category(con)
    assert {r["category"] for r in rows} == {
        "books", "electronics", "grocery", "home"
    }


def test_top_products(con):
    rows = analytics.top_products(con, limit=3)
    assert len(rows) == 3
    revenue = [r["revenue"] for r in rows]
    assert revenue == sorted(revenue, reverse=True)
    assert {r["name"] for r in rows}.issubset(
        {r["name"] for r in analytics.top_products(con, limit=10)}
    )


def test_revenue_trend_weekly_buckets(con):
    rows = analytics.revenue_trend(con, "week")
    buckets = [r["bucket"] for r in rows]
    assert buckets == sorted(buckets)
    assert 25 <= len(buckets) <= 27  # ~26 weeks in 180 days
    assert rows[0]["revenue"] > 0


def test_revenue_trend_unknown_bucket(con):
    with pytest.raises(ValueError):
        analytics.revenue_trend(con, "fortnight")


def test_database_info(con):
    info = analytics.database_info(con)
    assert info["duckdb_version"].startswith("v1.5.5")
    assert info["settings"]["threads"] >= 1
    names = info["tables"]["name"]
    for expected in ("employees", "products", "sales", "v_sales_detail"):
        assert expected in names