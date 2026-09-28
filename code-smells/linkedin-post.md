# LinkedIn post — "I wrote 30 files of deliberately bad Java, then fixed all of them"

> Suggested hook + body for the creator's timeline feed. Drop the blockquote
> (it is a staging note, not post text) and paste the plain paragraphs.

---

**"Code smell" courses describe. I made you commit to an answer first, then showed
you the number that proves you were right."**

The Serious CTO's *"Code Smells: Identifying and Refactoring Troubled Code"* covers
15 smells with real conviction — the god class that grew because it was already
imported, the hierarchy that is a museum, the abstraction built for a future nobody
chose. Watching is one thing. I wanted something you can **be wrong at** — so I
built `code-smells`: 15 labs, each pairing a smelly snippet with its refactored
twin, in one `docker-compose.yml`.

**The punchline is evidence on the page, not screenshots.** Every lab shows the
smelly code, the fixed code, seven metrics for both, an LCS line diff, and a quiz:

- **Long Method** — `33 → 8` longest method, while code lines go `37 → 68`. Yes,
  it got *longer*. That inversion is the lesson: you paid in lines to buy names.
- **God Class** — 7 fields become 3. Not deleted: **dispersed**, each extracted
  class carrying only the state its own concern needs.
- **Switch Statements** — `15 → 5` switch/case lines. Repetition was never the
  syntax, it was the third time you had to remember the same list.
- **Comments** — `10 → 0` comment lines, and the misleading one next to
  `// this is wrong` deleted along with the code it lied about.
- **Hidden Bugs** — and the lab whose code gets *bigger*, because you trade lines
  for loud failures: widened types, named units, bounds checks.

Quiz is two questions — *which smell?* and *which refactoring?* — because those are
different skills. Both right is a double-right. Every attempt lands in Postgres, so
the leaderboard is evidence, and the per-lab percentages tell an instructor where
the class actually needs another fifteen minutes.

**I reordered the playlist.** It starts with God Class. My course starts with Long
Method, because Extract Method is the prerequisite for eleven of the other fourteen
smells. The tour order is chronological; the teaching order is pedagogical. Those
are different jobs and conflating them is why most smell lists feel like trivia.

**The part worth reading — what actually broke.** (Everyone's favorite track.)

- **My headline metric was lying.** "Longest method" reported `11 → 9` on a refactor
  that takes a 33-line method to 6. It was measuring the *gaps between method-shaped
  lines* — and method **calls** are method-shaped. Rewrote it to walk brace depth
  from each declaration. Now it says `33 → 8`. No test failed and no log line
  appeared, because a metric is a **claim**, and a claim needs a test.
- So I wrote 13 tests that don't check exact numbers — they check **direction**.
  Long Method's longest method *must* shrink. Hidden Bugs *must not* shrink. A
  snippet edit that makes a lesson weaker now fails the build.
- **The most expensive bug I shipped was prose.** Four labs' explanations claimed
  metrics that moved the other way. Every page rendered. Every test passed. Caught
  only by making the app print its numbers and reading them against my own
  sentences. A pedagogical defect is invisible to every check in the build.
- One of those claims was a feature I'd promised but never built ("the dashboard
  counts switch branches"). So I built the counter instead of softening the copy.
- Thymeleaf fragment args are expressions, not a function call — writing
  `metrics(table(${m}), label('Before'))` 500'd all fifteen labs. The home page
  still rendered, so a `/` smoke test would have shipped it.
- `th:text="'you said " + ${answer} + '"'` 500'd every quiz submit: the literal's
  quote terminated the attribute. Your template's problem is usually that the view
  is doing presentation work.
- `Set.of` rejects duplicates at class-init — two duplicate words in a stop-list
  became `NoClassDefFoundError` on 10/10 tests.
- Ports 8080/8081/8083 and 5432/5433/5434 were taken by my own earlier demos, so
  this one is **:8084** and **:5435**.

And because "a lesson plan is a system, not a prompt", the repo ships a full course
on that 9-input framework (`lessons/`) — goal, sequence, assessment evidence,
learner profile, prior knowledge, activities, output requirements, accessibility,
teacher decisions — plus exercises, an answer key, and a 15,400-word `education.md`
that documents the trade-offs and all four defects.

**Try it (all local, Docker only):** `cd code-smells && docker compose up --build` →
http://localhost:8084

#refactoring #codesmells #java #springboot #teaching #softwarearchitecture #cleancode
