"""API tests: boot the FastAPI app via TestClient (lifespan seeds the engine).
No Docker."""


def test_index_and_info(client):
    assert client.get("/").status_code == 200
    body = client.get("/info").json()
    assert body["duckdb_version"].startswith("v1.5.5")
    assert set(body["tables"]["name"]) == {"employees", "products", "sales", "places"}


def test_plans_endpoints(client):
    described = client.get("/plans/describe/employees").json()
    assert len(described) == 6

    root = client.get("/plans/explain/sales-by-category").json()
    assert "GROUP" in root["plan"].upper()

    assert client.get("/plans/describe/nope").status_code == 400
    assert client.get("/plans/explain/bogus").status_code == 400


def test_csv_wrangle_endpoint(client):
    payload = client.get("/wrangle/csv/fitbit.csv").json()
    assert payload["row_count"] == 200


def test_csv_path_escape_is_rejected(client):
    assert client.get("/wrangle/csv/../../etc/passwd").status_code in (400, 404)


def test_graph_wrangle_endpoint(client):
    payload = client.get("/wrangle/graph/followers.jsonl").json()
    assert payload["vertices"] == 60
    assert payload["degrees"][0]["follows_count"] == 5


def test_parquet_and_hive_endpoints(client):
    parquet = client.post("/wrangle/parquet/products").json()
    assert parquet["rows"] == 8

    assert client.post("/wrangle/hive").status_code == 200

    partitions = client.get("/wrangle/hive").json()
    groups = {row["category"]: row["n"] for row in partitions["rows_by_partition"]}
    assert sum(groups.values()) == 6000

    books = client.get("/wrangle/hive/books").json()
    assert books["filtered_count"] == groups["books"]
    assert "PARQUET" in books["plan"].upper()


def test_relations_endpoints(client):
    api = client.get("/relations/api").json()
    assert api["source_is_relation"] is True
    assert len(api["composite_relations_limit_head"]) == 2

    interop = client.get("/relations/interop").json()
    assert interop["polars"]["type"] == "DataFrame"

    polars_ = client.get("/relations/polars").json()
    assert polars_["result"] == [
        {"item": "mac", "score": 42},
        {"item": "kettle", "score": 19},
    ]


def test_extension_endpoints(client):
    spatial = client.get("/extensions/spatial").json()
    assert len(spatial["points_and_distances_km"]) == 5

    h3 = client.get("/extensions/h3").json()
    assert "ok" in h3


def test_wasm_page(client):
    page = client.get("/wasm")
    assert page.status_code == 200
    assert "duckdb-wasm" in page.text