# Lesson Plan · Code Smells: Identifying and Refactoring Troubled Code

This lesson suite is a system, not a prompt — it follows the 9 inputs from `OLD.txt`:
1. Learning goal
2. Lesson sequence
3. Assessment evidence (EES)
4. Learner profile
5. Prior knowledge
6. Learning activities
7. Output requirements
8. Accessibility & supports
9. Teacher decisions

## 1. Learning Goal
Learners will be able to identify 15 common code smells from real, tiny examples, choose
the correct refactoring for each, explain *why* the refactor improves cohesion/coupling,
and demonstrate it by reasoning about code metrics and a before/after diff.

## 2. Lesson Sequence
The order is pedagogical, not the playlist's publication order. It builds the
master refactoring skill first, then specific smells.

| Lesson | Focus | Time | Notes |
|---|---|---|---|
| L1 | Long Method | 10 min | Extract Method (master skill). |
| L2 | God Class | 10 min | Extract Class, Facade keeps callers working. |
| L3 | Data Class | 10 min | Move Behaviour into the class (tell: behaviour lives elsewhere). |
| L4 | Primitive Obsession | 10 min | Replace Primitive with Object (Value Objects). |
| L5 | Temporary Field | 10 min | Make state valid or hoist to parameters. |
| L6 | Feature Envy | 10 min | Move Method to data owner. |
| L7 | Inappropriate Intimacy | 10 min | Law of Demeter, cut bidirectional coupling. |
| L8 | Divergent Change | 10 min | One class, one reason to change (SRP). |
| L9 | Switch Statements | 10 min | Replace Conditional with Polymorphism. |
| L10 | Lazy Class | 10 min | Inline Class / Collapse Hierarchy. |
| L11 | Class Obsession | 10 min | Prefer composition where useful. |
| L12 | Middle Man | 10 min | Remove Middle Man. |
| L13 | Speculative Generality | 10 min | YAGNI: delete speculative layers. |
| L14 | Comments | 10 min | Remove redundant/misleading/dead; rename for intent. |
| L15 | Hidden Bugs | 10 min | Types, units, bounds, edge cases. |

## 3. Assessment Evidence (EES)
Assessment is built into the app.

| Evidence | Type | Criteria |
|---|---|---|
| Quiz at end of each lab | Formative | Both identify and refactor must be correct (double-right). Saved to DB. |
| Scoreboard | Formative/Summative | Leaderboard + per-lab % (attempts count; mastery = double-right %). |
| Code metrics comparison | Analytical | Explain why codeLines/methodCount/fieldCount/typeMentions changed. |
| Diff reasoning | Analytical | Point to 2–3 changed hunks and explain the refactor. |
| Five-line fix (exercise) | Product | Write Before→After on a tiny variant matching the smell's fix. |

## 4. Learner Profile
Intermediate Java developers (1–3 years). Prefer tight loops: read small example → see metrics/diff → test choice.

## 5. Prior Knowledge
- Java syntax (fields, methods, classes, interfaces)
- Basic refactorings (Extract Method, Extract Class, Move Method)
- OOP (cohesion/coupling)
- Reading diffs

## 6. Learning Activities
| Activity | Purpose | Mode |
|---|---|---|
| Preview + video | Activate prior knowledge | Independent |
| Read Before.java | Spot symptom | Active reading |
| Compare metrics | Quantify (evidence) | Guided |
| Read After.java | See alternative | Guided |
| Read diff (LCS) | See *what* changed | Guided |
| Two-question quiz | Apply ID + fix | Retrieval practice |
| Exercise variant | Reproduce refactor | Practice |
| Scoreboard check | Metacognitive | Reflection |

## 7. Output Requirements
- Run locally (`mvn spring-boot:run`) and via Docker (`docker compose up --build`)
- Snippets in `src/main/resources/snippets/*/{Before,After}.java`
- Postgres `smell_attempts` persisted (indexes)
- Metrics, diff, quiz verdicts per lab
- Ports: app `8084:8080`, db `5435:5432`

## 8. Accessibility & Supports
- Monospaced, line numbers visible
- Two-column + highlighted diff
- Multiple choice reduces load
- Short, declarative text
- Replayable in any order

## 9. Teacher Decisions
- Pedagogical sequence (L1 first) over playlist order
- Say *one common cure*, explain why
- Metrics = heuristics (not parser)
- Save every attempt (no shame); doubles rewarded
- Tiny snippets (20–80 lines) so diff obvious
