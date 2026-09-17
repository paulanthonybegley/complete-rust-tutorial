"""FastAPI education app — "Practical Applications of DuckDB".

Each endpoint maps to a practical application from the podcast episode
(Developer Voices × Simon Aubury × Ned Letcher, "Getting Started with DuckDB"):

  * /wrangle/*    — CSV sniffing, JSONL social-graph wrangling, parquet
                    round trips and hive-partitioned folders.
  * /relations/*  — the relational API, lazy relations, pandas/polars/arrow
                    interop.
  * /extensions/* — opt-in extensions: spatial (core), h3 (community).
  * /plans/*      — DESCRIBE / SUMMARIZE / EXPLAIN.
  * /wasm         — DuckDB-WASM running in the learner's own browser.

Tests (no Docker) hit these same endpoints via TestClient.
"""

import json
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse, HTMLResponse

from duckdb_app import extensions, plans, relations, wrangle
from duckdb_app.config import DATA_DIR, DUCKDB_PATH, STATIC_DIR, resolve_data_file
from duckdb_app.database import Database


def create_app(duckdb_path: str | None = None, data_dir=None) -> FastAPI:
    path = duckdb_path if duckdb_path is not None else DUCKDB_PATH
    data = data_dir if data_dir is not None else DATA_DIR

    @asynccontextmanager
    async def lifespan(_app: FastAPI):
        app.state.db = Database(path)
        yield
        app.state.db.close()

    app = FastAPI(
        title="Practical Applications of DuckDB",
        version="1.0.0",
        lifespan=lifespan,
        description=(
            "Education API mirroring 'Practical Applications of DuckDB' "
            "(Developer Voices, S. Aubury & N. Letcher). Wrangle messy "
            "CSV/JSON, query parquet & hive-partitioned folders, compose "
            "SQL with the relational API, load opt-in extensions — all on "
            "one shared analytics.duckdb file."
        ),
    )

    def con():
        return app.state.db.connection

    def guard(fn):
        def wrapper(*args, **kwargs):
            try:
                return fn(*args, **kwargs)
            except ValueError as exc:
                raise HTTPException(status_code=400, detail=str(exc))

        wrapper.__name__ = fn.__name__
        return wrapper

    @app.get("/", response_class=HTMLResponse)
    def index():
        return """<h1>Practical Applications of DuckDB</h1>
<p>Course API. Try <a href='/docs'>/docs</a> (OpenAPI), <a href='/info'>/info</a>
and the in-browser <a href='/wasm'>WASM demo</a>.</p>"""

    @app.get("/info")
    def info():
        return plans.database_info(con())

    # ---------------- inspection ----------------
    @app.get("/plans/describe/{table}")
    def describe(table: str):
        return guard(plans.describe)(con(), table)

    @app.get("/plans/summarize/{table}")
    def summarize(table: str):
        return guard(plans.summarize)(con(), table)

    @app.get("/plans/explain/{metric}")
    def explain(metric: str):
        return guard(plans.explain_metric)(con(), metric)

    # ---------------- wrangling ----------------
    @app.get("/wrangle/csv/{name}")
    def wrangle_csv(name: str):
        return wrangle.csv_sniff(con(), resolve_data_file(name))

    @app.get("/wrangle/csv/{name}/frozen")
    def wrangle_csv_frozen(name: str):
        return wrangle.csv_frozen(con(), resolve_data_file(name))

    @app.get("/wrangle/graph/{name}")
    def wrangle_graph(name: str):
        return wrangle.json_social_graph(con(), resolve_data_file(name))

    @app.post("/wrangle/parquet/{table}")
    def wrangle_parquet(table: str):
        return guard(wrangle.parquet_roundtrip)(con(), table, DATA_DIR / f"{table}.parquet")

    @app.post("/wrangle/hive")
    def wrangle_hive():
        return wrangle.generate_hive_sales(con(), DATA_DIR / "sales_hive")

    @app.get("/wrangle/hive")
    def read_hive():
        return wrangle.hive_partition(con(), DATA_DIR / "sales_hive")

    @app.get("/wrangle/hive/{category}")
    def read_hive_filtered(category: str):
        return wrangle.hive_partition(con(), DATA_DIR / "sales_hive", category)

    # ---------------- relational API / interop ----------------
    @app.get("/relations/api")
    def relations_api():
        return relations.relational_api(con())

    @app.get("/relations/interop")
    def relations_interop():
        return relations.dataframe_interop(con())

    @app.get("/relations/polars")
    def relations_polars():
        return relations.polars_into_sql(con())

    # ---------------- extensions ----------------
    @app.get("/extensions/spatial")
    def ext_spatial():
        return extensions.spatial_demo(con())

    @app.get("/extensions/h3")
    def ext_h3():
        return extensions.h3_demo(con())

    # ---------------- wasm in the browser ----------------
    @app.get("/wasm", response_class=HTMLResponse)
    def wasm_page():
        page = STATIC_DIR / "wasm.html"
        if page.exists():
            return page.read_text()
        raise HTTPException(status_code=404, detail="wasm.html not built yet")

    return app


app = create_app()