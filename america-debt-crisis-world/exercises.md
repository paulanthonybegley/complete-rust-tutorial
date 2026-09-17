# Exercises — America's Debt Crisis

These exercises mirror the `# TASK` cells and checkpoints in the notebooks.
Answers in `solutions.md`. Levels match the notebook ladder
(blank / rookie / experienced).

## Notebook 1 — Understand the debt

**1.01 (blank)** How many times did total debt grow 2006→2016 and 2016→2026?
Where does that show in `debt`?

**1.02 (blank)** Approximate 2026 daily net interest — does $3.0B land
in [2.5, 3.5]? What assumption turns a $B figure into a daily rate?

**1.03 (rookie)** In 2026, which category *besides* "All other" is missing
from the top-5? What share of outlays do the five named categories take?

**1.04 (rookie)** Explain (one sentence) why a **30-year** 5.3% auction
matters more than a 2-year note for the *debt* story.

## Notebook 2 — Forecast scenarios

**2.01 (blank)** Baseline crossover year (g ≥ r) — print it.

**2.02 (blank)** Sensitivity: r +84bp → ? ; r +158bp → ?

**2.03 (rookie)** In `model.py`, `g(t) = min(RATE_G, g2026 + GROWTH_STEP·t)`.
Why does the crossover never happen if `r > RATE_G`?

**2.04 (rookie)** The `interest_outlook` decade sums to ~$20.2T, the lecture
says ">$16T". Which framework (`model.py` vs the market) produces which
number, and why the gap is the point?

**2.05 (experienced)** The `# TASK` experiment: shrink `GROWTH_STEP` until
the crossover disappears inside `project(30)`. Which step does it? What does
that mean for a "secular stagnation" bet?

## Notebook 3 — Growth-plan challenge

**3.01 (blank)** Run the "333" (g2026 = 3.0%) through `project(30)`.
Crossover? Is 3% growth *necessary* for breathing room, or is it the ceiling?

**3.02 (blank)** 2026 primary surplus as % GDP (ex-interest budget). Does
-3.0% hold?

**3.03 (rookie)** Count `primary_surplus_flag` years in 1977-2026. How many
times in 50 years did the primary budget run positive? Does that make "3%
primary deficit" easy or heroic to sustain?

**3.04 (rookie)** 2028 energy total (M boe/d) vs the 18.7 target. Met or not?

## Notebook 4 — Capstone

**4.01 (experienced)** For your chosen scenario, name the single *lever*
that most changes the answer, one number that would flip your verdict, and one
counterargument. (This is the DEFENSE paragraph.)

**4.02 (experienced)** Run Kernel → Restart & Run All and confirm the full
notebook reproduces in one pass — the reproducibility bar is part of the
grade.