# America's Debt Crisis

An educational **Jupyter notebook** unit built on a single deterministic
DuckDB dataset. It distils one lecture — a two-minute-plus deep dive into the
US debt, net-interest share, the "333" growth plan and the g-vs-r crossover
clock (the "breathing room" thesis) — into four run-and-interpret notebooks.

Everything here is **reproducible**: the seed data uses *fixed* anchors
(the lecture's own figures) plus deterministic noise — never `random` — so a
notebook run today reproduces the same numbers a year from now, on any
machine, independent of the host DuckDB version (driver pinned to **1.5.5**
to match the Docker CLI).

## The four notebooks

| # | notebook | level | what you leave with |
|---|---|---|---|
| 1 | `01-understand-the-debt` | understand | the five headline facts ($40T, $3B/day, 3.2% of GDP, 5.3% 30-year, the budget map) |
| 2 | `02-forecast-scenarios` | analyze | the g-vs-r baseline, market-stress sensitivity, the crossover clock (2031 base) |
| 3 | `03-growth-plan-challenge` | challenge | decompose Bessent's "333" plan and challenge each lever |
| 4 | `04-capstone-project` | capstone | pick a scenario, refit a chart, defend your verdict |

## Quick start (no Docker needed)

```bash
cd america-debt-crisis-world
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
.venv/bin/python scripts/seed_db.py        # creates data/analytics.duckdb
.venv/bin/jupyter lab                       # open notebooks/course/*.ipynb
```

Run all four headlessly to smoke-test the unit:

```bash
bash scripts/run_notebooks.sh
.venv/bin/pytest -q                         # seed + model invariants
```

## Optional: the DuckDB CLI side-car (Docker)

The same `seed/seed.sql` and the same `data/analytics.duckdb` file are served
to a DuckDB CLI container — learners can interrogate the dataset directly from
SQL:

```bash
docker compose up -d --build --wait
docker compose exec duckdb duckdb /data/analytics.duckdb \
  -c "SELECT * FROM debt ORDER BY year DESC LIMIT 5;"
```

On macOS the Docker engine cannot bind-mount `~/Documents`; `.env` sets
`DUCKDB_DATA` to a host path Docker can mount (the seed lives in the image, so
the CLI is self-seeding).

## Dataset (all measured in billions of dollars unless noted)

| table | rows | purpose |
|---|---|---|
| `gdp` | 32 | nominal GDP 1995-2026 |
| `debt` | 32 | total debt + held-by-public % GDP |
| `interest` | 32 | net interest bill + % GDP |
| `interest_outlook` | 11 | 2026-2036 projected net interest ($20.2T decade) |
| `deficit` | 50 | receipts/outlays, deficit % GDP, primary-surplus flags |
| `spending` | 36 | outlays by category (top-5 = ~78% in 2026) |
| `thirty_year_yields` | 32 | annualized 30-year auction yields (2026 ≈ 5.3%) |
| `monthly_30y_yield_2025` | 12 | the Aug-2025 5.18% spike |
| `energy` | 12 | crude + natural gas production (2028 ≈ 18.7 M boe/d) |
| `growth_quarters` | 14 | 2023Q1-2026Q2 real growth (H1'26 ≈ 1.8%) |

## Project layout

```
America.txt            course context for the unit
OLD.txt                the lesson-plan framework (a system, not a prompt)
seed/seed.sql          single source of truth (deterministic)
scripts/
  model.py             g-vs-r projection + sensitivity (deterministic)
  seed_db.py           build data/analytics.duckdb locally
  build_notebooks.py   regenerate the four notebooks
  run_notebooks.sh     headless notebook smoke runner
notebooks/course/      the four .ipynb deliverables
tests/                 pytest: seed anchors + model invariants (no Docker)
```

`make test` (from the repository root) runs this unit's pytest as part of the
whole course suite; `make start-debt-db` / `make stop-debt-db` control the CLI
side-car.