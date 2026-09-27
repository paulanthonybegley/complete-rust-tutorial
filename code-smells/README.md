# Code Smells · Spring Boot 4 + Thymeleaf + HTMX

An educational app following **The Serious CTO**'s playlist
[Code Smells: Identifying and Refactoring Troubled Code](https://www.youtube.com/playlist?list=PL8KjQF_t916yAkxl3bvLD7c1j6-Pj9sb3) (15 videos).

15 labs, each pairing a smelly snippet with its refactored twin, crude-but-honest
metrics, an LCS line diff, and a two-question quiz. Attempts are stored in Postgres
so the scoreboard is real evidence.

## Run it

```bash
# with Docker (recommended)
cd code-smells
docker compose up --build
# app -> http://localhost:8084   (db on 5435)

# or locally (needs a Postgres on localhost:5435, or point SPRING_DATASOURCE_* elsewhere)
mvn spring-boot:run
```

## What's inside

- `src/main/java/com/example/codesmells/domain/SmellRegistry.java` — the syllabus: all 15 labs (summary, symptom, fix, why, quiz options), ordered pedagogically.
- `src/main/resources/snippets/<slug>/{Before,After}.java` — 30 tiny, honest Java examples (the teaching material).
- `SniffMetrics` — deterministic line heuristics (code lines, comments, methods, longest method, fields, type mentions).
- `Diff` — small LCS line diff so the app shows *what* the refactor changed.
- `AttemptStore` — persists every quiz attempt to Postgres (`smell_attempts`).
- Templates use HTMX for the quiz check (server returns the verdict + refreshed scoreboard).
- `lessons/` — lesson plan (OLD.txt 9-input framework), exercises, solutions.
- `education.md` — the full build record: what was made, why, how, the fifteen lab chapters, and the defects verification caught (~15,400 words).

## The 15 labs (teaching order)

1. Long Method · 2. God Class · 3. Data Class · 4. Primitive Obsession · 5. Temporary Field
6. Feature Envy · 7. Inappropriate Intimacy · 8. Divergent Change · 9. Switch Statements
10. Lazy Class · 11. Class Obsession · 12. Middle Man · 13. Speculative Generality
14. Comments · 15. Hidden Bugs

## Learning approach (from `lessons/lesson-plan.md`)

Read the summary, then the smelly code, try to name the smell + fix **before** looking at
the After panel, then take the quiz. Scoring a *double-right* (both questions correct) is
the target. Metrics/diff are there so the refactor's benefit is visible, not asserted.
