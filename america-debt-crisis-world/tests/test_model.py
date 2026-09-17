"""Deterministic g-vs-r model invariants (scripts/model.py)."""

import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
import model as m


def test_baseline_crossover_2031():
    rows = m.project(30)
    assert m.crossover(rows) == 2031


def test_baseline_starts_painful():
    first = m.project(1)[0]
    assert first["g"] < first["r"]


def test_breathing_starts_and_sticks():
    rows = m.project(60)
    years = [r["year"] for r in rows if r["breathing"]]
    assert years == list(range(2031, 2031 + len(years)))


def test_g_heals_toward_ceiling():
    rows = m.project(40)
    assert rows[-1]["g"] == pytest.approx(m.RATE_G, abs=0.01)
    assert all(r["g"] <= m.RATE_G + 1e-9 for r in rows)


def test_growth_333_immediate_crossover():
    rows = m.project(30, g2026=3.0)
    assert m.crossover(rows) == 2026
    assert rows[0]["breathing"]


def test_sensitivity_slides_crossover_later():
    sens = m.sensitivity()
    assert sens[0]["crossover"] > 2031
    assert sens[-1]["crossover"] == 2038
    assert [s["crossover"] for s in sens] == sorted(s["crossover"] for s in sens)


def test_override_r_above_ceiling_never_crosses():
    rows = m.project(40, r=m.RATE_G + 1.0)
    assert all(r["g_minus_r"] < 0 for r in rows)
    assert m.crossover(rows) is None


def test_rejects_bad_years():
    with pytest.raises(ValueError):
        m.project(0)
    with pytest.raises(ValueError):
        m.project(101)