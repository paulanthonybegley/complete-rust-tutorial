# linkedin-post.md — share the unit

> Draft post. Trim to taste; keep the numbers consistent with `solutions.md`.

---

I gave my data learners the same question the markets give every fiscal
statement: **does growth ever outrun the interest bill?**

Built a 4-notebook DuckDB unit from a debt-crisis lecture's own numbers —
$40T debt, ~$3B/day interest, 3.2% of GDP, a 5.3% 30-year — and made the
whole thing **deterministic**: fixed anchors + seeded noise, zero RNG, so a
notebook run today reproduces the same answers years from now, on any machine.

What the unit teaches:

- **Understand**: the five headline facts, re-derived from one `.duckdb` file
- **Analyze**: the g-vs-r crossover clock (baseline “breathing room” in
  2031; +84bp slides it to 2035, +158bp to 2038)
- **Challenge**: decompose the “333” growth plan into its three levers and
  judge each against the data (3% growth → immediate crossover; primary
  –3.0% of GDP; 18.7 M boe/d by 2028)
- **Capstone**: pick a scenario, refit a chart, defend a verdict

Test that actually protects the lesson: 19 pytest invariants + a headless-run
gate on every notebook. No Docker required — the DB is one shared file.

Stack: DuckDB 1.5.5 (pinned to match the CLI side-car), pandas, matplotlib,
Jupyter.

If you teach fiscal literacy or data analysis, the repo is the output of a
lesson-plan framework I'm walking through again — happy to share the design
inputs. DM me.

#DuckDB #DataEducation #DataLiteracy #Jupyter #Finance2026