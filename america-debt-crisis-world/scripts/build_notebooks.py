#!/usr/bin/env python3
"""Build the four course notebooks (deterministic, one source of truth)."""

import json
from pathlib import Path

import nbformat as nbf

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "notebooks" / "course"
OUT.mkdir(parents=True, exist_ok=True)

DB = "../../data/analytics.duckdb"


def write(name, cells):
    nb = nbf.v4.new_notebook(cells=cells)
    nb["metadata"] = {
        "kernelspec": {"display_name": "Python 3 (ipykernel)", "language": "python", "name": "python3"},
        "language_info": {"name": "python"},
    }
    (OUT / name).write_text(json.dumps(nb, indent=1, ensure_ascii=False) + "\n")
    print("wrote", name, f"({len(cells)} cells)")


def md(text):
    return nbf.v4.new_markdown_cell(text.strip())


def code(src):
    return nbf.v4.new_code_cell(src.strip())


PRE = f"""Unit: America's Debt Crisis. Concept level: **understand → analyze → challenge → capstone**.

Data: the course dataset lives in one deterministic DuckDB file, `{DB}`, seeded
from `seed/seed.sql` (identical inside the Docker CLI). Every number here is
stable: rerun any notebook years from now and it reproduces the same answers,
because the seed uses fixed anchors (the course video's figures) plus
deterministic noise -- never the random number generator.

Workflow: run cells top to bottom. A `# TASK` comment marks a cell you should
edit; answer in the markdown cell just below when asked.
""".strip()

IMPORTS = """\
%matplotlib inline
import sys
from pathlib import Path
import duckdb

sys.path.insert(0, "../../scripts")   # the g-vs-r model used in 02-03
import model as m

DB = "../../data/analytics.duckdb"
con = duckdb.connect(DB)

import matplotlib
import matplotlib.pyplot as plt
import pandas as pd

plt.rcParams.update({"figure.figsize": (8, 4.4),
                     "axes.grid": True, "grid.alpha": 0.35,
                     "font.size": 10})
"""

# ===========================================================================
# 1 - UNDERSTAND THE DEBT
# ===========================================================================
cells_01 = [
    md(f"# 1. Understand the debt\n\n{PRE}"),

    md("""## Heads-up reading
The numbers below are the ones the course video leads with:
* **~$40T** total national debt at the start of fiscal 2026 -- *doubled* in ten years, *quadrupled* in twenty.
* **~$3B a day** of net interest on the federal debt.
* Net interest **3.2% of GDP** and climbing.
* A **30-year Treasury auction at 5.3%** in 2026 -- the most expensive run since 2001.
* Per the CBO-style projection, interest sits above nominal growth until the **early 2030s** -- the "breathing room" thesis tested in notebook 2.

Five questions this notebook answers: how big is the debt, how fast is it growing, what does the interest bill do to the budget, who sets the price, and what does "growth fixes it" even mean?"""),

    md("### 1.1 The dataset"),
    md("""| table | rows | what it holds |
|---|---|---|
| `gdp` | 32 | 1995-2026 nominal GDP (billions) |
| `debt` | 32 | total debt + held-by-public as % GDP |
| `interest` | 32 | net interest bill and % of GDP |
| `interest_outlook` | 11 | 2026-2036 projected net interest |
| `deficit` | 50 | 1977-2026 receipts/outlays, deficits, primary-surplus flags |
| `spending` | 36 | 2000-2026 federal outlays by category |
| `thirty_year_yields` | 32 | annualized 30-year auction yields |
| `monthly_30y_yield_2025` | 12 | 2025 monthly yields (the run-up to 5.3%) |
| `energy` | 12 | US production 2017-2028 (crude + natural gas, M boe/d) |
| `growth_quarters` | 14 | 2023Q1-2026Q2 real GDP growth |"""),
    code("""# Bootstrap: imports + connection
""" + IMPORTS),
    code("""tables = con.execute(
    "SELECT table_name FROM information_schema.tables "
    "WHERE table_schema='main' ORDER BY table_name"
).fetchall()
print("tables:", ", ".join(t for (t,) in tables))"""),
    code("""debt = con.execute("SELECT year, debt_total_billions FROM debt ORDER BY year").fetchdf()
gdp = con.execute("SELECT year, nominal_gdp_billions FROM gdp ORDER BY year").fetchdf()

fig, ax = plt.subplots()
ax.plot(debt.year, debt.debt_total_billions, label="total debt ($B)")
ax.plot(gdp.year, gdp.nominal_gdp_billions, label="nominal GDP ($B)")
ax.set(xlabel="fiscal year", ylabel="trillions of dollars", title="National debt and GDP")
ax.set_yticklabels([f"${v/1000:.0f}T" for v in ax.get_yticks()])
ax.legend()
plt.show()

for y in (2006, 2016, 2026):
    d = debt.set_index("year").loc[y, "debt_total_billions"]
    prev10 = debt.set_index("year").loc[y - 10, "debt_total_billions"]
    print(f"{y}: ${d:.0f}B  (x{d / prev10:.2f} versus {y-10})")"""),
    md("### 1.2 The interest bill: ~$3 billion a *day*"),
    code("""intr = con.execute(
    "SELECT year, net_interest_billions, interest_pct_gdp FROM interest ORDER BY year"
).fetchdf()
i26 = intr.set_index("year").loc[2026]
daily = i26.net_interest_billions / 365
print(f"2026: interest {i26.interest_pct_gdp}% of GDP -> ~${daily:.2f}B/day, ${daily*30:.0f}B/month")
assert 2.5 <= daily <= 3.5, "the ~$3B/day anchor"

fig, ax = plt.subplots()
ax.plot(intr.year, intr.interest_pct_gdp, ls="--", marker="o")
ax.axhline(i26.interest_pct_gdp, color="C3", alpha=0.4)
ax.set(xlabel="year", ylabel="% of GDP", title="Net interest share of GDP")
plt.show()"""),
    md("### 1.3 The 30-year: who prices the debt?"),
    code("""yld = con.execute("SELECT year, avg_30y_pct FROM thirty_year_yields ORDER BY year").fetchdf()
fig, ax = plt.subplots()
ax.plot(yld.year, yld.avg_30y_pct, marker="o", ms=3)
ax.axhline(5.3, color="C3", ls="--", alpha=0.4)
ax.annotate("5.3% sale (2026)", xy=(2026, 5.3), xytext=(2012, 6.2),
            arrowprops=dict(arrowstyle="->"))
ax.set(xlabel="year", ylabel="30-year Treasury yield (%)",
       title="The market price of the fiscal path")
plt.show()

m25 = con.execute("SELECT month, avg_30y_pct FROM monthly_30y_yield_2025 ORDER BY month").fetchdf()
print("2025 peak month:", m25.sort_values("avg_30y_pct").tail(1).values[0])"""),
    md("### 1.4 Where the budget goes"),
    code("""cp = con.execute(
    "SELECT category, SUM(amount_billions) AS total_billions FROM spending "
    "WHERE fiscal_year = 2026 GROUP BY category ORDER BY total_billions DESC"
).fetchdf()
others = cp.category == "All other"
cp = pd.concat([cp[~others], cp[others]])
cp["pct"] = cp.total_billions / cp.total_billions.sum() * 100
fig, ax = plt.subplots()
bars = ax.barh(cp.category, cp.pct)
ax.invert_yaxis()
for b, p in zip(bars, cp.pct):
    ax.text(b.get_width() + 0.4, b.get_y() + b.get_height()/2, f"{p:.0f}%", va="center")
ax.set(xlabel="% of 2026 outlays", title="Where the 2026 budget goes")
plt.show()
big5 = cp[~others].pct.sum()
print(f"Top-5 named categories = {big5:.0f}% of outlays; interest is in the top few")
assert 74 <= big5 <= 82, "the ~78% top-five share" """,),
    md("### 1.5 The 'growth fixes it' lever -- and where it stalls"),
    code("""e = con.execute(
    "SELECT year, crude_mbpd, natural_gas_mboed, total_boe_mbpd "
    "FROM energy ORDER BY year"
).fetchdf()
fig, ax = plt.subplots()
ax.plot(e.year, e.total_boe_mbpd, marker="o", ms=3, label="total (M boe/d)")
ax.plot(e.year, e.crude_mbpd, ls="--", label="crude only")
ax.set(xlabel="year", ylabel="million boe/day", title="The energy lever in the 333 plan")
ax.legend()
plt.show()
print("2028 target ~18.7 -> actual in data:",
      round(e.set_index("year").loc[2028, "total_boe_mbpd"], 1))"""),
    md("### 1.6 Checkpoint"),
    md("""1. How many **times** debt grew between **2006-2016** and **2016-2026**`? Look at the printed ratios.
2. The ratio of held-by-public debt to GDP in 2026 is above 100% -- true or false?  (Check `debt`.)
3. Which single category is **not** in the top-5 but still eats ~20% of 2026 outlays?  (Notice interest is _inside_ the top group on the chart.)
4. **Why** does a 5.3% **30-year** yield matter more than a 2-year yield for the debt story?  One sentence below."""),
    md("""> **Answer to 4:** the 30-year locks in the government's borrowing cost for a generation; the market, not the CBO, decides what that cost is.

---
End of notebook 1. Next: `02-forecast-scenarios` -- what the g-vs-r projections say."""),
]

# ===========================================================================
# 2 - FORECAST SCENARIOS
# ===========================================================================
cells_02 = [
    md(f"# 2. Forecast scenarios\n\n{PRE}"),

    md("""The cinematic device of the video: **let the bond market speak.** The CBO
can forecast Congress's numbers, but the market prices them continuously --
the 2026 30-year auction at 5.3% was the clearest signal in the dataset.

Three levels of thinking:
1. **Baseline** -- debt service vs growth, interest flat at 2.5%, growth heals ~1.2% -> 4.1%.
2. **Static sensitivity** -- what if the market reprices the 30-year (higher effective r)?
3. **The crossover clock** -- the first year growth stays above interest. The single most quotable number."""),

    md("### 2.1 The model in ten lines"),
    md("""Growth g(t) rises linearly from `g2026` (+`GROWTH_STEP`/yr) up to the
`RATE_G` ceiling; effective interest `r` is fixed at `BASELINE_R` unless you
overrule it. The share `interest_pct_gdp` drifts with `g - r`. All
deterministic -- see `../../scripts/model.py`. Baseline crossover: **2031**."""),
    code("# Bootstrap\n" + IMPORTS),
    code("""base = m.project(30)                       # 2026 -> 2055
crossover = m.crossover(base)
print(f"BASELINE_CROSSOVER_YEAR = {crossover}")
assert crossover == 2031, "the video's ~2031 breathing-room anchor"

base_df = pd.DataFrame(base)
fig, ax = plt.subplots()
ax.plot(base_df.year, base_df.g, label="nominal growth g (%)", color="C0")
ax.plot(base_df.year, base_df.r, label="effective interest r (%)", color="C3")
ax.axvline(crossover, color="C2", ls=":", lw=1)
ax.text(crossover, 1.0, "crossover", color="C2", ha="center")
ax.legend(); ax.set(xlabel="year", ylabel="%/year",
                    title="Baseline: g vs r (debt breathes once g >= r)")
plt.show()"""),
    md("### 2.2 The scenario table (flipping the market's rate)"),
    md("""`model.sensitivity()` raises effective r by each stress and reports the new
crossover year."""),
    code("""sens = m.sensitivity()
print(pd.DataFrame(sens).to_string(index=False))
for row in sens:
    print(f"r +{row['r_bp']}bp -> crossover {row['crossover']}")
assert sens[0]["crossover"] > 2031 and sens[-1]["crossover"] == 2038"""),
    code("""fig, ax = plt.subplots()
for row in sens:
    rows = m.project(30, r=round(m.BASELINE_R + row["r_bp"] / 100, 3))
    df = pd.DataFrame(rows)
    x = row["crossover"]
    label = f"r = {df.r.iloc[0]}% (cross {x})" if x else f"r = {df.r.iloc[0]}% (never)"
    ax.plot(df.year, df.g_minus_r, label=label)
ax.axhline(0, color="C2", ls="--")
ax.set(xlabel="year", ylabel="g - r (pp)", title="g - r staying below zero -> no breathing room")
ax.legend(fontsize=8)
plt.show()"""),
    md("### 2.3 The interest decade"),
    md("""The `interest_outlook` table carries a 10-year net-interest sum of
**$20,235B ~ $20.2T**, comfortably above the video's "$16T" headline. The gap
is exactly the tension of the lecture: the 2.5% effective-r framework sits far
below the market's own 5.3%."""),
    code("""out = con.execute("SELECT * FROM interest_outlook ORDER BY fiscal_year").fetchdf()
decade_total = out.net_interest_billions.sum()
print(f"10-year net interest (2026-2036) = ${decade_total:,.0f}B ~ ${decade_total/1000:.1f}T")
assert decade_total > 16_000
fig, ax = plt.subplots()
ax.bar(out.fiscal_year.astype(str), out.net_interest_billions)
ax.set(xlabel="fiscal year", ylabel="$B", title="Growing net-interest bill")
plt.show()"""),
    md("""### 2.4 Checkpoint
1. Under the **baseline**, which year does growth first keep up with interest?
2. Raising r by **84bp** pushes that year to ___; by 158bp to ___ (printed above).
3. A 30-year sell-off works through which channel, **g** or **r**?
4. **Reft**: how slow could the *growth healing* be (`GROWTH_STEP`) so `g2026 = 1.2`
   never crosses 2.5% inside `project(30)`?  Try it below -- that's the pessimistic case."""),
    code("""# TASK: shrink GROWTH_STEP until the crossover disappears in the window.
for step in (0.2, 0.15, 0.1, 0.05):
    m.GROWTH_STEP = step
    m.GROWTH_2026 = 1.2
    print(f"GROWTH_STEP={step}  crossover={m.crossover(m.project(30))}")
m.GROWTH_STEP = 0.26   # reset everything for later notebooks
m.GROWTH_2026 = 1.2
print("reset OK")"""),
    md("""---
End of notebook 2. Next: `03-growth-plan-challenge` -- stress the Bessent "333" plan."""),
]

# ===========================================================================
# 3 - GROWTH-PLAN CHALLENGE
# ===========================================================================
cells_03 = [
    md(f"# 3. The growth-plan challenge: Bessent's \"333\"\n\n{PRE}"),

    md("""The video's reform anchor is the **"333" plan**: 3% growth, 3% primary
deficit, 3M extra barrels of oil equivalent a day by 2028. This notebook
decomposes it into three levers and challenges each one -- the way the video
challenges the CBO's happy path. You leave with a verdict.

* Lever 1 -- **3% growth**: raises g so the crossover arrives sooner.
* Lever 2 -- **3% primary deficit**: the budget shape after interest.
* Lever 3 -- **3M boe/d** by 2028: the energy/revenue pivot.

The composite matters: 3% growth with a 5% primary deficit is still a plan
that loses the plot."""),
    md("### 3.1 What the dataset says about each lever"),
    code("# Bootstrap\n" + IMPORTS),
    code("""fig, axes = plt.subplots(1, 2, figsize=(10, 4))

gq = con.execute(
    "SELECT quarter, real_gdp_growth_pct AS growth_pct_annualized "
    "FROM growth_quarters ORDER BY quarter"
).fetchdf()
q = pd.to_datetime(gq.quarter.str.replace("Q", "-"))
axes[0].plot(q, gq.growth_pct_annualized, marker="o", ms=3)
axes[0].axhline(3.0, color="C3", ls="--", lw=1)
axes[0].set_title("Real growth (annualized, %): the 3% huddle")
axes[0].tick_params(axis="x", rotation=60)

e = con.execute(
    "SELECT year, crude_mbpd, natural_gas_mboed, total_boe_mbpd "
    "FROM energy ORDER BY year"
).fetchdf()
axes[1].plot(e.year, e.total_boe_mbpd, marker="o", ms=3)
axes[1].axhline(18.7, color="C2", ls="--", lw=1)
axes[1].set(title="Total oil+gas (M boe/d) vs 18.7 target")
plt.tight_layout(); plt.show()"""),
    md("### 3.2 Run the 333 plan through the model"),
    code("""plan = m.project(30, g2026=3.0)
plan_df = pd.DataFrame(plan)
c_plan = m.crossover(plan)
print(f"333-plan crossover: {c_plan}")
assert c_plan == 2026, "3% growth immediately outruns 2.5% r"

base = m.project(30)
fig, ax = plt.subplots()
ax.plot(pd.DataFrame(base).year, pd.DataFrame(base).g_minus_r,
        label="baseline g-r (cross 2031)", color="C0")
ax.plot(plan_df.year, plan_df.g_minus_r, label="333 plan g-r", color="C2")
ax.axhline(0, color="C3", ls="--")
ax.set(xlabel="year", ylabel="g - r (pp)", title="Does 3% growth buy early breathing room?")
ax.legend(); plt.show()"""),
    md("### 3.3 The primary-deficit leg"),
    code("""primary = con.execute(
    "SELECT d.fiscal_year, "
    "  d.receipts_billions - (d.outlays_billions - COALESCE(i.net_interest_billions, 0)) "
    "    AS primary_surplus_billions "
    "FROM deficit d LEFT JOIN interest i ON i.year = d.fiscal_year "
    "WHERE d.fiscal_year >= 2000 ORDER BY d.fiscal_year"
).fetchdf()
gdp_map = con.execute("SELECT year, nominal_gdp_billions FROM gdp").fetchdf()
primary["pct_gdp"] = primary.primary_surplus_billions / (
    gdp_map.set_index("year").reindex(primary.fiscal_year).nominal_gdp_billions.values
)
fig, ax = plt.subplots()
ax.plot(primary.fiscal_year, primary.pct_gdp, marker="o", ms=3)
ax.axhline(0, color="C3")
ax.set(xlabel="fiscal year", ylabel="primary surplus, % GDP",
       title="Primary balance: the discipline lever")
plt.show()
print("2026 primary surplus:", round(primary.pct_gdp.iloc[-1] * 100, 2), "% of GDP")
assert abs(primary.pct_gdp.iloc[-1] * 100 + 3.0) < 1.0, "the 3% primary deficit" """,),
    md("### 3.4 The composite verdict"),
    md("""Pull the pieces together. **3% growth** hands the crossover far earlier
than the market-stressed baselines of notebook 2. **3% primary deficit** keeps
the debt-to-GDP slope flat even before interest falls. The **energy lever** is
the only one the executive branch can *fund* -- but it moves the slowest.

Verdict questions (write in the cell below):
1. Growth 3%: policy lever, or weather forecast?
2. Primary deficit 3%: does the data show it is achievable?  (Count `primary_surplus_flag` years.)
3. Energy 3M boe/d by 2028: is the 18.7M boe/d target met in our data?"""),
    code("""flags = con.execute(
    "SELECT SUM(primary_surplus_flag) AS years, COUNT(*) AS total FROM deficit"
).fetchdf()
print("Years of primary surplus in 1977-2026:",
      int(flags.years.iloc[0]), "of", int(flags.total.iloc[0]))
e28 = con.execute(
    "SELECT total_boe_mbpd FROM energy WHERE year = 2028"
).fetchdf().total_boe_mbpd.iloc[0]
print("2028 total production (target 18.7):", round(e28, 1))
assert int(flags.years.iloc[0]) == 12"""),
    md("""---
End of notebook 3. Next: the open-ended **capstone project**."""),
]

# ===========================================================================
# 4 - CAPSTONE PROJECT
# ===========================================================================
cells_04 = [
    md(f"# 4. Capstone project: your own debt thesis\n\n{PRE}"),

    md("""Mission (capstone level): **choose one scenario, load the dataset, refit a
chart, and defend a verdict** -- the exact move the video makes, with your own
numbers.

Pick **one**:
* **A — "The market is the truth"**: show g - r under the 5.3% 30-year, and
  defend whether the ~2038 (or never) crossover is the real price.
* **B — "Growth is the medicine"**: the 333 plan with 3% + 3% + 3M -- prove
  the answer still hinges on the primary deficit, not the interest bill.
* **C — "The interest hurricane"**: replay `02` but make the stress a *path*:
  higher in 2026-2030, fading after. Find the crossover.

Deliverable: a working cell + a written defense under `DEFENSE`. Graders value
(1) a clear hypothesis, (2) a reproducible cell, (3) a defense that names the
*lever* that would change your answer."""),
    md("### 4.1 Boot"),
    code("# Bootstrap\n" + IMPORTS),
    code("""print(con.execute(
    "SELECT year, debt_total_billions, debt_held_public_pct_gdp "
    "FROM debt ORDER BY year").fetchdf())"""),
    md("### 4.2 Your scenario"),
    code("""# TASK: pick A, B, or C
SCENARIO = "A"   # <- change me

if SCENARIO == "A":
    rows = m.project(50, r=round(m.BASELINE_R + m.STATIC_R_BP[2] / 100, 3))
elif SCENARIO == "B":
    rows = m.project(50, g2026=3.0)
else:
    rows = m.project(50)
df = pd.DataFrame(rows)
print("SCENARIO:", SCENARIO, " crossover:", m.crossover(rows))"""),
    code("""fig, ax = plt.subplots()
ax.plot(df.year, df.g, label="g", color="C0")
ax.plot(df.year, df.r, label="r", color="C3")
ax.plot(df.year, df.interest_pct_gdp, ls="--", color="k", label="interest % GDP")
c = m.crossover(rows)
if c:
    ax.axvline(c, color="C2", ls=":", lw=1)
    ax.text(c, df.g.min(), "crossover", color="C2", ha="center")
ax.legend(); ax.set(xlabel="year", ylabel="%/year",
                    title=f"Scenario {SCENARIO}")
plt.show()"""),
    md("### 4.3 Sensitivity your way"),
    code("""# TASK: which lever moves IT?  (r = market rate, g = growth healing)
lever = "r"
if lever == "r":
    table = m.sensitivity()
else:
    table = []
    for step in (0.26, 0.35, 0.5, 0.75):
        m.GROWTH_STEP = step
        table.append({"step": step, "crossover": m.crossover(m.project(50))})
    m.GROWTH_STEP = 0.26
print(pd.DataFrame(table).to_string(index=False))"""),
    md("""### 4.4 Defense

> **DEFENSE** — write 3-5 sentences here. Name: (a) your scenario, (b) the
> *lever* that matters most to your answer, (c) the single number that would
> make you change your mind, (d) a counterargument. Then run
> **Kernel -> Restart & Run All** to prove reproducibility.

---
End of the unit. Debrief with `lesson_plans.md`, `exercises.md`, `solutions.md`."""),
]

for name, cells in (
    ("01-understand-the-debt.ipynb", cells_01),
    ("02-forecast-scenarios.ipynb", cells_02),
    ("03-growth-plan-challenge.ipynb", cells_03),
    ("04-capstone-project.ipynb", cells_04),
):
    write(name, cells)