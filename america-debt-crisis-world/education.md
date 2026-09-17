# education.md — framing notes for the instructor

## Why this unit exists

The lecture's message is *memorable because it is numbers a citizen can
re-derive* ($40T, $3B/day, 3.2% of GDP, 5.3% 30-year, "breathing room in
2031"). This unit teaches the *method* beneath the story — the g-vs-r
crossover and the plan-vs-data tension — rather than the anchor numbers
themselves.

## Pedagogical spine (from OLD.txt: a lesson plan is a system)

- **Understand → analyze → challenge → capstone** across the four notebooks.
- **EES**: every checkpoint has a reproducible artifact (a printed number, a
  written answer). The tests in `tests/` encode the same invariants for the
  instructor.
- **Determinism as a teaching tool**: students can compare answers across
  machines/years. The seed never calls RNG; `scripts/model.py` has no
  randomness either.
- **The market as the antagonist**: notebook 2's sensitivity is the 
  "let the bond market speak" beat — a durable idea kids can reuse in any
  debt discussion.

## What NOT to teach first

- Do not start with the model. Notebook 1 grounds every abstraction (GDP,
  interest share, primary deficit) in a visible chart first.
- Do not over-index on the "333" plan politics — the module is about *decomposing
  any growth plan*; Bessent's is the worked example.

## Grading posture

Reproduce ≥ criticize ≥ original. Notebook 1-3 mostly *reproduce*; the
capstone is where the discrimination happens (scenario chosen, lever named,
counterargument given).

## Running the unit

```bash
.venv/bin/python scripts/seed_db.py && .venv/bin/jupyter lab   # teach
bash scripts/run_notebooks.sh && .venv/bin/pytest -q           # verify
```

## Extension ideas

- Import a *real* FRED series and diff it against the seed (teaches trust).
- Rebuild `interest_outlook` under the +158bp market case and compare
  decade totals (teaches the ">$16T" gap).
- Swap `GROWTH_STEP` for a quarter-by-quarter path (the capstone's version of
  sensitivity).