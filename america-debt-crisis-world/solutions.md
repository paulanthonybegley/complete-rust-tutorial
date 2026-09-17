# Solutions — America's Debt Crisis

Answers for `exercises.md`. All numbers are stable because the seed is
deterministic (fixed anchors + deterministic noise) and the driver is pinned
to DuckDB **1.5.5**.

## Notebook 1 — Understand the debt

**1.01** 2006 → 2016: **×2.0** (≈ $9.9T → $20.1T). 2016 → 2026: **×2.0**
(≈ $20.1T → $39.7T). Net effect over twenty years: **quadrupled**
(≈ $40T in 2026). Read straight from `debt.debt_total_billions`.

**1.02** 2026 net interest = **≈ $1,066B** (3.2% of $33.4T GDP) →
`1066 / 365 ≈ $2.92B/day`. Assumption: the annual interest bill is spread
evenly across the year.

**1.03** Only "All other" is outside the top-5; it eats **~22%** of outlays.
The named five (Social Security, Medicare, Medicare-costs-in-Health,
Net-interest, Defense) capture **78%** — the "spending cuts are theatrics"
lesson: the sliver available to cut is the bottom ~22%.

**1.04** The 30-year **locks in the government's borrowing cost for a
generation**; the market, not the CBO, decides that price. A 2-year note
refinances soon; a 5.3% 30-year sale cements expensive debt-service for
decades.

## Notebook 2 — Forecast scenarios

**2.01** Baseline crossover (g ≥ r) = **2031**.

**2.02** r +84bp → **2035**; r +158bp → **2038**. (Ballpark: higher
effective r slides breathing room ~a quad.)

**2.03** g rises to a ceiling `RATE_G` = 4.1%. If `r > RATE_G`, g can never
reach r, so `crossover()` returns `None` — the "**never**" regime. (In model
`project(40, r=RATE_G+1)` the whole series has `g_minus_r` < 0.)

**2.04** `interest_outlook` sums the *projected bill*: **$20,235B ≈ $20.2T**
in 2026-2036, above the lecture's ">$16T". The model's effective-r 2.5%
framework sits below the market's 5.3% 30-year — the **gap is the point**:
the official path and the market price disagree, and the market is the one
that pays.

**2.05** `GROWTH_STEP` 0.20→2033, 0.15→2035, 0.10→2039, 0.05→2052: even a
0.05 pp/yr healing eventually crosses inside 30 years (2052). Only
**step = 0** (growth frozen at 1.2%) never crosses. Reading: a *stagnation*
world (no healing at all) is what kills the crossover, not a slow one.

## Notebook 3 — Growth-plan challenge

**3.01** With `g2026 = 3.0` the first year already satisfies g ≥ r ⇒
crossover **2026** (immediately). 3% growth is not the *necessity* — it is the
**ceiling** the economy needs to hit; the baseline (1.2%) stays in the painful
regime until 2031.

**3.02** 2026 primary surplus = **-3.0%** of GDP
(`(5431 − (7500 − 1066)) / 33368`). Matches the plan's target exactly — the
seed anchors it there.

**3.03** **12** primary-surplus years in 50 — the "12 times in 50 years"
line. Holding a persistent **negative** primary deficit (-3.0%) is therefore
*heroic* by the recent record, which is Bessent's whole point: the plan
demands behavior the data has rarely seen.

**3.04** 2028 total = **18.7 M boe/d** (crude 13.85 + gas 4.85) — the target
**is met** in the dataset; but note it takes 2025→2028 to add only ~0.9 M
boe/d, in the plan's happy case.

## Notebook 4 — Capstone

**4.01** Grading rubric (self-check):
(a) scenario named (A/B/C); (b) lever = **r** (market) or **g** (growth
healing); (c) a number that would flip the verdict — e.g. "if the 30-year
stays above 5% I lose the ~2035 crossover"; (d) one counterargument (e.g.
"energy growth is a revenue story, not a borrowing-cost story").

**4.02** `bash scripts/run_notebooks.sh` must print `all notebooks executed
(4)` with no `[FAILED]`. That is the reproducibility gate.