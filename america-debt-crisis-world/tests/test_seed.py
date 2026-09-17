"""The seed dataset must reproduce the video anchors (deterministic data).

These tests open a throwaway DuckDB file, apply seed/seed.sql, and check the
"fixed facts" the notebooks rely on. They do NOT require Docker — the same
seed.sql is what the container applies at first start.
"""

import os
import tempfile
from pathlib import Path

import duckdb
import pandas as pd
import pytest

ROOT = Path(__file__).resolve().parents[1]
SEED = ROOT / "seed" / "seed.sql"


def make_con() -> duckdb.DuckDBPyConnection:
    fd, path = tempfile.mkstemp(suffix=".duckdb")
    os.close(fd)
    os.unlink(path)  # duckdb refuses to open an existing empty file
    con = duckdb.connect(path)
    con.execute(SEED.read_text())
    return con


def table(con, name) -> pd.DataFrame:
    return con.execute(f"SELECT * FROM {name}").fetchdf()


def row(con, name, col, value) -> pd.Series:
    df = table(con, name)
    return df.set_index(col).loc[value]


def test_table_shapes():
    con = make_con()
    assert len(table(con, "gdp")) == 32
    assert len(table(con, "debt")) == 32
    assert len(table(con, "interest")) == 32
    assert len(table(con, "interest_outlook")) == 11
    assert len(table(con, "deficit")) == 50
    assert len(table(con, "spending")) == 36
    assert len(table(con, "thirty_year_yields")) == 32
    assert len(table(con, "monthly_30y_yield_2025")) == 12
    assert len(table(con, "energy")) == 12
    assert len(table(con, "growth_quarters")) == 14
    con.close()


def test_debt_doubling_and_quadrupling():
    con = make_con()
    d2006 = row(con, "debt", "year", 2006)
    d2016 = row(con, "debt", "year", 2016)
    d2026 = row(con, "debt", "year", 2026)
    assert d2006.debt_total_billions / 10_000 == pytest.approx(1.0, rel=0.05)
    assert d2016.debt_total_billions / d2006.debt_total_billions == pytest.approx(2.0, rel=0.05)
    assert d2026.debt_total_billions / d2016.debt_total_billions == pytest.approx(2.0, rel=0.05)
    assert d2026.debt_held_public_pct_gdp > 100.0
    con.close()


def test_interest_anchors():
    con = make_con()
    i = row(con, "interest", "year", 2026)
    assert i.interest_pct_gdp == pytest.approx(3.2, abs=0.05)
    daily = i.net_interest_billions / 365
    assert 2.5 <= daily <= 3.5
    con.close()


def test_interest_outlook_decade_sum_above_16T():
    con = make_con()
    assert table(con, "interest_outlook").net_interest_billions.sum() > 16_000
    con.close()


def test_thirty_year_2026_5_3():
    con = make_con()
    assert row(con, "thirty_year_yields", "year", 2026).avg_30y_pct == pytest.approx(5.3, abs=0.01)
    con.close()


def test_2025_august_peak_month():
    con = make_con()
    m25 = table(con, "monthly_30y_yield_2025").set_index("month")
    assert m25.loc["Aug"].avg_30y_pct >= 5.0
    con.close()


def test_budget_consistency():
    con = make_con()
    d = row(con, "deficit", "fiscal_year", 2026)
    g = row(con, "gdp", "year", 2026)
    global_deficit_pct = (d.outlays_billions - d.receipts_billions) / g.nominal_gdp_billions * 100
    assert global_deficit_pct == pytest.approx(6.2, abs=0.3)
    interest_2026 = row(con, "interest", "year", 2026).net_interest_billions
    primary_pct = (d.receipts_billions - (d.outlays_billions - interest_2026)) / g.nominal_gdp_billions * 100
    assert primary_pct == pytest.approx(-3.0, abs=0.3)
    assert d.deficit_pct_gdp == pytest.approx(global_deficit_pct, abs=0.3)
    con.close()


def test_twelve_primary_surplus_years():
    con = make_con()
    assert int(table(con, "deficit").primary_surplus_flag.sum()) == 12
    con.close()


def test_spending_top_five_is_78pct():
    con = make_con()
    cp = table(con, "spending")
    cp = cp[cp.fiscal_year == 2026]
    named = cp[cp.category != "All other"]
    share = named.amount_billions.sum() / cp.amount_billions.sum() * 100
    assert share == pytest.approx(78.0, abs=2)
    con.close()


def test_energy_2028_target_near_18_7():
    con = make_con()
    e28 = row(con, "energy", "year", 2028)
    assert e28.total_boe_mbpd == pytest.approx(18.7, abs=0.5)
    con.close()


def test_growth_h1_2026_around_1_8():
    con = make_con()
    gq = table(con, "growth_quarters")
    two = gq[gq.quarter.str.startswith("2026")].real_gdp_growth_pct
    assert two.mean() == pytest.approx(1.8, abs=0.2)
    con.close()