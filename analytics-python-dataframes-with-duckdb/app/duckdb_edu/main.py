"""FastAPI education app for "Analytics from Python DataFrames with DuckDB".

Each endpoint maps to one teaching point from the course. Everything returns
JSON-safe dicts/lists (see duckdb_edu.analytics.records), so the payloads are
exactly what tests assert against.
"""

from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException
from fastapi.responses import HTMLResponse

from duckdb_edu import analytics, worker
from duckdb_edu.config import DUCKDB_PATH
from duckdb_edu.database import Database


def create_app(duckdb_path: str | None = None) -> FastAPI:
    path = duckdb_path if duckdb_path is not None else DUCKDB_PATH

    @asynccontextmanager
    async def lifespan(_app: FastAPI):
        app.state.db = Database(path)
        yield
        app.state.db.close()

    app = FastAPI(
        title="Analytics from Python DataFrames with DuckDB",
        version="1.0.0",
        lifespan=lifespan,
        description=(
            "Education API. Run SQL where table names are Python variables, "
            "register() dataframes explicitly, and inspect plans with "
            "DESCRIBE / EXPLAIN / SUMMARIZE — all on one shared "
            "analytics.duckdb file."
        ),
    )

    def con():
        return app.state.db.connection

    def guard(fn):
        def wrapper(*args, **kwargs):
            try:
                return fn(*args, **kwargs)
            except ValueError as exc:  # unknown table/metric/bucket
                raise HTTPException(status_code=400, detail=str(exc))

        wrapper.__name__ = fn.__name__
        return wrapper

    @app.get("/", response_class=HTMLResponse)
    def index():
        return """<h1>Analytics from Python DataFrames with DuckDB</h1>
<p>Course API. Explore <code>/docs</code> for the OpenAPI view and
<code>/info</code> for database facts.</p>"""

    @app.get("/info")
    def info():
        return analytics.database_info(con())

    @app.get("/magic")
    def magic():
        return analytics.sql_on_dataframe(con())

    @app.get("/registered")
    def registered():
        return analytics.registered_dataframe(con())

    @app.get("/tables/{table}/describe")
    def describe(table: str):
        return guard(analytics.describe_table)(con(), table)

    @app.get("/tables/{table}/summarize")
    def summarize(table: str):
        return guard(analytics.summarize_table)(con(), table)

    @app.get("/explain/{metric}")
    def explain(metric: str):
        return guard(analytics.explain_metric)(con(), metric)

    @app.get("/joins")
    def joins():
        return analytics.join_dataframes(con())

    @app.get("/parquet/{table}")
    def parquet(table: str):
        return guard(analytics.to_parquet)(con(), table, "/tmp/_duckdb_edu.parquet")

    @app.get("/queries/avg-salary-by-job")
    def query_avg_salary():
        return analytics.avg_salary_by_job(con())

    @app.get("/queries/sales-by-category")
    def query_sales_by_category():
        return analytics.sales_by_category(con())

    @app.get("/queries/top-products")
    def query_top_products(limit: int = 5):
        return analytics.top_products(con(), limit)

    @app.get("/queries/revenue-trend")
    def query_revenue_trend(bucket: str = "week"):
        return guard(analytics.revenue_trend)(con(), bucket)

    @app.get("/threads")
    def threads(n_threads: int = 4):
        return worker.run_threaded_reads(app.state.db.path, "SELECT count(*) FROM sales", n_threads=n_threads)

    return app


app = create_app()