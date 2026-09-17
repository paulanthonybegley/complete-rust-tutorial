"""Deterministic growth-vs-interest (g vs r) model for the US debt outlook.

This is a Calm Foundation School ("CFS")-style framework like the one used in
the course video: the *difference* between nominal economic growth (g) and the
effective net-interest rate (r) drives whether debt keeps swallowing GDP
("painful regime", r > g) or starts shrinking as a share of GDP ("breathing
room", g > r).

The memorable anchor from the video: the CBO projected interest would keep
pipping growth until years into the future, and independent analysts pointed at
a ~2031 "breathing room" crossover. In the baseline below that crossover lands
exactly in 2031 — growth in 2026 is 1.2%, effective interest is 2.5%, and growth
heals +0.26 pp/year toward a 4.1% ceiling. If the market reprices the 30-year
(the video's "Let the Bond Market Speak" zone), r spikes and the crossover
slides into the 2030s or disappears entirely.

Everything is deterministic: no RNG anywhere, so notebooks reproduce exactly.

Typical use:
    import model as m
    rows = m.project(30)
    m.crossover(rows)      # 2031 on the baseline
    m.sensitivity()        # crossovers for 30y-yield stress levels
"""

from __future__ import annotations

from typing import Optional

# ---------------------------------------------------------------------------
# Fixed facts (course video + the 2026 debt dataset)
# ---------------------------------------------------------------------------
BASE_GDP_BILLIONS = 33_368.0          # 2026 nominal GDP (seed table `gdp`)
DEBT_BILLIONS = 39_700.0              # 2026 total public debt (~$40T)
INTEREST_PCT_GDP = 3.2                # 2026 net interest, % of GDP
AVG_30Y_YIELD = 5.3                   # 2026 30-year market auction yield (%)

# g vs r regime constants (Calm Foundation School conventions)
BASELINE_R = 2.5                      # effective net-interest cost, %/yr
GROWTH_STEP = 0.26                    # annual g healing, pp/yr
GROWTH_2026 = 1.2                     # nominal growth assumed in 2026
RATE_G = 4.1                          # long-run nominal growth ceiling

# Market-stress sensitivity ("Let the Bond Market Speak")
DEFAULT_STATIC_30Y_BP = (79, 116, 315)   # +bp on the 30-year, stress cases
STATIC_R_BP = (84, 130, 158)             # effective r (+bp) implied by above
GROWTH_DEFAULT_R_BP = (130, 150, 165)    # r bp used by sensitivity()


def project(
    years: int = 30,
    g2026: float = GROWTH_2026,
    r: Optional[float] = None,
) -> list[dict]:
    """Deterministic g-vs-r projection.

    Args:
        years:  how many years to project from 2026 (default 30).
        g2026:  assumed nominal GDP growth in 2026 (%/yr).
        r:      effective interest rate if overriding the baseline 2.5.

    Returns: list of rows {year, g, r, g_minus_r, interest_pct_gdp, g_gt_r}.
    """
    if years < 1 or years > 100:
        raise ValueError("years must be in [1, 100]")
    eff_r = BASELINE_R if r is None else r
    rows: list[dict] = []
    for t in range(years):
        year = 2026 + t
        g = min(RATE_G, g2026 + GROWTH_STEP * t)
        pct = INTEREST_PCT_GDP + (g - eff_r) * 0.45     # share drifts w/ regime
        breathing = g >= eff_r
        rows.append(
            {
                "year": year,
                "g": round(g, 3),
                "r": round(eff_r, 3),
                "g_minus_r": round(g - eff_r, 3),
                "interest_pct_gdp": round(max(pct, 0.0), 2),
                "breathing": breathing,
            }
        )
    return rows


def crossover(rows: list[dict]) -> Optional[int]:
    """First year where g >= r ("breathing room"), or None if never."""
    for row in rows:
        if row["breathing"]:
            return row["year"]
    return None


def sensitivity(
    years: int = 30,
    caps: tuple[float, ...] = STATIC_R_BP,
) -> list[dict]:
    """Crossover year for each market-stress cap on the effective interest r.

    Sorted by r: {"r_bp": ..., "crossover": ...}.
    """
    out = []
    for bp in caps:
        rows = project(years, r=round(BASELINE_R + bp / 100, 3))
        out.append({"r_bp": bp, "crossover": crossover(rows)})
    return out