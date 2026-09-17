# Lesson plans — America's Debt Crisis (notebook unit)

_Companion to `OLD.txt` ("a lesson plan is a system, not a prompt"). Each plan
below names the system inputs (goal, learners, prior knowledge, evidence) and
then the sequence + activities. The notebooks themselves are the activities;
the plan explains the why and the grading.

## Unit learning goal

> Learners can take any fiscal "story" (a debt level, an interest share, a
> growth plan) from the lecture and re-derive it from a dataset with DuckDB —
> and defend whether the numbers make the case memorable or misleading.

## Learner profile

Adult self-paced learners, comfortable with Python/pandas basics (the
*analytics-python-dataframes-with-duckdb* course), new to fiscal macro
terminology. Time-bounded (2-3 hours total). One-linearizer, not a terminal
wizard — hence notebooks, not a CLI exercise.

## Prior knowledge

- Reading tables and running assertions in Jupyter (assumed).
- `pandas` filtering/grouping at a beginner level (assumed).
- *Not* assumed: what debt-to-GDP or "primary deficit" means — provided in
  markdown per notebook.

---

## Notebook 1 — Understand the debt (level: understand)

- **Goal**: name the five headline facts and where each lives in the data.
- **Evidence (EES)**: correct outputs for the 2006/2016/2026 multiples,
  the ~$3B/day anchor, and the written answers (esp. question 4, the 30-year).
- **Prior knowledge activated**: lines on a time axis, `groupby`-style sums.
- **Sequence**: intro claims → dataset tour → three headline charts →
  budget map → energy lever → checkpoint.
- **Activities**: run cells, read the numbers, write the 4 answers.
- **Accessibility**: charts carry plain-English titles; assertions teach
  (fail loudly) rather than grade.

## Notebook 2 — Forecast scenarios (level: analyze)

- **Goal**: explain *why* the crossover year exists and how market stress
  moves it.
- **Evidence (EES)**: reproduces baseline 2031; the sensitivity table
  (84bp → 2035, 158bp → 2038); the "never" regime at high r; the `GROWTH_STEP`
  experiment.
- **Prior knowledge activated**: model module, linear projection, reading
  `g - r`.
- **Sequence**: model in ten lines → baseline → static sensitivity →
  interest decade → checkpoint + reft.
- **Accessibility**: two-line formulas in math rendering; every plot has a
  "so what" title.

## Notebook 3 — Growth-plan challenge (level: challenge)

- **Goal**: decompose the "333" plan into levers and *judge* each.
- **Evidence (EES)**: 3% growth ⇒ immediate crossover; the primary-deficit
  leg lands at -3.0% of GDP; the energy target 18.7 M boe/d met in data;
  the written verdict on 3 questions.
- **Prior knowledge activated**: notebook-1 budget map + notebook-2 model.
- **Sequence**: lever check → 333 through the model → primary deficit →
  composite verdict.
- **Accessibility**: verdict written in the learner's own words; charts pair
  the "plan" line with the baseline.

## Notebook 4 — Capstone project (level: capstone)

- **Goal**: choose a scenario (A/B/C), refit a chart, and defend a verdict.
- **Evidence (EES)**: a working scenario cell, a sensitivity run, and the
  DEFENSE paragraph naming (a) scenario, (b) lever, (c) counterargument,
  (d) the number that would change the mind.
- **Prior knowledge activated**: all prior notebooks + `model.py` knobs.
- **Sequence**: boot → scenario → chart → sensitivity → DEFENSE.
- **Accessibility**: three canned scenarios scaffold the open-ended task;
  graders prompt for *leverage*, not verbosity.

---

## Assessment evidence (unit-wide)

| what | where | pass bar |
|---|---|---|
| Numbers reproduce | notebook outputs + asserts | 2031 crossover; 84/158bp; -3.0% primary; 18.7 boe |
| Explains mechanism | written checkpoint answers | names the *market* price channel |
| Critiques a plan | notebook-3 verdict | one lever explicitly judged |
| Independent thesis | notebook-4 DEFENSE | scenario + lever + counterargument named |

## Teacher decisions baked in

- Run everything headlessly with `bash scripts/run_notebooks.sh` before class
  (all four green = dataset sane).
- The `# TASK` cells are the live-editable parts; the rest is canned.
- Grading bar is deliberately low (reproduce > original) — the capstone does
  the discriminating.