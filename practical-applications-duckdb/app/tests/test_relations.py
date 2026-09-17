"""Tests for the relational API and dataframe interop lessons."""

from duckdb_app import relations


def test_relational_api_composition(con):
    payload = relations.relational_api(con)
    assert payload["source_is_relation"] is True
    # books has two products -> the top-3 peek returns the two of them
    assert len(payload["composite_relations_limit_head"]) == 2
    assert "GROUP" in payload["composite_plan"].upper()
    assert len(payload["lazy_rows"]) == 2


def test_relational_api_top_books(con):
    head = relations.relational_api(con)["composite_relations_limit_head"]
    assert {row["product_name"] for row in head} == {"Clean Code", "Germinal"}
    assert head[0]["revenue"] >= head[1]["revenue"]


def test_dataframe_interop_three_dialects(con):
    payload = relations.dataframe_interop(con)
    assert payload["pandas"]["type"] == "DataFrame"
    assert payload["polars"]["type"] == "DataFrame"
    assert payload["arrow"]["type"] == "Table"
    assert payload["pandas"]["rows"] == payload["polars"]["rows"] == 4
    for list_of_rows in payload["sql_on_registered_dataframes"]:
        assert len(list_of_rows) == 2
        assert "department" in list_of_rows[0]


def test_polars_into_sql(con):
    payload = relations.polars_into_sql(con)
    assert payload["result"] == [
        {"item": "mac", "score": 42},
        {"item": "kettle", "score": 19},
    ]